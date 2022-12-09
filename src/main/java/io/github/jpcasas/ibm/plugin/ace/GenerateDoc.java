package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import io.github.jpcasas.ibm.plugin.model.doc.Content;
import io.github.jpcasas.ibm.plugin.model.doc.ProjectFile;
import io.github.jpcasas.ibm.plugin.model.doc.Props;
import io.github.jpcasas.ibm.plugin.utils.Tools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.maven.model.Scm;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author Juan Pablo Casas
 */
@Mojo(name = "ace-doc")
public class GenerateDoc extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("----------------------------------------------------");
        getLog().info("               IBM ACE Generate DOC                  ");
        getLog().info("----------------------------------------------------");
        getLog().info(" ");

        File root = new File(System.getProperty("user.dir"));
        File readme = new File(root, "README.md");
        try {
            PrintWriter meWrite = new PrintWriter(readme);

            String readmeModole = getFile("doc/README.md");

            Properties props = project.getProperties();

            Properties p = new Properties();
            String projectName = project.getArtifactId();
            String groupId = project.getGroupId();
            Scm scm = project.getScm();
            String git = "";
            if(scm!=null)
                git = project.getScm().getDeveloperConnection();
            String version = project.getVersion();

            HashMap<String, List<Content>> map = new HashMap<>();
            buildContext(root, map);

            p.put("PROJECTNAME", projectName);
            
            p.put("GROUPID", groupId);
            p.put("VERSION", version);
            p.put("GITURL", git);
            p.put("properties", getProperties(props));
            p.put("content", transformToList(map));

            String readMeText = Tools.template(readmeModole, p, null);

            meWrite.println(readMeText);
            meWrite.flush();
            meWrite.close();

        } catch (IOException e1) {

            e1.printStackTrace();
        }

    }

    private ArrayList<Content> transformToList(HashMap<String, List<Content>> map) {
        ArrayList<Content> contents = new ArrayList<>();
        Set<String> exts = map.keySet();
        for (String key : exts) {
            Content content = new Content(key);
            List<Content> files = map.get(key);
            for (Content cont : files) {
                content.addAllProjectFile(cont.getFiles());
            }
            contents.add(content);

        }

        return contents;
    }

    private ArrayList<Props> getProperties(Properties props) {
        ArrayList<Props> properties = new ArrayList<>();
        Set<Object> setk = props.keySet();
        for (Object object : setk) {
            String k = (String) object;
            String v = props.getProperty(k);
            properties.add(new Props(k, v));
        }
        return properties;
    }


    private void buildContext(File root, HashMap<String, List<Content>> map) {
        
        Collection<File> c = FileUtils.listFilesAndDirs(root, FileFilterUtils.fileFileFilter(), FileFilterUtils.notFileFilter(FileFilterUtils.prefixFileFilter(".git").or(FileFilterUtils.prefixFileFilter(".svn"))));
        
        for (File file : c) {
                String ext = FilenameUtils.getExtension(file.getName());
                List<Content> values = map.get(ext);
                if (values == null)
                    values = new ArrayList<>();
                String path = file.getAbsolutePath();
                String base = System.getProperty("user.dir");
                String relative = new File(base).toURI().relativize(new File(path).toURI()).getPath();
                Content content = new Content(ext);
                content.addProjectFile(new ProjectFile(relative, file.getName()));
                values.add(content);
                map.put(ext, values);
        }

    }

    public String getFile(String fileName) throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(fileName)) {

            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        }
    }

}
