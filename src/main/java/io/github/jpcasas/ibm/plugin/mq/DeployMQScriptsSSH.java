package io.github.jpcasas.ibm.plugin.mq;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.jcraft.jsch.JSchException;

import io.github.jpcasas.ibm.plugin.utils.ServerCredentials;
import io.github.jpcasas.ibm.plugin.utils.SshClient;

/**
 * Runs MQSC scripts on a queue manager through SSH. With several servers
 * (multi-instance queue manager) the node where the queue manager is running is used.
 */
@Mojo(name = "mq-deploy-ssh", requiresProject = false, threadSafe = true)
public class DeployMQScriptsSSH extends DeployMQScripts {

	/**
	 * Remote command. With one {@code %s} (queue manager) the script is sent through stdin
	 * (recommended). Legacy two-placeholder templates ({@code echo "%s" | runmqsc %s}) are
	 * still supported.
	 */
	@Parameter(defaultValue = "runmqsc %s", property = "mq.ssh.exec", required = false)
	private String exec;

	/** Wrapper used to run {@code dspmq} on each node, e.g. to load the mqm profile. */
	@Parameter(defaultValue = "bash --login -c \"%s\"", property = "mq.ssh.runc", required = false)
	private String runc;

	@Parameter(defaultValue = "~/.ssh/id_rsa", property = "mq.ssh.privatekeylocation", required = false)
	private String privateKeyLocation;

	@Parameter(defaultValue = "true", property = "mq.key.auth", required = false)
	private boolean auth;

	@Parameter(defaultValue = "22", property = "mq.ssh.port", required = true)
	private int sshPort;

	/** {@code yes} (default, host must be in known_hosts), {@code accept-new} or {@code no} (insecure). */
	@Parameter(defaultValue = "yes", property = "mq.ssh.strictHostKeyChecking", required = false)
	private String strictHostKeyChecking;

	@Parameter(defaultValue = "~/.ssh/known_hosts", property = "mq.ssh.knownHosts", required = false)
	private String knownHosts;

	@Parameter(defaultValue = "30000", property = "mq.ssh.timeout", required = false)
	private int sshTimeout;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("----------------------------------------------------");
		getLog().info("        IBM MQ - Deploy MQ Scripts (SSH)            ");
		getLog().info("----------------------------------------------------");

		MqscCommand.validateQueueManagerName(queueManagerName);
		if (MqscCommand.isLegacyTemplate(exec)) {
			getLog().warn("mq.ssh.exec uses the deprecated two-placeholder form; prefer 'runmqsc %s' "
					+ "so the script is sent through stdin.");
		}

		Map<File, String> scripts = loadScripts();
		if (scripts.isEmpty()) {
			return;
		}

		ServerCredentials creds = credentials();
		List<String> errors = new ArrayList<>();
		try (SshClient ssh = connectToActiveNode(creds)) {
			for (Map.Entry<File, String> script : scripts.entrySet()) {
				getLog().info("Running " + script.getKey().getName() + " on " + ssh.getHost());
				MqscCommand cmd = MqscCommand.build(exec, queueManagerName, script.getValue());
				getLog().debug(script.getValue());
				SshClient.Result result = ssh.exec(cmd.getCommand(), cmd.getStdin());
				getLog().info(result.getOut());
				if (!result.getErr().trim().isEmpty()) {
					getLog().error(result.getErr());
				}
				if (result.getExitCode() != 0 || result.getOut().contains("command has a syntax error")) {
					errors.add(String.format("MQ script %s failed (exit code %d)", script.getKey(),
							result.getExitCode()));
				}
			}
		} catch (JSchException e) {
			throw new MojoExecutionException(
					String.format("Error connecting to QM %s on %s: %s", queueManagerName, mqServer, e.getMessage()), e);
		} catch (IOException e) {
			throw new MojoExecutionException("Error running MQ scripts: " + e.getMessage(), e);
		}
		failIfErrors(errors);
	}

	private SshClient.Options options(String host, ServerCredentials creds) {
		return new SshClient.Options().host(host).port(sshPort).user(creds.getUsername())
				.password(creds.getPassword())
				.privateKey(creds.getPrivateKey() != null ? creds.getPrivateKey() : privateKeyLocation)
				.passphrase(creds.getPassphrase()).keyAuth(auth).strictHostKeyChecking(strictHostKeyChecking)
				.knownHosts(knownHosts).timeoutMillis(sshTimeout);
	}

	private SshClient connectToActiveNode(ServerCredentials creds)
			throws JSchException, IOException, MojoExecutionException {
		List<String> servers = servers();
		if (servers.size() == 1) {
			return SshClient.connect(options(servers.get(0), creds), getLog());
		}
		String dspmq = String.format(runc, "dspmq -m " + queueManagerName);
		for (String host : servers) {
			getLog().info("Checking queue manager " + queueManagerName + " on " + host);
			SshClient ssh = null;
			try {
				ssh = SshClient.connect(options(host, creds), getLog());
				SshClient.Result r = ssh.exec(dspmq, null);
				if (r.getExitCode() == 0 && r.getOut().contains("STATUS(Running)")) {
					getLog().info("Active node: " + host);
					SshClient active = ssh;
					ssh = null;
					return active;
				}
				getLog().info(host + " is not the active instance: " + r.getOut().trim());
			} catch (JSchException e) {
				getLog().warn("Unable to connect to " + host + ": " + e.getMessage());
			} finally {
				if (ssh != null) {
					ssh.close();
				}
			}
		}
		throw new MojoExecutionException(
				"Queue manager " + queueManagerName + " is not running on any of " + servers);
	}
}
