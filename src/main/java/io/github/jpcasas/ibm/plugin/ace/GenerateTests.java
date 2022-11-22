package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import io.github.jpcasas.ibm.plugin.utils.Tools;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "ace-tests")
public class GenerateTests extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;
    @Parameter(defaultValue = "EndpointConfig.java", property = "ibm.ace.tests.endpointClass", required = false)
    private String endPointFileName;

    @Parameter(defaultValue = "resources/tests", property = "ibm.ace.tests.folder", required = false)
    private String testDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("----------------------------------------------------");
        getLog().info("               IBM ACE Generate TESTS                  ");
        getLog().info("----------------------------------------------------");
        getLog().info(" ");

        String projectName = project.getArtifactId();

        File root = new File(System.getProperty("user.dir"));
        File resourcesTest = new File(root, testDir);
        resourcesTest.mkdirs();

        File endPoint = new File(resourcesTest, endPointFileName);
        File dumbTest = new File(resourcesTest, projectName + "IT.java");
        try {
            PrintWriter endWriter = new PrintWriter(endPoint);
            PrintWriter dumbWriter = new PrintWriter(dumbTest);

            String endModel = getFile("tests/EndpointConfig.mustache");
            String testModel = getFile("tests/TestIT.mustache");

            Properties p = new Properties();

            p.put("ArtifactId", projectName);

            String testModelText = Tools.template(testModel, p, null);

            dumbWriter.println(testModelText);
            dumbWriter.flush();
            dumbWriter.close();

            endWriter.println(Tools.template(endModel, p, null));
            endWriter.flush();
            endWriter.close();

        } catch (IOException e1) {

            e1.printStackTrace();
        }

    }

    public String getFile(String fileName) throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(fileName)) {

            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        }
    }

}
