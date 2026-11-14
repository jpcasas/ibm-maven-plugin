package io.github.jpcasas.ibm.plugin.ace;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import io.github.jpcasas.ibm.plugin.utils.Tools;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

/**
 *
 * @author Juan Pablo Casas
 */
@Mojo(name = "ace-keywords", requiresProject = false)
public class SetKeyWords extends AbstractMojo {

	@Parameter(property = "ibm.ace.bar", required = false)
	private File barFileName;

	@Parameter(defaultValue = "${session}")
	protected MavenSession session;

	

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("----------------------------------------------------");
		getLog().info("        Integration Bus - KEY WORDS                 ");
		getLog().info("----------------------------------------------------");
		getLog().info(" ");

		List<File> bars = null;

		if (barFileName == null) {
			bars = new ArrayList<>(FileUtils.listFiles(new File("."), new String[] { "bar" }, true));

		} else {
			bars = new ArrayList<File>();
			bars.add(barFileName);
		}

		boolean fail = false;
		for (File bar : bars) {
			getLog().info("Generating KEYWORDS FOR " + bar + "...");
			Map<String, String> system = System.getenv();

			HashMap<String, String> info = new HashMap<String, String>();
			addProperty(info, system, "BUILD_USER");
			addProperty(info, system, "BUILD_USER_ID");
			addProperty(info, system, "BUILD_URL");
			addProperty(info, system, "BUILD_USER_EMAIL");

			addMQSIProps(info, session.getSystemProperties());

			if (generateKeywords(bar, info, system.get("Version"))) {
				getLog().info("Generating KEYWORDS FOR " + bar + " DONE");
			} else {
				fail = true;
			}

		}
		if (fail)
			throw new MojoExecutionException("Error Generating Keywords");
	}

	private void addMQSIProps(HashMap<String, String> info, Properties systemProperties) {

		Set<Object> se = systemProperties.keySet();
		for (Object object : se) {
			String k = (String) object;
			if (k.startsWith("MQSI_")) {
				String val = systemProperties.getProperty(k);
				getLog().info("Setting " + k + " variable");
				String nkey = Tools.camelCase(k.replace("MQSI_", ""));
				info.put(nkey, val);
			}

		}
	}

	private boolean generateKeywords(File barFile, HashMap<String, String> info, String version) {
		File barBackup = new File(barFile.getParentFile(), barFile.getName() + ".bak");
		File tmpFolder = null;
		boolean backedUp = false;
		try {
			tmpFolder = Files.createTempDirectory("ace-keywords").toFile();
			Tools.uncompress(barFile, tmpFolder);
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(CompressionMethod.DEFLATE);
			parameters.setCompressionLevel(CompressionLevel.NORMAL);
			parameters.setFileNameInZip("META-INF/keywords.txt");

			String keyWordsString = getKeyWordsString(info, version);
			for (File file : Tools.searchFiles(tmpFolder, "appzip")) {
				try (net.lingala.zip4j.ZipFile appZip = new net.lingala.zip4j.ZipFile(file)) {
					appZip.addStream(new ByteArrayInputStream(keyWordsString.getBytes(StandardCharsets.UTF_8)),
							parameters);
				}
			}

			Files.move(barFile.toPath(), barBackup.toPath(), StandardCopyOption.REPLACE_EXISTING);
			backedUp = true;
			try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(barFile))) {
				Tools.zipFolder(tmpFolder, "", zipOut);
			}
			Files.delete(barBackup.toPath());
			return true;
		} catch (IOException e) {
			getLog().error("Could not generate keywords for " + barFile.getName() + ": " + e.getMessage(), e);
			if (backedUp) {
				try {
					Files.move(barBackup.toPath(), barFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException restore) {
					getLog().error("Unable to restore " + barFile + " from " + barBackup, restore);
				}
			}
			return false;
		} finally {
			if (tmpFolder != null) {
				FileUtils.deleteQuietly(tmpFolder);
			}
		}
	}

	public static String getKeyWordsString(HashMap<String, String> info, String version) {
		StringBuilder sb = new StringBuilder();
		if (version != null) {
			sb.append(String.format("$MQSI_VERSION=%s MQSI$ ", version));
		}
		Set<String> keys = info.keySet();
		for (String key : keys) {
			sb.append(String.format("$MQSI %s=%s MQSI$ ", key, info.get(key)));
		}
		return sb.toString();
	}

	private void addProperty(HashMap<String, String> info, Map<String, String> system, String key) {
		String val = system.get(key);
		if (val != null) {

			info.put(Tools.camelCase(key), val);
		}

	}

}
