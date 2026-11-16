package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import io.github.jpcasas.ibm.plugin.model.MessageAssembly;
import io.github.jpcasas.ibm.plugin.utils.ProcessRunner;
import io.github.jpcasas.ibm.plugin.utils.Tools;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.xml.sax.SAXException;

@Execute(phase = LifecyclePhase.PROCESS_RESOURCES)
@Mojo(name = "ace-create-tests")
public class GenerateTests extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

     @Parameter(defaultValue = "tests", property = "ibm.ace.tests.group", required = true)
    private String groupTests;

    @Parameter(property = "ibm.ace.tests.folder", required = false)
    private File testsProjectParentFolder;

    @Parameter(defaultValue = "ibmint generate tests --recorded-messages %s --output-test-project %s --java-class %s", property = "ibm.ace.tests.cmd", required = true)
    private String cmd;

    @Parameter(defaultValue = "workspace", property = "ibm.ace.build.workspace", required = false)
    private String workspaceName;

   

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("----------------------------------------------------");
        getLog().info("               IBM ACE Generate TESTS                  ");
        getLog().info("----------------------------------------------------");
        getLog().info(" ");

        if (testsProjectParentFolder == null || !testsProjectParentFolder.exists()  || !testsProjectParentFolder.isDirectory()) {
            testsProjectParentFolder = new File(project.getBasedir().getParentFile(), workspaceName);
        }

        if (Tools.isMqsiprofileSet() && Tools.commandExists("ibmint")) {
            Collection<File> massemblys = FileUtils.listFiles(project.getBasedir(), new String[] { "mxml" }, true);
            if (massemblys.size() > 0) {
                MessageAssembly ma;
                try {
                    File testMessage = (File)massemblys.toArray()[0];
                    ma = Tools.parseMessageAssembly(testMessage);
                    String javaName = ma.getApplication() + "_" + ma.getFlow() + "_" + ma.getNode();
                    String fullJavaName = groupTests+"."+javaName;
                    String path = testMessage.getParentFile().getAbsolutePath();
                    File prj = new File(testsProjectParentFolder, project.getArtifactId()+"_Tests");
                    // Arguments are substituted after splitting, so paths with spaces stay a single argument
                    List<String> command = new ArrayList<>();
                    Object[] values = { path, prj.getAbsolutePath(), fullJavaName };
                    int next = 0;
                    for (String token : ProcessRunner.split(cmd)) {
                        if (token.contains("%s") && next < values.length) {
                            command.add(token.replace("%s", String.valueOf(values[next++])));
                        } else {
                            command.add(token);
                        }
                    }
                    getLog().info(String.join(" ", command));
                    ProcessRunner.run(command, getLog(), null, null, true, true);

                } catch (ParserConfigurationException | SAXException e) {
                    throw new MojoExecutionException("Unable to parse message assembly: " + e.getMessage(), e);
                } catch (IOException e) {
                    throw new MojoExecutionException("Unable to generate tests: " + e.getMessage(), e);
                }
            } else {
                getLog().warn("No recorded message assemblies (*.mxml) found, no tests generated");
            }

        }else{
            getLog().error("mqsiprofile not set or ibmint not found, please run in an IBM App Connect console");
            throw new MojoFailureException("No MQSIPROFILE Environment Variables");
        }

    }

    

}
