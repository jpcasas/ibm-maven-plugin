package io.github.jpcasas.ibm.plugin.ace.gnative;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;


@Mojo(name = "dependencies", requiresDependencyResolution = ResolutionScope.COMPILE)
public class DependenciesGoal extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", required = true)
    private MavenSession session;

    @Component
    private BuildPluginManager pluginManager;

    @Parameter(defaultValue = "workspace", property = "ibm.ace.build.workspace", required = false)
    private String workspaceName;


    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("----------------------------------------------------");
        getLog().info("          IBM ACE downloads dependencies            ");
        getLog().info("----------------------------------------------------");
        getLog().info(" ");
        executeMojo(
                plugin(groupId("org.apache.maven.plugins"), artifactId("maven-dependency-plugin"), version("3.4.0")),
                goal("unpack-dependencies"),
                configuration(
                        element(name("outputDirectory"), "../" + workspaceName),
                        element(name("stripClassifier"), "true")),
                executionEnvironment(project, session, pluginManager)

        );
        try {
            
            FileUtils.deleteDirectory(outputDirectory);
        } catch (IOException e) {
            getLog().error("Can't clean target folder", e);

        }

    }

}
