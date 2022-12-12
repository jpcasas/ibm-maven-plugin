package io.github.jpcasas.ibm.plugin.ace.gnative;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import com.ibm.broker.MessageBrokerAPIException;
import com.ibm.broker.config.appdev.FlowRendererBAR;

import io.github.jpcasas.ibm.plugin.utils.Tools;

@Execute(phase = LifecyclePhase.PACKAGE)
@Mojo(name = "ace-verify")
public class RunTestProjectGoal extends AbstractMojo {

    @Parameter(defaultValue = "%1$s/common/classes/Integration*.jar:%1$s/common/classes/IntegrationAPI.jar:%1$s/server/classes/junit-*.jar", property = "ibm.ace.test.jars", required = false)
    private String classpath;

    @Parameter(defaultValue = "workspace", property = "ibm.ace.build.workspace", required = false)
    private String workspaceName;

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("----------------------------------------------------");
        getLog().info("          IBM ACE Package Tests                     ");
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

            String fullClasspath = buildClasspath(classpath, Tools.getInstallationFolder());

            try {
                createJar(testProjectFolder, projectTestName, fullClasspath);
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

    private String buildClasspath(String classp, String installationFolder) {
        String replace = String.format(classp, installationFolder);
        String paths[] = replace.split(":");
        HashSet<String> set = new HashSet<>();
        for (String path : paths) {
            File p = new File(path);
            if (p.exists()) {
                set.add(path);
            } else {

                String[] folderWithWild = getFolderWithWild(path);
                if (folderWithWild != null && folderWithWild.length == 2) {
                    Collection<File> files = FileUtils.listFiles(new File(folderWithWild[0]), new WildcardFileFilter(folderWithWild[1]), null);
                    set.addAll(files.stream().map( file -> file.getAbsolutePath()).collect(Collectors.toList()));
                }

            }

        }
        return set.stream().collect(Collectors.joining(":"));
    }

   
    private String[] getFolderWithWild(String path) {
        int lastIndex = path.lastIndexOf(File.separator);
        if (lastIndex > 0) {
            return new String[] { path.substring(0, lastIndex), path.substring(lastIndex + 1) };
        }
        return null;
    }

    private void createJar(File testProjectFolder, String projectTestName, String cp) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("ant/build.xml");
        File buildFile = File.createTempFile("build", ".xml");
        FileOutputStream fos = new FileOutputStream(buildFile);
        copy(is, fos);
        fos.close();
        File build = new File(testProjectFolder, "build");
        build.mkdirs();
        Project p = new Project();
        p.setProperty("src", new File(testProjectFolder, "src").getAbsolutePath());
        p.setProperty("build", build.getAbsolutePath());
        p.setProperty("dist", new File(testProjectFolder, "dist").getAbsolutePath());
        p.setProperty("cp", cp);
        p.setProperty("projectLocation", testProjectFolder.getAbsolutePath());
        p.setProperty("jarName", projectTestName);
        p.setUserProperty("ant.file", buildFile.getAbsolutePath());
        p.init();
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        p.addReference("ant.projectHelper", helper);
        helper.parse(p, buildFile);
        p.executeTarget(p.getDefaultTarget());

    }

    void copy(InputStream source, OutputStream target) throws IOException {
        byte[] buf = new byte[8192];
        int length;
        while ((length = source.read(buf)) != -1) {
            target.write(buf, 0, length);
        }
    }

}

// jar project test project
/*
 * $JAVA_HOME/../bin/javac -classpath
 * "$MQSI_BASE_FILEPATH/common/classes/IntegrationTest.jar:$MQSI_BASE_FILEPATH/common/classes/IntegrationAPI.jar:$MQSI_BASE_FILEPATH/server/classes/junit-platform-console-standalone-1.7.0.jar:."
 * src/main/java/tests/IBMAceAppDemo_MyFLow_Pass_through.java
 * $JAVA_HOME/../bin/jar
 * 
 */