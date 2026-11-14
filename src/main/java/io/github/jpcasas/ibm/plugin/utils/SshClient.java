package io.github.jpcasas.ibm.plugin.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * Small SSH client on top of JSch.
 * <ul>
 * <li>Host keys are verified against a known_hosts file (no silent MITM).</li>
 * <li>Data (e.g. MQSC scripts) is sent through the channel's stdin instead of being
 * interpolated into a shell command line.</li>
 * <li>stdout and stderr are captured without the risk of blocking on a full pipe.</li>
 * </ul>
 */
public class SshClient implements AutoCloseable {

	/** Values accepted by {@code StrictHostKeyChecking}. */
	public static final List<String> HOST_KEY_MODES = Arrays.asList("yes", "accept-new", "no");

	private static final List<String> DEFAULT_KEYS = Arrays.asList("id_ed25519", "id_ecdsa", "id_rsa");

	private final Session session;
	private final String host;

	public static final class Result {
		private final int exitCode;
		private final String out;
		private final String err;

		Result(int exitCode, String out, String err) {
			this.exitCode = exitCode;
			this.out = out;
			this.err = err;
		}

		public int getExitCode() {
			return exitCode;
		}

		public String getOut() {
			return out;
		}

		public String getErr() {
			return err;
		}
	}

	public static final class Options {
		String host;
		int port = 22;
		String user;
		String password;
		String privateKey;
		String passphrase;
		boolean keyAuth = true;
		String strictHostKeyChecking = "yes";
		String knownHosts;
		int timeoutMillis = 30000;

		public Options host(String v) { host = v; return this; }
		public Options port(int v) { port = v; return this; }
		public Options user(String v) { user = v; return this; }
		public Options password(String v) { password = v; return this; }
		public Options privateKey(String v) { privateKey = v; return this; }
		public Options passphrase(String v) { passphrase = v; return this; }
		public Options keyAuth(boolean v) { keyAuth = v; return this; }
		public Options strictHostKeyChecking(String v) { strictHostKeyChecking = v; return this; }
		public Options knownHosts(String v) { knownHosts = v; return this; }
		public Options timeoutMillis(int v) { timeoutMillis = v; return this; }
	}

	private SshClient(Session session, String host) {
		this.session = session;
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public static SshClient connect(Options o, Log log) throws JSchException {
		String mode = o.strictHostKeyChecking == null ? "yes" : o.strictHostKeyChecking.toLowerCase(Locale.ROOT);
		if (!HOST_KEY_MODES.contains(mode)) {
			throw new IllegalArgumentException(
					"Invalid strictHostKeyChecking '" + o.strictHostKeyChecking + "', expected one of " + HOST_KEY_MODES);
		}
		String user = o.user;
		if (user == null || user.trim().isEmpty()) {
			user = System.getenv("LOGNAME");
			if (user == null || user.trim().isEmpty()) {
				user = System.getProperty("user.name");
			}
		}

		JSch jsch = new JSch();
		File knownHosts = new File(expandHome(o.knownHosts == null ? "~/.ssh/known_hosts" : o.knownHosts));
		if ("no".equals(mode)) {
			log.warn("SSH host key verification is DISABLED for " + o.host
					+ " (strictHostKeyChecking=no). The connection is vulnerable to man-in-the-middle attacks.");
		} else {
			if (!knownHosts.exists()) {
				if ("yes".equals(mode)) {
					throw new JSchException("known_hosts file " + knownHosts + " not found. Add the host key with "
							+ "'ssh-keyscan -p " + o.port + " -H " + o.host + " >> " + knownHosts
							+ "' or set mq.ssh.strictHostKeyChecking=accept-new");
				}
				createEmpty(knownHosts);
			}
			jsch.setKnownHosts(knownHosts.getAbsolutePath());
			if ("accept-new".equals(mode)) {
				jsch.setHostKeyRepository(new AcceptNewRepository(jsch.getHostKeyRepository(), log));
			}
		}

		Session session = jsch.getSession(user, o.host, o.port);
		// accept-new is implemented by AcceptNewRepository: JSch itself must stay strict so a
		// CHANGED key is always rejected
		session.setConfig("StrictHostKeyChecking", "no".equals(mode) ? "no" : "yes");

		boolean keyAdded = false;
		if (o.keyAuth) {
			for (File key : candidateKeys(o.privateKey)) {
				if (key.isFile()) {
					log.debug("Using SSH private key " + key.getAbsolutePath());
					if (o.passphrase != null && !o.passphrase.isEmpty()) {
						jsch.addIdentity(key.getAbsolutePath(), o.passphrase);
					} else {
						jsch.addIdentity(key.getAbsolutePath());
					}
					keyAdded = true;
					break;
				}
			}
			if (!keyAdded) {
				log.debug("No SSH private key found, falling back to password authentication");
			}
		}
		if (o.password != null && !o.password.isEmpty()) {
			session.setPassword(o.password);
		}
		session.connect(o.timeoutMillis);
		return new SshClient(session, o.host);
	}

	private static void createEmpty(File file) throws JSchException {
		try {
			File parent = file.getAbsoluteFile().getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}
			if (!file.exists()) {
				Files.createFile(file.toPath());
			}
		} catch (IOException e) {
			throw new JSchException("Unable to create " + file + ": " + e.getMessage(), e);
		}
	}

