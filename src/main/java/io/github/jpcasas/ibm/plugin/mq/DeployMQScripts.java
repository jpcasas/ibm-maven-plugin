package io.github.jpcasas.ibm.plugin.mq;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public class DeployMQScripts extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true)
    MavenProject project;

    @Parameter(defaultValue = "properties", property = "mq.pextension", required = false)
    String pextension;

    @Parameter(defaultValue = "env.properties", property = "mq.env.properties", required = true)
    File enviromentProperties;

    @Parameter(defaultValue = ",", property = "mq.separator", required = false)
    String separator;

    @Parameter(property = "mq.servers", required = true)
    String mqServer;

    @Parameter(property = "mq.queueManager", required = true)
    String queueManagerName;

    @Parameter(property = "mq.scripts.install", required = false)
    File mqscriptsInstall;
    @Parameter(property = "mq.scripts.uninstall", required = false)
    File mqscriptsUninstall;

    @Parameter(defaultValue = "install", property = "mq.operation", required = false)
    String operation;

    @Parameter(property = "mq.user", required = false)
	String user;

	@Parameter(defaultValue = "", property = "mq.password", required = false)
	String password;

	

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

    }
}
