package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.util.ArrayList;

import com.ibm.broker.config.appdev.FlowRendererBAR;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "ace-policy-bar", defaultPhase = LifecyclePhase.PACKAGE)
public class BarPolicyCreation extends AbstractMojo {

	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;

	@Parameter(defaultValue = "${project}", required = true)
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("----------------------------------------------------");
		getLog().info("               IBM ACE Packaging Bar                ");
		getLog().info("----------------------------------------------------");
		getLog().info(" ");

		try {
			File projectf = new File(project.getBasedir(), ".project");
			if (projectf.exists()) {
				String artifact = project.getArtifactId();
				getLog().info("        [BAR] Building " + artifact + "                   ");

				String workspace = project.getBasedir().getParent();

				getLog().info("Workspace: " + workspace);
				getLog().info("Artifact: " + artifact);

				// Remove pom.xml (needed for policy bars)
				ArrayList<String> filesToAdd = new ArrayList<String>();
				filesToAdd.add(artifact);
				FlowRendererBAR.write(project.getBasedir().getParent(), filesToAdd, project.getBasedir() + "/target/",
						artifact + ".bar", 0, true);

				getLog().info(" ");
				getLog().info("(\"-------------------------END-------------------------");
			} else {
				throw new MojoFailureException("Project not found, Check if the .project exists into the folder");
			}
			/*
			 * } catch (IOException e) { throw new MojoFailureException("IOException", e); }
			 * catch (MessageBrokerAPIException e) { throw new
			 * MojoFailureException("MessageBrokerAPIException", e);
			 */
		} catch (Exception e) {
			throw new MojoFailureException("Exception", e);
		}

	}

	

}
