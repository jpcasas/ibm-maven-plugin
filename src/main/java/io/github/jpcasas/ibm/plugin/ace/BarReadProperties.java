package io.github.jpcasas.ibm.plugin.ace;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.ibm.broker.config.util.ReadBar;

import io.github.jpcasas.ibm.plugin.utils.Tools;

/**
 *
 * @author Juan Pablo Casas
 */

@Mojo(name = "ace-properties")
public class BarReadProperties extends AbstractMojo {

	@Parameter(defaultValue = "../workspace", property = "outputDir", required = true)
	private File outputDirectory;

	@Parameter(defaultValue = "${project}", required = true)
	private MavenProject project;

	@Parameter(defaultValue = ".bar", property = "ibm.ace.extension", required = true)
	private String extension;

	@Parameter(defaultValue = "QA,CERT,PRD", property = "ibm.environments", required = true)
	private String env;

	@Parameter(defaultValue = ".properties", property = "ibm.ace.pextension", required = false)
	private String pextension;

	@Parameter(defaultValue = "resources/overrides", property = "ibm.ace.propertiesFolder", required = false)
	private String pfolder;

	@Parameter(defaultValue = "false", property = "ibm.ace.dryRun", required = false)
	private boolean dryRun;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("----------------------------------------------------");
		getLog().info("               IBM ACE Generate Properties           ");
		getLog().info("----------------------------------------------------");
		getLog().info(" ");

		String artifact = project.getArtifactId();
		File barfile = new File(outputDirectory, artifact + extension);
		if (!barfile.exists()) {
			getLog().warn("Bar file not found: " + barfile.getAbsolutePath());
			return;
		}
		File resources = new File(pfolder);
		resources.mkdirs();
		File pmodel = new File(resources, "DEV.properties");

		String[] outputLines = readBar(barfile).split("\\r?\\n");
		List<String> properties = extractDeploymentDescriptor(outputLines);
		if (dryRun) {
			properties.forEach(getLog()::info);
			return;
		}
		try (PrintWriter pr = new PrintWriter(pmodel, StandardCharsets.UTF_8.name())) {
			properties.forEach(pr::println);
		} catch (IOException e) {
			throw new MojoExecutionException("Can't write file " + pmodel, e);
		}
		try {
			Tools.copyFiles(pmodel, resources, env, pextension);
		} catch (IOException e) {
			throw new MojoExecutionException("Can't copy files", e);
		}
	}

	/**
	 * Runs IBM's ReadBar, which prints to System.out, capturing its output. System.out is
	 * always restored (it used to stay redirected for the rest of the Maven build).
	 */
	private static synchronized String readBar(File barfile) {
		PrintStream original = System.out;
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		try (PrintStream capture = new PrintStream(baout, true, StandardCharsets.UTF_8.name())) {
			System.setOut(capture);
			ReadBar.execute(new String[] { "-r", "-b", barfile.getAbsolutePath(), "-runtime" });
		} catch (java.io.UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		} finally {
			System.setOut(original);
		}
		return new String(baout.toByteArray(), StandardCharsets.UTF_8);
	}

	/** Returns the lines of the first "Deployment descriptor" block (same indentation). */
	static List<String> extractDeploymentDescriptor(String[] outputLines) {
		List<String> result = new ArrayList<>();
		boolean print = false;
		boolean start = false;
		int spaces = 0;
		for (String line : outputLines) {
			if (print) {
				if (start) {
					spaces = getSpaces(line);
					start = false;
				}
				if (spaces == getSpaces(line) && !line.trim().isEmpty()) {
					result.add(line.trim());
				} else {
					print = false;
				}
			}
			if (line.contains("Deployment descriptor")) {
				print = true;
				start = true;
			}
		}
		return result;
	}

	private static int getSpaces(String string) {
		int count = 1;
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) != ' ') {
				return count;
			}
			count++;
		}
		return count;
	}

}
