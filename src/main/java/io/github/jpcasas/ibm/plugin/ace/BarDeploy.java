package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.SettingsDecrypter;

import io.github.jpcasas.ibm.plugin.model.DeployResult;
import io.github.jpcasas.ibm.plugin.model.LogEntry;
import io.github.jpcasas.ibm.plugin.utils.ServerCredentials;
import io.github.jpcasas.ibm.plugin.utils.SslContexts;
import io.github.jpcasas.ibm.plugin.utils.Tools;
import kong.unirest.HttpRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;
import kong.unirest.json.JSONObject;

/**
 * Deploys BAR files through the ACE administration REST API ({@code /apiv2}).
 *
 * @author Juan Pablo Casas
 */
@Mojo(name = "ace-deploy", requiresProject = false, threadSafe = true)
public class BarDeploy extends AbstractMojo {

	@Parameter(property = "ibm.ace.sec.truststore", required = false)
	private String truststore;

	@Parameter(property = "ibm.ace.sec.truststorePassword", required = false)
	private String truststorePassword;

	@Parameter(property = "ibm.ace.sec.keystore", required = false)
	private String keystore;

	@Parameter(property = "ibm.ace.sec.keystorePassword", required = false)
	private String keystorePassword;

	@Parameter(property = "ibm.ace.sec.user", required = false)
	private String user;

	/** Prefer {@code ibm.ace.serverId}: passwords on the command line leak into shell history. */
	@Parameter(property = "ibm.ace.sec.password", required = false)
	private String password;

	/** Id of a {@code <server>} in settings.xml holding the username/password (can be encrypted). */
	@Parameter(property = "ibm.ace.serverId", required = false)
	private String serverId;

	/** Request timeout in seconds. */
	@Parameter(defaultValue = "600", property = "ibm.ace.timeout", required = false)
	private int timeout;

	@Parameter(defaultValue = "false", property = "ibm.ace.useSSL", required = false)
	private boolean useSSL;

	/**
	 * Disables TLS certificate and host name verification. Only for test environments with
	 * self-signed certificates: prefer {@code ibm.ace.sec.truststore}.
	 */
	@Parameter(defaultValue = "false", property = "ibm.ace.ssl.insecure", required = false)
	private boolean insecure;

	@Parameter(property = "ibm.ace.host", required = true)
	private String host;

	@Parameter(defaultValue = "4414", property = "ibm.ace.port")
	private int port;

	@Parameter(property = "ibm.ace.is")
	private String integrationServer;

	@Parameter(property = "ibm.ace.bar", required = false)
	private File barFileName;

	@Parameter(defaultValue = "${settings}", readonly = true)
	private Settings settings;

	@Component
	private SettingsDecrypter settingsDecrypter;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("----------------------------------------------------");
		getLog().info("        Integration Bus - DEPLOY BAR                ");
		getLog().info("----------------------------------------------------");
		getLog().info(" ");

		List<File> bars;
		if (barFileName == null) {
			bars = new ArrayList<>(FileUtils.listFiles(new File("."), new String[] { "bar" }, true));
		} else {
			bars = new ArrayList<>();
			bars.add(barFileName);
		}
		if (bars.isEmpty()) {
			throw new MojoFailureException("No bar files found to deploy");
		}

