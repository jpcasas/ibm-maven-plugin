package io.github.jpcasas.ibm.plugin.utils;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.SettingsProblem;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;

/**
 * Resolves credentials from a {@code <server>} entry of settings.xml, decrypting
 * passwords encrypted with {@code mvn --encrypt-password}. This avoids passing secrets
 * on the command line ({@code -Dpassword=...}), where they end up in shell history and
 * process listings.
 */
public final class ServerCredentials {

	private final String username;
	private final String password;
	private final String privateKey;
	private final String passphrase;

	private ServerCredentials(String username, String password, String privateKey, String passphrase) {
		this.username = username;
		this.password = password;
		this.privateKey = privateKey;
		this.passphrase = passphrase;
	}

	public static ServerCredentials of(String username, String password) {
		return new ServerCredentials(username, password, null, null);
	}

	/**
	 * @param serverId  id of the server in settings.xml; when blank the explicit values are returned
	 * @param username  explicit user (takes precedence over settings.xml)
	 * @param password  explicit password (takes precedence over settings.xml)
	 */
	public static ServerCredentials resolve(String serverId, String username, String password, Settings settings,
			SettingsDecrypter decrypter) throws MojoExecutionException {
		if (isBlank(serverId)) {
			return of(username, password);
		}
		Server server = settings == null ? null : settings.getServer(serverId);
		if (server == null) {
			throw new MojoExecutionException("Server '" + serverId + "' not found in settings.xml");
		}
		if (decrypter != null) {
			SettingsDecryptionResult result = decrypter.decrypt(new DefaultSettingsDecryptionRequest(server));
			for (SettingsProblem problem : result.getProblems()) {
				if (problem.getSeverity() != SettingsProblem.Severity.WARNING) {
					throw new MojoExecutionException(
							"Unable to decrypt server '" + serverId + "': " + problem.getMessage());
				}
			}
			server = result.getServer();
		}
		return new ServerCredentials(isBlank(username) ? server.getUsername() : username,
				isBlank(password) ? server.getPassword() : password, server.getPrivateKey(), server.getPassphrase());
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public boolean hasUsername() {
		return !isBlank(username);
	}
}
