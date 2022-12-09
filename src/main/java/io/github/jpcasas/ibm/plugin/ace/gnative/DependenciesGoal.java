package io.github.jpcasas.ibm.plugin.ace.gnative;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

@Mojo(name = "dependencies")
public class DependenciesGoal extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    @Parameter(defaultValue = "workspace", property = "ibm.ace.build.workspace", required = false)
    private String workspaceName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("----------------------------------------------------");
        getLog().info("          IBM ACE downloads dependencies            ");
        getLog().info("----------------------------------------------------");
        getLog().info(" ");
        File basedir = project.getBasedir();
        File pomFile = new File(basedir, "pom.xml");
        File workspace = new File(basedir.getParentFile(), workspaceName);
        workspace.mkdirs();
        if(pomFile.exists()){
            InvocationRequest request = new DefaultInvocationRequest();
            
            request.setBaseDirectory(basedir);
            request.setPomFile(pomFile);
            request.setGoals(Arrays.asList("dependency:unpack-dependencies"));
            request.setBatchMode(true);
            Properties props = new Properties();
            props.put("excludeTransitive", "true");
            props.put("outputDirectory", "../"+workspaceName);
            request.setProperties(props);
            Invoker invoker = new DefaultInvoker();
            
            try {               
                invoker.execute(request);
                FileUtils.deleteDirectory(new File(basedir, "target"));
            } catch (MavenInvocationException e) {
                getLog().error("Can't call dependency goal", e);
                throw new MojoExecutionException(e);
            } catch (IOException e) {
                getLog().error("Can't clean target folder", e);
                
            }
        }

        

    }

}
