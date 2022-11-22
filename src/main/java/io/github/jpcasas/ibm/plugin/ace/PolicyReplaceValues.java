package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Mojo(name = "ace-policy-replace", requiresProject = false)
public class PolicyReplaceValues extends AbstractMojo {

    @Parameter(property = "ibm.ace.policy.projectFolder", required = true)
    private File projectFile;

    @Parameter(property = "ibm.ace.policy.propertyFile", required = true)
    private File propertiesFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        boolean error = false;
        String message = null;
        getLog().info("----------------------------------------------------");
        getLog().info("   ACE - MODYFING PROPERTIES IN POLICY            ");
        getLog().info("----------------------------------------------------");
        getLog().info(" ");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        Properties props = new Properties();
        try {
            props.load(new FileReader(propertiesFile));

            Set<Object> keys = props.keySet();
            for (Object object : keys) {

                String key = (String) object;
                String[] split = key.split("\\.");
                if (split.length > 1) {
                    try {
                        String fileName = split[0];
                        File xmlpolicy = new File(projectFile, fileName + ".policyxml");
                        if (xmlpolicy.exists()) {
                            getLog().info(" READING POLICY " + xmlpolicy);
                            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                            Document doc;

                            doc = dBuilder.parse(xmlpolicy);

                            doc.getDocumentElement().normalize();
                            NodeList nList = doc.getElementsByTagName(split[1]);
                            if (nList.getLength() > 0) {
                                Node toModify = nList.item(0);
                                String val = props.getProperty(key);
                                getLog().info(" OVERRITING " + split[1] + " VALUE = " + val);
                                toModify.setTextContent(val);
                            }
                            TransformerFactory transformerFactory = TransformerFactory.newInstance();

                            Transformer transformer = transformerFactory.newTransformer();
                            DOMSource domSource = new DOMSource(doc);

                            StreamResult streamResult = new StreamResult(xmlpolicy);
                            transformer.transform(domSource, streamResult);
                            getLog().info("OVERRIDE OK");
                        }
                    } catch (SAXException e) {
                        error = true;
                        message = e.getMessage();
                    } catch (ParserConfigurationException e) {
                        error = true;
                        message = e.getMessage();
                    } catch (TransformerConfigurationException e) {
                        error = true;
                        message = e.getMessage();
                    } catch (TransformerException e) {
                        error = true;
                        message = e.getMessage();
                    }

                }
            }

        } catch (FileNotFoundException e) {
            error = true;
            message = e.getMessage();
        } catch (IOException e) {
            error = true;
            message = e.getMessage();
        }

        getLog().info("-------------------------END------------------------");
        if (error) {
            throw new MojoExecutionException(message);
        }

    }
}
