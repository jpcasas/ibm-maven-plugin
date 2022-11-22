package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.ibm.broker.MessageBrokerAPIException;
import com.ibm.broker.config.appdev.FlowRendererBAR;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "ace-policy-package", requiresProject = false)
public class PolicyPackage extends AbstractMojo {

    @Parameter(property = "ibm.ace.policy.projectFolder", required = true)
    private File projectFile;

    @Parameter(property = "ibm.ace.policy.destination.dir", required = false)
    private String destDir;

    @Parameter(defaultValue = ".bar", property = "ibm.ace.extension", required = true)
    private String extension;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        getLog().info("----------------------------------------------------");
        getLog().info("               ACE - PACKAGE POLICY                 ");
        getLog().info("----------------------------------------------------");
        getLog().info(" ");

        try {
            File projectf = new File(projectFile, ".project");
            String folderName = projectFile.getName();
            if (projectf.exists()) {
                String artifact = folderName;
                getLog().info("        [BAR] Building " + artifact + "                   ");
                String workspace = projectFile.getParentFile().getAbsolutePath();
                ArrayList<String> individual = new ArrayList<String>();
                individual.add(folderName);
                getLog().info(folderName);
                if (destDir == null) {
                    destDir = workspace;
                }

                FlowRendererBAR.write(workspace, individual, destDir, artifact + extension, 0, true);

                getLog().info("        [BAR] FILE " + new File(new File(destDir), artifact + extension)
                        + "                   ");

                getLog().info(" ");
                getLog().info("(\"-------------------------END-------------------------");
            } else {
                throw new MojoFailureException("Project not found, Check if the .project exists into the folder");
            }
        } catch (IOException e) {
            throw new MojoFailureException("IOException", e);
        } catch (MessageBrokerAPIException e) {
            throw new MojoFailureException("MessageBrokerAPIException", e);

        }

    }
}
