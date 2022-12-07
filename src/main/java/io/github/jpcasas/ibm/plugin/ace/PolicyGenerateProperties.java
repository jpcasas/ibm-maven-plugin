package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import io.github.jpcasas.ibm.plugin.utils.Tools;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Mojo(name = "ace-policy-properties", requiresProject = false)
public class PolicyGenerateProperties extends AbstractMojo {

    @Parameter(defaultValue = "QA,CERT,PRD", property = "ibm.environments", required = true)
	private String env ;

    @Parameter(defaultValue = ".properties", property = "ibm.ace.pextension", required = false)
    private String pextension;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        boolean error = false;
        String message = null;
        getLog().info("----------------------------------------------------");
        getLog().info("   ACE - GENERATE PROPERTIES FROM POLICY            ");
        getLog().info("----------------------------------------------------");
        getLog().info(" ");
        File root = new File(System.getProperty("user.dir"));

        getLog().info(" CURRENT FOLDER = " + root);
        File resources = new File(root, "properties");

        resources.mkdirs();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        File[] policies = FileFilterUtils.filter(FileFilterUtils.suffixFileFilter("policyxml"), root.listFiles());
        String fileName = "DEV.properties";
        PrintWriter pr = null;
        File toWrite = new File(resources, fileName);
        try {
            pr = new PrintWriter(toWrite);

            for (File file : policies) {
                getLog().info(" POLICY = " + file);
                String policyName = file.getName().replace(".policyxml", "");
                getLog().info("POLICY NAME = " + policyName);
                try {

                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    NodeList nList = doc.getElementsByTagName("policy");
                    Node policyNode = nList.item(0);
                    NodeList items = policyNode.getChildNodes();
                    for (int i = 0; i < items.getLength(); i++) {
                        Node item = items.item(i);
                        String nodeName = item.getNodeName();
                        if (!nodeName.equals("#text")) {

                            pr.println(policyName + "." + item.getNodeName() + "=" + item.getTextContent());

                        }

                    }

                } catch (FileNotFoundException e) {
                    getLog().error("CAN'T WRITE FILE");
                    error = true;
                    message = e.getMessage();
                } catch (ParserConfigurationException e) {
                    error = true;
                    message = e.getMessage();
                    getLog().error("CAN'T PARSE POLICY FILE");
                } catch (SAXException e) {
                    error = true;
                    message = e.getMessage();
                    getLog().error("CAN'T SAX Exeption");
                } catch (IOException e) {
                    error = true;
                    message = e.getMessage();
                    getLog().error("IO Exception reading Policy File");
                }

            }
            pr.flush();
            pr.close();
            Tools.copyFiles(toWrite, resources, env, pextension);

        } catch (IOException e1) {
            error = true;
            message = e1.getMessage();
        } finally {
            if (pr != null) {
                pr.close();
            }
        }
        getLog().info("-------------------------END------------------------");
        if (error) {
            throw new MojoExecutionException(message);
        }

    }
}
