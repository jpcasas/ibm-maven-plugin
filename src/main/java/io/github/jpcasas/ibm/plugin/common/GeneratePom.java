package io.github.jpcasas.ibm.plugin.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import io.github.jpcasas.ibm.plugin.utils.Tools;

/**
 *
 * @author Juan Pablo Casas
 */
@Mojo(name = "generate-pom", requiresProject = false)
public class GeneratePom extends AbstractMojo  implements CommonConstants {

	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;

	@Parameter(defaultValue = "${session}", required = true)
	private MavenSession session;

	@Parameter(defaultValue = "dev.jpcasas.integration", property = "ibm.groupId", required = true)
	private String groupId;

	@Parameter(defaultValue = "1.0.0", property = "version", required = true)
	private String version;

	@Parameter(defaultValue = "artifactId", property = "artifactId", required = true)
	private String artifactId;

	@Parameter(defaultValue = "${project}", required = true)
	private MavenProject project;

	@Parameter(property = "ibm.apic.projectType")
	private String projectType;

	



	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("----------------------------------------------------");
		getLog().info("        Integration Bus - GENERATE POM              ");
		getLog().info("----------------------------------------------------");
		getLog().info(" ");
		try {
			File projectFile = new File(project.getBasedir(), ".project");
			if (projectFile.exists()) { // The file .project exists
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder;
				try {
					dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(projectFile);
					doc.getDocumentElement().normalize();
					String artifact = getProjectName(doc);
					if (projectType == null || projectFile.length() == 0) {
						projectType = getProjectType(doc);
					}
					if (projectType == null || !PROJECT_TYPES.contains(projectType)) {
						throw new MojoExecutionException("Unknown project type");
					}
					getLog().info("The type of the project is: " + projectType);
					
					// Create the POM file
					String workspace = new File(System.getProperty("user.dir")).getParentFile().getParentFile()
							.getName();

					createPom(projectType, artifact, groupId, workspace);

				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (XPathExpressionException e) {
					e.printStackTrace();
				}

			}
		} catch (IOException e) {
			throw new MojoExecutionException("No file model found it", e);
		}

	}

	private void createDir(List<String> listDirToCreate) {
		if (listDirToCreate == null || listDirToCreate.size() == 0) {
			return;
		}
		for (String curDir : listDirToCreate) {
			File curFileDir = new File(curDir);
			if (!curFileDir.exists()) {
				getLog().info("Create the directory: " + curDir);
				new File(curDir).mkdirs();
			}
		}
	}

	private void createPom(String projectType, String artifactId, String groupId, String workspace) throws IOException {
		getLog().info("Create the XML pom file...");
		String model = null;
		Properties props = new Properties();
		props.put("artifactId", artifactId);
		props.put("groupId", groupId);
		props.put("type", projectType);
		if (workspace != null) {
			props.put("workspace", workspace);
		} else {
			props.put("workspace", artifactId);
		}

		model = getFile(projectType + "_pom.xml");
		String pom = Tools.template(model, props, null);
		PrintWriter pr = new PrintWriter(new File(project.getBasedir(), "pom.xml"));
		pr.println(pom);
		pr.flush();
		pr.close();

	}

	private String getProjectType(Document doc) throws XPathExpressionException {
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xp = xpf.newXPath();
		String natures = xp.evaluate("/projectDescription/natures", doc.getDocumentElement());
		if (natures.contains("com.ibm.etools.msgbroker.tooling.applicationNature")) {
			return PROJECT_TYPE_ACE_APPLICATION;
		} else if (natures.contains("com.ibm.etools.msgbroker.tooling.libraryNature")) {
			if (natures.contains("com.ibm.etools.msgbroker.tooling.sharedLibraryNature")) {
				return PROJECT_TYPE_ACE_SHARED_LIBRARY;
			} else {
				return PROJECT_TYPE_ACE_STATIC_LIBRARY;
			}
		} else if (natures.contains("org.eclipse.jdt.core.javanature")) {
			return PROJECT_TYPE_ACE_JAVA;
		} else if (natures.contains("com.ibm.etools.mft.policy.ui.Nature")) {
			return PROJECT_TYPE_ACE_POLICY;
		} else if (natures.contains("com.ibm.etools.msg.validation.msetnature")) {
			return PROJECT_TYPE_ACE_STATIC_LIBRARY;
		} else if (!FileUtils.listFiles(new File("."), new String[] { "yaml", "yml" }, true).isEmpty()) {
			return PROJECT_TYPE_APIC;
		} else {
			return null;
		}

	}

	private String getProjectName(Document doc) throws XPathExpressionException {
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xp = xpf.newXPath();
		String text = xp.evaluate("/projectDescription/name/text()", doc.getDocumentElement());
		return text;
	}

	public String getFile(String fileName) throws IOException {

		ClassLoader classLoader = getClass().getClassLoader();

		try (InputStream inputStream = classLoader.getResourceAsStream(fileName)) {

			return IOUtils.toString(inputStream, StandardCharsets.UTF_8);

		}
	}
}