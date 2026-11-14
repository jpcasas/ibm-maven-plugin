package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.xml.sax.SAXException;

import com.ibm.broker.MessageBrokerAPIException;
import com.ibm.broker.config.appdev.FlowRendererBAR;

import io.github.jpcasas.ibm.plugin.model.eclipse.ProjectDescriptor;
import io.github.jpcasas.ibm.plugin.utils.Tools;
import net.lingala.zip4j.ZipFile;

/**
 *
 * @author Juan Pablo Casas
 */

@Mojo(name = "ace-bar", defaultPhase = LifecyclePhase.PACKAGE)
public class BarPackage extends AbstractMojo {

	@Parameter(defaultValue = "../workspace", property = "outputDir", required = true)
	private File outputDirectory;

	@Parameter(defaultValue = "${project}", required = true)
	private MavenProject project;

	@Parameter(defaultValue = "true", property = "deployAsSource", required = true)
	private boolean deployAsSource;

	@Parameter(defaultValue = "false", property = "trace", required = true)
	private boolean trace;

	@Parameter(defaultValue = ".bar", property = "ibm.ace.extension", required = true)
	private String extension;

	@Parameter(defaultValue = "resources,properties", property = "ibm.ace.resources.folders", required = true)
	private String resourcesFolders;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("----------------------------------------------------");
		getLog().info("               IBM ACE Packaging Bar                ");
		getLog().info("----------------------------------------------------");
		getLog().info(" ");

		try {
			File baseDir = project.getBasedir();
			File projectf = new File(baseDir, ".project");
			if (projectf.exists()) {
				String artifact = project.getArtifactId();
				getLog().info("        [BAR] Building " + artifact + "                   ");

				String workspace = outputDirectory.getCanonicalPath();
				ArrayList<String> individual = new ArrayList<String>();
				individual.add(artifact);
				getLog().debug("Workspace: " + workspace);

				ProjectDescriptor descriptor = Tools.parseEclipseProject(projectf);

				// for policies and libs
				//
				if (descriptor.getNatures().contains("com.ibm.etools.msgbroker.tooling.libraryNature")) {
					File zipFile = new File(outputDirectory, artifact + ".zip");
					try (ZipFile installer = new ZipFile(zipFile)) {
						installer.addFolder(baseDir);
					}
					project.getArtifact().setFile(zipFile);
				} else {

					FlowRendererBAR.write(workspace, individual, workspace, artifact + extension, 0, true);
					File barfile = new File(outputDirectory, artifact + extension);
					FileUtils.copyFileToDirectory(barfile, baseDir);
					getLog().info("BAR File copied from "+barfile.getAbsolutePath()+" to folder "+baseDir.getAbsolutePath());
					File zipFile = new File(outputDirectory, artifact + ".zip");
					try (ZipFile installer = new ZipFile(zipFile)) {
						installer.addFile(barfile);
						for (String rfolder : resourcesFolders.split(",")) {
							File rfile = new File(baseDir, rfolder.trim());
							if (rfile.isDirectory()) {
								installer.addFolder(rfile);
							}
						}
					}
					project.getArtifact().setFile(zipFile);
					getLog().info("Artifact " + zipFile.getAbsolutePath());
				}

				
				
			} else {
				throw new MojoFailureException("Project not found, Check if the .project exists into the folder");
			}
		} catch (IOException e) {
			throw new MojoFailureException("Error packaging bar: " + e.getMessage(), e);
		} catch (MessageBrokerAPIException e) {
			throw new MojoFailureException("Error building bar: " + e.getMessage(), e);
		} catch (ParserConfigurationException | SAXException e) {
			throw new MojoFailureException("Error parsing .project file: " + e.getMessage(), e);
		}

	}
}
