package io.github.jpcasas.ibm.plugin.ace;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

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

	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
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
		getLog().info("               IBM ACE Generate Poperties           ");
		getLog().info("----------------------------------------------------");
		getLog().info(" ");

		String artifact = project.getArtifactId();
		File barfile = new File(outputDirectory, artifact + extension);
		if (barfile.exists()) {
			try {
				File resources = new File(pfolder);
				resources.mkdirs();
				File pmodel = new File(resources, "DEV.properties");

				PrintWriter pr = (dryRun) ? null : new PrintWriter(pmodel);

				ByteArrayOutputStream baout = new ByteArrayOutputStream();
				System.setOut(new PrintStream(baout));
				String params[] = new String[] { "-r", "-b", barfile.getAbsolutePath(), "-runtime" };
				ReadBar.execute(params);
				String outputLines[] = new String(baout.toByteArray()).split("\n");
				boolean print = false;
				int spaces = 0;
				boolean start = false;
				for (String string : outputLines) {
					if (print) {
						if (start) {
							spaces = getSpaces(string);
							start = false;
						}
						if (spaces == getSpaces(string)) {
							if (!dryRun) {
								if (pr != null) {
									pr.println(string.trim());
									pr.flush();
								}
							} else {
								getLog().info(string.trim());
							}
						} else {
							print = false;
						}

					}

					if (string.contains("Deployment descriptor")) {
						print = true;
						start = true;

					}

				}
				if (!dryRun) {
					if (pr != null)
						pr.close();
					Tools.copyFiles(pmodel, resources, env, pextension);
				}
			} catch (FileNotFoundException e) {
				throw new MojoExecutionException("Can't write file", e);
			} catch (IOException e) {
				throw new MojoExecutionException("Can't copy files", e);
			}
		}

	}

	private static int getSpaces(String string) {
		int sz = string.length();
		int count = 1;
		for (int i = 0; i < sz; i++) {
			if (((int) string.charAt(i)) == 32) {
				count++;
			} else {
				return count;
			}

		}
		return count;
	}

}
