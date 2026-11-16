package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import com.ibm.broker.config.appdev.FlowRendererMSGFLOW;
import com.ibm.broker.config.appdev.MessageFlow;
import com.ibm.broker.config.appdev.Node;
import com.ibm.broker.config.appdev.nodes.MQInputNode;
import com.ibm.broker.config.appdev.nodes.MQOutputNode;
import io.github.jpcasas.ibm.plugin.utils.Tools;

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

        ArrayList<File> flows = Tools.searchFiles(root, "msgflow");
        flows.addAll(Tools.searchFiles(root, "subflow"));
        Set<String> queues = searchQueues(flows);
        if (queues.isEmpty()) {
            getLog().warn("No MQInput/MQOutput nodes found, no MQ scripts generated");
            return;
        }
        if (dryRun) {
            queues.forEach(q -> getLog().info(" QUEUE " + q));
            return;
        }

        install.mkdirs();
        uninstall.mkdirs();
        File iscript = new File(install, installFileName);
        File uscript = new File(uninstall, uninstallFileName);
        try (PrintWriter prin = new PrintWriter(iscript, StandardCharsets.UTF_8.name());
                PrintWriter prun = new PrintWriter(uscript, StandardCharsets.UTF_8.name())) {

            String imodel = Tools.readResource("mq/create_replace_queue.mustache");
            String umodel = Tools.readResource("mq/delete_queue.mustache");

            for (String queue : queues) {
                getLog().info(" SCRIPTING QUEUE " + queue);
                Properties props = new Properties();
                props.put("QUEUE_NAME", queue);
                prin.println(Tools.template(imodel, props, null));
                prun.println(Tools.template(umodel, props, null));
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write MQ scripts: " + e.getMessage(), e);
        }
        getLog().info("MQ scripts written to " + iscript + " and " + uscript);
    }

    private Set<String> searchQueues(List<File> flows) throws MojoExecutionException {
        Set<String> queues = new TreeSet<>();
        for (File file : flows) {
            try {
                MessageFlow mf = FlowRendererMSGFLOW.read(file);
                for (Node node : mf.getNodes()) {
                    if (node instanceof MQInputNode || node instanceof MQOutputNode) {
                        Object queueName = node.getPropertyValue("queueName");
                        if (queueName != null && !queueName.toString().trim().isEmpty()) {
                            queues.add(queueName.toString().trim());
                        }
                    }
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to read flow " + file + ": " + e.getMessage(), e);
            }
        }
        return queues;
    }

}
