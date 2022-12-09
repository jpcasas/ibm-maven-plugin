package io.github.jpcasas.ibm.plugin.ace;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import net.lingala.zip4j.exception.ZipException;
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
			bars = (List<File>) FileUtils.listFiles(new File("."), new String[] { "bar" }, true);

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

			//setMQSIKeys(info, system);

			generateKeywords(bar, info, system.get("Version"));

			getLog().info("Generating KEYWORDS FOR " + bar + " DONE");

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

	private void setMQSIKeys(HashMap<String, String> info, Map<String, String> system) {
		Set<String> keys = system.keySet();
		for (String key : keys) {
			if (key.startsWith("MQSI_")) {
				String val = system.get(key);
				String nkey = Tools.camelCase(key.replace("MQSI_", ""));
				info.put(nkey, val);
			}
		}

	}

	private void generateKeywords(File barFile, HashMap<String, String> info, String version) {
		File tempdir = FileUtils.getTempDirectory();
		File tmpFolder = new File(tempdir, "keywords");
		tmpFolder.mkdirs();

		File barBackup = new File(barFile.getParentFile(), barFile.getName() + ".bak");
		boolean problem = false;
		try {
			Tools.uncompress(barFile, tmpFolder);
			ArrayList<File> files = Tools.searchFiles(tmpFolder, "appzip");
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(CompressionMethod.DEFLATE);
			parameters.setCompressionLevel(CompressionLevel.NORMAL);

			for (File file : files) {
				net.lingala.zip4j.ZipFile appZip = new net.lingala.zip4j.ZipFile(file);
				String keyWordsString = getKeyWordsString(info, version);
				ByteArrayInputStream bais = new ByteArrayInputStream(keyWordsString.getBytes());
				parameters.setFileNameInZip("META-INF/keywords.txt");
				appZip.addStream(bais, parameters);
				appZip.close();
			}
			String fileName = barFile.getName();

			barFile.renameTo(barBackup);
			File newBarFile = new File(barFile.getParentFile(), fileName);
			FileOutputStream fos = new FileOutputStream(newBarFile);
			ZipOutputStream zipOut = new ZipOutputStream(fos);
			Tools.zipFolder(tmpFolder, "", zipOut);
			zipOut.close();
			fos.close();
			barBackup.delete();
			FileUtils.deleteDirectory(tmpFolder);
		} catch (ZipException e) {
			problem = true;

		} catch (FileNotFoundException e) {
			problem = true;
		} catch (IOException e) {
			problem = true;
		}
		if (problem) {
			barBackup.renameTo(barFile);
			getLog().info("COULD NOT GENERATE KEYWORDS FOR " + barFile.getName() + "...");
		}

	}

	public static String getKeyWordsString(HashMap<String, String> info, String version) {
		StringBuffer sb = new StringBuffer();
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
