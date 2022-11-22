package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

import com.ibm.broker.config.appdev.FlowRendererMSGFLOW;
import com.ibm.broker.config.appdev.MessageFlow;
import com.ibm.broker.config.appdev.Node;
import com.ibm.broker.config.appdev.nodes.MQInputNode;
import com.ibm.broker.config.appdev.nodes.MQOutputNode;
import io.github.jpcasas.ibm.plugin.utils.Tools;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author Juan Pablo Casas
 */
@Mojo(name = "ace-mqs", requiresProject = false)
public class GenerateMQS extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    @Parameter(defaultValue = "resources/mq/install", property = "ibm.mq.install.folder", required = false)
    private File install;

    @Parameter(defaultValue = "resources/mq/uninstall", property = "ibm.mq.uninstall.folder", required = false)
    private File uninstall;

    @Parameter(defaultValue = "false", property = "ibm.ace.dryRun", required = false)
    private boolean dryRun;

    @Parameter(defaultValue = "01-install.mqs", property = "ibm.mq.install.fileName", required = false)
    private String installFileName;

    @Parameter(defaultValue = "01-uninstall.mqs", property = "ibm.mq.uninstall.fileName", required = false)
    private String uninstallFileName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("----------------------------------------------------");
        getLog().info("               IBM MQ Generate MQS                  ");
        getLog().info("----------------------------------------------------");
        getLog().info(" ");

        File root = new File(System.getProperty("user.dir"));

        if (!install.exists()) {
            install.mkdirs();
        }
        if (!uninstall.exists()) {
            uninstall.mkdirs();
        }
        File iscript = new File(install, installFileName);
        File uscript = new File(uninstall, uninstallFileName);
        try {

            PrintWriter prin = new PrintWriter(iscript);
            PrintWriter prun = new PrintWriter(uscript);

            String imodel = getFile("mq/create_replace_queue.mustache");
            String umodel = getFile("mq/delete_queue.mustache");

            ArrayList<File> flows = Tools.searchFiles(root, "msgflow");
            ArrayList<File> subf = Tools.searchFiles(root, "subflow");
            flows.addAll(subf);
            String[] queues = searchQueues(flows);
            for (String queue : queues) {
                getLog().info(" SCRIPTING QUEUE " + queue);
                Properties props = new Properties();
                props.put("QUEUE_NAME", queue);
                try {
                    String installationq = Tools.template(imodel, props, null);
                    String unstallq = Tools.template(umodel, props, null);
                    prin.println(installationq);
                    prun.println(unstallq);
                    prin.flush();
                    prun.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            prin.close();
            prun.close();
        } catch (IOException e1) {

            e1.printStackTrace();
        }

    }

    public String getFile(String fileName) throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(fileName)) {

            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        }
    }

    private String[] searchQueues(ArrayList<File> flows) {
        StringBuilder sb = new StringBuilder();
        for (File file : flows) {

            MessageFlow mf;
            try {
                mf = FlowRendererMSGFLOW.read(file);
                Vector<Node> nodes = mf.getNodes();
                nodes.forEach(node -> {
                    if (node instanceof MQInputNode || node instanceof MQOutputNode) {
                        String queueName = (String) node.getPropertyValue("queueName");
                        sb.append(queueName + ",");

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return sb.toString().split(",");

    }

}