	/**
	 * OpenSSH {@code accept-new}: unknown hosts are trusted and recorded, but a host whose
	 * key differs from the recorded one is rejected.
	 */
	static final class AcceptNewRepository implements HostKeyRepository {
		private final HostKeyRepository delegate;
		private final Log log;

		AcceptNewRepository(HostKeyRepository delegate, Log log) {
			this.delegate = delegate;
			this.log = log;
		}

		@Override
		public int check(String host, byte[] key) {
			int result = delegate.check(host, key);
			if (result == NOT_INCLUDED) {
				try {
					HostKey hk = new HostKey(host, key);
					delegate.add(hk, null);
					log.warn("Permanently added " + host + " (" + hk.getType() + " " + hk.getFingerPrint(new JSch())
							+ ") to the list of known hosts");
					return OK;
				} catch (JSchException e) {
					log.error("Unable to record host key for " + host, e);
				}
			}
			return result;
		}

		@Override
		public void add(HostKey hostkey, UserInfo ui) {
			delegate.add(hostkey, ui);
		}

		@Override
		public void remove(String host, String type) {
			delegate.remove(host, type);
		}

		@Override
		public void remove(String host, String type, byte[] key) {
			delegate.remove(host, type, key);
		}

		@Override
		public String getKnownHostsRepositoryID() {
			return delegate.getKnownHostsRepositoryID();
		}

		@Override
		public HostKey[] getHostKey() {
			return delegate.getHostKey();
		}

		@Override
		public HostKey[] getHostKey(String host, String type) {
			return delegate.getHostKey(host, type);
		}
	}

	static List<File> candidateKeys(String configured) {
		List<File> keys = new ArrayList<>();
		if (configured != null && !configured.trim().isEmpty()) {
			keys.add(new File(expandHome(configured)));
		}
		for (String name : DEFAULT_KEYS) {
			keys.add(new File(expandHome("~/.ssh/" + name)));
		}
		return keys;
	}

	/** Expands a leading {@code ~} to the user's home directory (Java does not do it). */
	public static String expandHome(String path) {
		if (path == null) {
			return null;
		}
		if (path.equals("~") || path.startsWith("~/") || path.startsWith("~" + File.separator)) {
			return System.getProperty("user.home") + path.substring(1);
		}
		return path;
	}

	/**
	 * Executes a remote command.
	 *
	 * @param command remote command line
	 * @param stdin   data written to the command's standard input, may be {@code null}
	 */
	public Result exec(String command, String stdin) throws JSchException, IOException {
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		try {
			channel.setCommand(command);
			channel.setInputStream(stdin == null ? null
					: new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)));
			ByteArrayOutputStream err = new ByteArrayOutputStream();
			channel.setErrStream(err, true);
			InputStream in = channel.getInputStream();
			channel.connect();
			String out = IOUtils.toString(in, StandardCharsets.UTF_8);
			while (!channel.isClosed()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IOException("Interrupted while waiting for the remote command", e);
				}
			}
			return new Result(channel.getExitStatus(), out, new String(err.toByteArray(), StandardCharsets.UTF_8));
		} finally {
			channel.disconnect();
		}
	}

	@Override
	public void close() {
		session.disconnect();
	}
}
