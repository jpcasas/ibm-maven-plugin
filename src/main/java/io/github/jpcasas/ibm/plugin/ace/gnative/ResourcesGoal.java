package io.github.jpcasas.ibm.plugin.ace.gnative;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "resources")
public class ResourcesGoal extends AbstractMojo {
    
    

    @Parameter(defaultValue = "${project}", required = true)
	private MavenProject project;

    @Parameter(defaultValue = "workspace", property = "ibm.ace.build.workspace", required = false)
    private String workspaceName;

    @Parameter(defaultValue = ".git,properties,resources", property = "ibm.ace.build.prefixFilters", required = true)
    private String prefixToFilter;

    @Parameter(defaultValue = "bar,BAR,zip", property = "ibm.ace.build.suffixFilters", required = true)
    private String suffixToFilter;

    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;

    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("----------------------------------------------------");
		getLog().info("          IBM ACE Preparing workspace               ");
		getLog().info("----------------------------------------------------");
		getLog().info(" ");

        File basedir = project.getBasedir();
        String pname = project.getArtifactId();
        
        File workspace = new File(basedir.getParentFile(), workspaceName);
        workspace.mkdirs();
        
        File dest = new File(workspace, pname);
        dest.mkdirs();
        try {
            IOFileFilter ff = null;
            if(prefixToFilter!=null){
                String ext[] = prefixToFilter.split(",");
                ff = FileFilterUtils.prefixFileFilter(ext[0]);
                for (int i = 1; i < ext.length; i++) {
                    ff = ff.or(FileFilterUtils.prefixFileFilter(ext[i]));
                }
                
            }

            if(suffixToFilter!=null){
                String ext[] = suffixToFilter.split(",");
                if(ff == null){
                    ff = FileFilterUtils.suffixFileFilter(ext[0]);
                }else{
                    ff = ff.or(FileFilterUtils.suffixFileFilter(ext[0]));
                }
                
                for (int i = 1; i < ext.length; i++) {
                    ff = ff.or(FileFilterUtils.suffixFileFilter(ext[i]));
                }
            }
            
            FileUtils.copyDirectory(basedir, dest, FileFilterUtils.notFileFilter(ff));
            
        } catch (IOException e) {
            String message = "Error copying project to the workspace ../"+workspaceName;
            getLog().error(message, e);
            throw new MojoExecutionException(message);
        }

       
        
    }
    
}
