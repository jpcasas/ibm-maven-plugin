package io.github.jpcasas.ibm.plugin.mq;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.SettingsDecrypter;

import io.github.jpcasas.ibm.plugin.utils.ServerCredentials;
import io.github.jpcasas.ibm.plugin.utils.Tools;

/**
 * Common parameters and script loading for the MQ deployment goals.
 */
public abstract class DeployMQScripts extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true)
	MavenProject project;

	@Parameter(defaultValue = "${settings}", readonly = true)
	Settings settings;

	@Component
	SettingsDecrypter settingsDecrypter;

	@Parameter(defaultValue = "properties", property = "mq.pextension", required = false)
	String pextension;

	@Parameter(alias = "enviromentProperties", defaultValue = "env.properties", property = "mq.env.properties", required = true)
	File environmentProperties;

	@Parameter(defaultValue = ",", property = "mq.separator", required = false)
	String separator;

	@Parameter(property = "mq.servers", required = true)
	String mqServer;

	@Parameter(property = "mq.queueManager", required = true)
	String queueManagerName;

	@Parameter(property = "mq.scripts.install", required = false)
	File mqscriptsInstall;

	@Parameter(property = "mq.scripts.uninstall", required = false)
	File mqscriptsUninstall;

	@Parameter(defaultValue = "install", property = "mq.operation", required = false)
	String operation;

	@Parameter(property = "mq.user", required = false)
	String user;

	/** Prefer {@code mq.serverId}: passwords on the command line leak into shell history. */
	@Parameter(property = "mq.password", required = false)
	String password;

	/** Id of a {@code <server>} in settings.xml holding the username/password (can be encrypted). */
	@Parameter(property = "mq.serverId", required = false)
	String serverId;

	protected ServerCredentials credentials() throws MojoExecutionException {
		return ServerCredentials.resolve(serverId, user, password, settings, settingsDecrypter);
	}

	protected List<String> servers() {
		List<String> list = new ArrayList<>();
		for (String s : mqServer.split(java.util.regex.Pattern.quote(separator == null ? "," : separator))) {
			if (!s.trim().isEmpty()) {
				list.add(s.trim());
			}
		}
		return list;
	}

	/**
	 * Renders every {@code .mqs/.mqsc} script of the selected operation with the
	 * environment properties and its own {@code <script>.<pextension>} file.
	 *
	 * @return script file → rendered MQSC, in a stable (sorted) order
	 */
	protected Map<File, String> loadScripts() throws MojoExecutionException {
		if (!"install".equals(operation) && !"uninstall".equals(operation)) {
			throw new MojoExecutionException("mq.operation must be 'install' or 'uninstall', got: " + operation);
		}
		File folder = "install".equals(operation) ? mqscriptsInstall : mqscriptsUninstall;
		Map<File, String> scripts = new LinkedHashMap<>();
		if (folder == null || !folder.exists()) {
			getLog().warn("No MQ scripts folder for operation '" + operation + "': " + folder);
			return scripts;
		}
		List<File> files = new ArrayList<>(FileUtils.listFiles(folder, new String[] { "mqs", "mqsc" }, true));
		Collections.sort(files);

		File base = project != null && project.getBasedir() != null ? project.getBasedir() : new File(".");
		for (File mqs : files) {
			try {
				Properties props = new Properties();
				load(props, environmentProperties);
				String propsName = String.format("%s.%s", FilenameUtils.removeExtension(mqs.getName()), pextension);
				load(props, Tools.searchFile(base, propsName));
				String model = new String(Files.readAllBytes(mqs.toPath()), StandardCharsets.UTF_8);
				scripts.put(mqs, Tools.getMQSC(model, props));
			} catch (IOException e) {
				throw new MojoExecutionException("Unable to read MQ script " + mqs + ": " + e.getMessage(), e);
			}
		}
		return scripts;
	}

	private static void load(Properties props, File file) throws IOException {
		if (file != null && file.isFile()) {
			try (Reader r = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
				props.load(r);
			}
		}
	}

	protected void failIfErrors(Collection<String> errors) throws org.apache.maven.plugin.MojoFailureException {
		if (!errors.isEmpty()) {
			for (String e : errors) {
				getLog().error(e);
			}
			throw new org.apache.maven.plugin.MojoFailureException(
					errors.size() + " MQ script(s) failed, check the log for details");
		}
	}
}
