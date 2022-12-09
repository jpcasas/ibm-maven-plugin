package io.github.jpcasas.ibm.plugin.ace.gnative;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "clean")
public class CleanGoal extends AbstractMojo {
    
    

    @Parameter(defaultValue = "${project}", required = true)
	private MavenProject project;

    @Parameter(defaultValue = "workspace", property = "ibm.ace.build.workspace", required = false)
    private String workspaceName;

    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("----------------------------------------------------");
		getLog().info("          IBM ACE Clean workspace               ");
		getLog().info("----------------------------------------------------");
		getLog().info(" ");
        File basedir = project.getBasedir();
        File workspace = new File(basedir.getParentFile(), workspaceName);
        if(workspace.exists()){
            try {
                FileUtils.deleteDirectory(workspace);
                FileUtils.deleteDirectory(outputDirectory);
                
                FileUtils.listFiles(basedir, new String[]{"bar"}, false).forEach(file -> file.delete());
            } catch (IOException e) {
                getLog().error("Can't remove the workspace folder ../"+workspaceName);
                throw new MojoExecutionException("Can't remove the workspace folder ../"+workspaceName);
            }
            
        }
    }
    
}
