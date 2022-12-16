package io.github.jpcasas.ibm.plugin.ace.gnative;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.ibm.broker.MessageBrokerAPIException;
import com.ibm.broker.config.appdev.FlowRendererBAR;

import io.github.jpcasas.ibm.plugin.utils.Tools;

@Execute(phase = LifecyclePhase.PACKAGE)
@Mojo(name = "ace-verify")
public class RunTestProjectGoal extends AbstractMojo {

    

    @Parameter(defaultValue = "workspace", property = "ibm.ace.build.workspace", required = false)
    private String workspaceName;

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("----------------------------------------------------");
        getLog().info("          IBM ACE Package & Run Tests               ");
        getLog().info("----------------------------------------------------");
        getLog().info(" ");

        File baseDir = project.getBasedir();
        getLog().info("Is MQSIPROFILE set:  " + Tools.isMqsiprofileSet());
        File workspaceFolder = new File(baseDir.getParentFile(), workspaceName);
        getLog().info("Workingspace " + workspaceFolder.getAbsolutePath() + " is present: " + workspaceFolder.exists());
        String projectTestName = project.getArtifactId() + "_Tests";
        File testProjectFolder = new File(workspaceFolder, projectTestName);
        getLog().info(
                "Test Project " + testProjectFolder.getAbsolutePath() + " is present: " + testProjectFolder.exists());

        if (Tools.isMqsiprofileSet() && workspaceFolder.exists() && testProjectFolder.exists()) {

            try {

                getLog().info(("Project compiled jar file: "
                        + new File(testProjectFolder, projectTestName + ".jar").getAbsolutePath()));
                ArrayList<String> prjs = new ArrayList<>();
                prjs.add(projectTestName);
                prjs.add(project.getArtifactId());

                // Creating test bar
                FlowRendererBAR.write(workspaceFolder.getAbsolutePath(), prjs, workspaceFolder.getAbsolutePath(),
                        "ArtifactToTest.bar", 0, true);

                // install test bar
                File isworkindir = new File(workspaceFolder, "isworkingdir");
                isworkindir.mkdirs();
                Tools.executeCommand("mqsibar --bar-file ArtifactToTest.bar --working-directory isworkingdir", getLog(),
                        workspaceFolder);

                Tools.executeCommand(
                        "IntegrationServer -w isworkingdir --start-msgflows false --test-project " + projectTestName,
                        getLog(), workspaceFolder);

            } catch (IOException e) {
                getLog().error(e);
                throw new MojoExecutionException("IOException", e);
            } catch (MessageBrokerAPIException e) {
                getLog().error(e);
                throw new MojoExecutionException("MessageBrokerAPIException", e);
            } catch (InterruptedException e) {
                getLog().error(e);
            }

        }

    }

}
