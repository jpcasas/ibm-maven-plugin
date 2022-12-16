package io.github.jpcasas.ibm.plugin.ace.gnative;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import io.github.jpcasas.ibm.plugin.utils.Tools;

public class CompileJavaProject extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    @Parameter(defaultValue = "workspace", property = "ibm.ace.build.workspace", required = false)
    private String workspaceName;

    @Parameter(defaultValue = "%1$s/common/classes/Integration*.jar:%1$s/common/classes/IntegrationAPI.jar:%1$s/server/classes/junit-*.jar", property = "ibm.ace.test.jars", required = false)
    private String classpath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("----------------------------------------------------");
        getLog().info("          IBM ACE Package Generated Tests           ");
        getLog().info("----------------------------------------------------");
        getLog().info(" ");

        File baseDir = project.getBasedir();

        File workspaceFolder = new File(baseDir.getParentFile(), workspaceName);
        getLog().info("Workingspace " + workspaceFolder.getAbsolutePath() + " is present: " + workspaceFolder.exists());
        String projectTestName = project.getArtifactId() + "_Tests";
        File testProjectFolder = new File(workspaceFolder, projectTestName);
        getLog().info(
                "Project " + testProjectFolder.getAbsolutePath() + " is present: " + testProjectFolder.exists());

        if (Tools.isMqsiprofileSet() && workspaceFolder.exists() && testProjectFolder.exists()) {

            String fullClasspath = Tools.buildClasspath(classpath, Tools.getInstallationFolder());

            try {
                Tools.createJar(testProjectFolder, projectTestName, fullClasspath);
            } catch (IOException e) {
                getLog().error("IOException", e);
            }

        }
    }
}
