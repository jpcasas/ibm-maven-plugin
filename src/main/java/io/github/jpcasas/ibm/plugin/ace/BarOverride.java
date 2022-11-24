package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.ibm.broker.config.util.ApplyBarOverride;

/**
 *
 * @author Juan Pablo Casas
 */
@Mojo(name = "ace-override", requiresProject = false, defaultPhase = LifecyclePhase.DEPLOY)
public class BarOverride extends AbstractMojo {

	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;

	@Parameter(defaultValue = "${session}", required = true)
	private MavenSession session;

	@Parameter(defaultValue = ".bar", required = true)
	private String ext;

	@Parameter(property = "ibm.ace.overrideFile", required = true)
	private File overrideFile;

	@Parameter(property = "ibm.ace.bar")
	private File barFileName;

	

	@Parameter(property = "ibm.ace.override.validate", defaultValue = "false")
	private boolean validate;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		getLog().info("----------------------------------------------------");
		getLog().info("        IBM ACE   -   OVERRIDE APP                  ");
		getLog().info("----------------------------------------------------");
		getLog().info(" ");
		List<File> bars = null;

		if (barFileName == null) {
			bars = (List<File>) FileUtils.listFiles(new File("."), new String[] { "bar" }, true);

		} else {
			bars = new ArrayList<File>();
			bars.add(barFileName);
		}

		for (File bar : bars) {
			getLog().info("OVERRIDE BAR FILE " + bar.getName());
			// String artifactId = FilenameUtils.removeExtension(bar.getName());
			String[] args = new String[] { "-b", bar.getAbsolutePath(),
					// "-k", artifactId,
					"-p", overrideFile.getAbsolutePath(), "-runtime", "-r" };
			if (overrideFile.exists()) {
				getLog().info("APPLING PROPERTIES " + overrideFile.getName());
				ApplyBarOverride.execute(args);
			} else {
				getLog().info("NOT PROPERTIES FOR PROFILE " + overrideFile);
				if (validate)
					throw new MojoFailureException("No override file found it");
			}
		}

	}

}