		ServerCredentials creds = ServerCredentials.resolve(serverId, user, password, settings, settingsDecrypter);
		if (creds.hasUsername() && !useSSL) {
			getLog().warn("Credentials are sent in clear text over HTTP to " + host
					+ ": enable ibm.ace.useSSL=true to protect them");
		}
		try (UnirestInstance http = createClient()) {
			String type = detectDeploymentType(http, creds);
			List<String> failed = new ArrayList<>();
			for (File bar : bars) {
				getLog().info("Deploying " + bar.getAbsolutePath() + "... MODE: " + type);
				try {
					if (!deploy(http, creds, bar, "integrationNode".equals(type) ? integrationServer : null)) {
						failed.add(bar.getName());
					}
				} catch (IOException e) {
					getLog().error("Error reading " + bar + ": " + e.getMessage());
					failed.add(bar.getName());
				}
			}
			if (!failed.isEmpty()) {
				throw new MojoFailureException("The following bar files weren't deployed: " + failed);
			}
		}
	}

	private UnirestInstance createClient() throws MojoExecutionException {
		UnirestInstance http = Unirest.spawnInstance();
		// no redirects: they would resend the Authorization header, possibly to another host
		http.config().connectTimeout(timeout * 1000).socketTimeout(timeout * 1000).followRedirects(false);
		if (useSSL) {
			if (insecure) {
				getLog().warn("TLS certificate verification is DISABLED (ibm.ace.ssl.insecure=true)");
				http.config().verifySsl(false);
			} else if (SslContexts.notBlank(truststore) || SslContexts.notBlank(keystore)) {
				try {
					http.config().sslContext(
							SslContexts.create(truststore, truststorePassword, keystore, keystorePassword));
				} catch (GeneralSecurityException | IOException e) {
					http.close();
					throw new MojoExecutionException("Unable to load the trust/key store: " + e.getMessage(), e);
				}
			}
		}
		return http;
	}

	private String baseUrl() {
		return String.format("%s://%s:%d/apiv2", useSSL ? "https" : "http", host, port);
	}

	/** Encodes a value used as a single URL path segment (e.g. "../x" cannot change the path). */
	static String pathSegment(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	private <R extends HttpRequest<?>> R auth(R request, ServerCredentials creds) {
		if (creds.hasUsername()) {
			String pass = creds.getPassword() == null ? "" : creds.getPassword();
			request.header("Authorization", "Basic " + Tools.base64(creds.getUsername() + ":" + pass));
		}
		return request;
	}

	private boolean deploy(UnirestInstance http, ServerCredentials creds, File bar, String is) throws IOException {
		String url = is != null ? String.format("%s/servers/%s/deploy", baseUrl(), pathSegment(is))
				: baseUrl() + "/deploy";
		HttpResponse<DeployResult> response = auth(http.post(url), creds).header("accept", "application/json")
				.header("Content-Type", "application/octet-stream").body(Tools.toByteArray(bar))
				.asObject(DeployResult.class);

		if (!response.isSuccess() || response.getBody() == null) {
			getLog().error("----------------------------------------------------");
			getLog().error("        Deploy failed for " + bar.getName());
			getLog().error("      STATUS:   " + response.getStatus() + " " + response.getStatusText());
			getLog().error("----------------------------------------------------");
			return false;
		}
		DeployResult result = response.getBody();
		getLog().info("Result deploying " + bar.getName());
		LogEntry[] entries = result.getLogEntry();
		boolean ok = true;
		if (entries != null && entries.length > 0) {
			LogEntry info = entries[0];
			ok = info.getMessage() != null && "0".equals(info.getMessage().getSeverity());
			printEntries(entries);
		}
		getLog().info("----------------------------------------------------");
		getLog().info(ok ? "        Application DEPLOYED" : "        Application NOT DEPLOYED");
		getLog().info("----------------------------------------------------");
		return ok;
	}

	private void printEntries(LogEntry[] logs) {
		getLog().info("        Logs:                                    ");
		for (LogEntry logEntry : logs) {
			getLog().info("----------------------------------------------------");
			getLog().info(logEntry.getDetailedText());
		}
	}

	private String detectDeploymentType(UnirestInstance http, ServerCredentials creds) {
		try {
			HttpResponse<JsonNode> res = auth(http.get(baseUrl() + "/"), creds).asJson();
			JsonNode body = res.getBody();
			if (res.isSuccess() && body != null && body.getObject() != null) {
				JSONObject json = body.getObject();
				if (json.has("type")) {
					return json.getString("type");
				}
			}
			getLog().warn("Unable to detect the deployment type (HTTP " + res.getStatus()
					+ "), assuming integrationNode");
		} catch (RuntimeException e) {
			getLog().warn("Unable to detect the deployment type: " + e.getMessage() + ", assuming integrationNode");
		}
		return "integrationNode";
	}

}
