package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author Juan Pablo Casas
 */

@Mojo(name = "ace-clean")
public class BarClean extends AbstractMojo {

	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;

	@Parameter(defaultValue = "${project}", required = true)
	private MavenProject project;

	@Parameter(defaultValue = "bar", property = "ibm.ace.clean.ext", required = false)
	private String extension;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("----------------------------------------------------");
		getLog().info("               IBM ACE Clean Workspace                ");
		getLog().info("----------------------------------------------------");
		getLog().info(" ");
		boolean ok = true;
		String message = "";
		if (outputDirectory != null && outputDirectory.exists()) {
			File[] output = outputDirectory.listFiles();

			for (File file : output) {
				if (file.isDirectory()) {
					try {
						FileUtils.deleteDirectory(file);
					} catch (IOException e) {
						ok = false;
						message += "\r\n" + e.getMessage();
					}
				} else if (!file.getName().endsWith(extension) && !file.getName().endsWith("tar.gz")) {
					file.delete();
				}
			}
		}
		if (!ok) {
			throw new MojoExecutionException(message);
		}

	}

}
