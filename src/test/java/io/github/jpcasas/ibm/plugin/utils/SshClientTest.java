package io.github.jpcasas.ibm.plugin.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.sshd.common.config.keys.PublicKeyEntry;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.jcraft.jsch.JSchException;

/**
 * Runs {@link SshClient} against an embedded SSH server. The fake {@code runmqsc}
 * command echoes its arguments and its stdin, so we can verify the script travels
 * through stdin and never through the command line.
 */
class SshClientTest {

	private static SshServer server;
	private static PublicKey hostKey;

	@BeforeAll
	static void start() throws Exception {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		gen.initialize(2048);
		KeyPair pair = gen.generateKeyPair();
		hostKey = pair.getPublic();

		server = SshServer.setUpDefaultServer();
		server.setHost("127.0.0.1");
		server.setPort(0);
		server.setKeyPairProvider(KeyPairProvider.wrap(pair));
		server.setPasswordAuthenticator((user, pass, session) -> "mqm".equals(user) && "secret".equals(pass));
		server.setCommandFactory((channel, command) -> new EchoCommand(command));
		server.start();
	}

	@AfterAll
	static void stop() throws IOException {
		server.stop(true);
	}

	private SshClient.Options options(File knownHosts, String mode) {
		return new SshClient.Options().host("127.0.0.1").port(server.getPort()).user("mqm").password("secret")
				.keyAuth(false).knownHosts(knownHosts.getAbsolutePath()).strictHostKeyChecking(mode)
				.timeoutMillis(10000);
	}

	private File knownHosts(Path dir) throws IOException {
		File f = dir.resolve("known_hosts").toFile();
		String line = "[127.0.0.1]:" + server.getPort() + " " + PublicKeyEntry.toString(hostKey) + "\n";
		Files.write(f.toPath(), line.getBytes(StandardCharsets.UTF_8));
		return f;
	}

	@Test
	void sendsScriptThroughStdin(@TempDir Path dir) throws Exception {
		try (SshClient ssh = SshClient.connect(options(knownHosts(dir), "yes"), new SystemStreamLog())) {
			SshClient.Result r = ssh.exec("runmqsc QM1", "DEFINE QLOCAL('A') $(touch /tmp/pwned)\n");
			assertEquals(0, r.getExitCode());
			assertEquals("cmd=runmqsc QM1\nDEFINE QLOCAL('A') $(touch /tmp/pwned)\n", r.getOut());
			assertEquals("stderr-line\n", r.getErr());
		}
	}

	@Test
	void reportsExitCode(@TempDir Path dir) throws Exception {
		try (SshClient ssh = SshClient.connect(options(knownHosts(dir), "yes"), new SystemStreamLog())) {
			assertEquals(3, ssh.exec("fail", null).getExitCode());
		}
	}

	@Test
	void rejectsUnknownHostByDefault(@TempDir Path dir) throws Exception {
		File empty = dir.resolve("known_hosts").toFile();
		Files.write(empty.toPath(), Collections.<String>emptyList());
		assertThrows(JSchException.class, () -> SshClient.connect(options(empty, "yes"), new SystemStreamLog()));
	}

	@Test
	void failsWhenKnownHostsIsMissing(@TempDir Path dir) {
		File missing = dir.resolve("missing").toFile();
		JSchException e = assertThrows(JSchException.class,
				() -> SshClient.connect(options(missing, "yes"), new SystemStreamLog()));
		assertTrue(e.getMessage().contains("ssh-keyscan"));
	}

	@Test
	void rejectsChangedHostKey(@TempDir Path dir) throws Exception {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		gen.initialize(2048);
		PublicKey other = gen.generateKeyPair().getPublic();
		File f = dir.resolve("known_hosts").toFile();
		Files.write(f.toPath(), ("[127.0.0.1]:" + server.getPort() + " " + PublicKeyEntry.toString(other) + "\n")
				.getBytes(StandardCharsets.UTF_8));
		assertThrows(JSchException.class, () -> SshClient.connect(options(f, "accept-new"), new SystemStreamLog()));
	}

	@Test
	void acceptNewStoresTheKey(@TempDir Path dir) throws Exception {
		File f = dir.resolve("ssh").resolve("known_hosts").toFile();
		try (SshClient ssh = SshClient.connect(options(f, "accept-new"), new SystemStreamLog())) {
			assertEquals(0, ssh.exec("runmqsc QM1", "x").getExitCode());
		}
		assertTrue(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8).contains("ssh-rsa"));
		// second connection must now succeed in strict mode
		try (SshClient ssh = SshClient.connect(options(f, "yes"), new SystemStreamLog())) {
			assertEquals(0, ssh.exec("runmqsc QM1", "x").getExitCode());
		}
	}

	@Test
	void rejectsInvalidMode(@TempDir Path dir) {
		assertThrows(IllegalArgumentException.class,
				() -> SshClient.connect(options(dir.toFile(), "maybe"), new SystemStreamLog()));
	}

	@Test
	void expandsHome() {
		String home = System.getProperty("user.home");
		assertEquals(home + "/.ssh/id_rsa", SshClient.expandHome("~/.ssh/id_rsa"));
		assertEquals("/etc/ssh", SshClient.expandHome("/etc/ssh"));
		assertEquals(new File(home, ".ssh/id_ed25519"), SshClient.candidateKeys(null).get(0));
	}

	private static final class EchoCommand implements Command, Runnable {
		private final String command;
		private InputStream in;
		private OutputStream out;
		private OutputStream err;
		private ExitCallback exit;

		EchoCommand(String command) {
			this.command = command;
		}

		@Override
		public void setInputStream(InputStream in) {
			this.in = in;
		}

		@Override
		public void setOutputStream(OutputStream out) {
			this.out = out;
		}

		@Override
		public void setErrorStream(OutputStream err) {
			this.err = err;
		}

		@Override
		public void setExitCallback(ExitCallback callback) {
			this.exit = callback;
		}

		@Override
		public void start(ChannelSession channel, Environment env) {
			new Thread(this).start();
		}

		@Override
		public void run() {
			try {
				if ("fail".equals(command)) {
					exit.onExit(3);
					return;
				}
				byte[] stdin = IOUtils.toByteArray(in);
				out.write(("cmd=" + command + "\n").getBytes(StandardCharsets.UTF_8));
				out.write(stdin);
				out.flush();
				err.write("stderr-line\n".getBytes(StandardCharsets.UTF_8));
				err.flush();
				exit.onExit(0);
			} catch (IOException e) {
				exit.onExit(1, e.getMessage());
			}
		}

		@Override
		public void destroy(ChannelSession channel) {
		}
	}
}
