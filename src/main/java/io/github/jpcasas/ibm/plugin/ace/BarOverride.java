package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
			bars = new ArrayList<>(FileUtils.listFiles(new File("."), new String[] { "bar" }, true));

		} else {
			bars = new ArrayList<File>();
			bars.add(barFileName);
		}

		if (!overrideFile.exists()) {
			getLog().info("NO PROPERTIES FOR PROFILE " + overrideFile);
			if (validate) {
				throw new MojoFailureException("Override file not found: " + overrideFile);
			}
			return;
		}
		for (File bar : bars) {
			getLog().info("OVERRIDE BAR FILE " + bar.getName());
			// String artifactId = FilenameUtils.removeExtension(bar.getName());
			String[] args = new String[] { "-b", bar.getAbsolutePath(),
					// "-k", artifactId,
					"-p", overrideFile.getAbsolutePath(), "-runtime", "-r" };
			getLog().info("APPLYING PROPERTIES " + overrideFile.getName());
			ApplyBarOverride.execute(args);
		}

	}

}
