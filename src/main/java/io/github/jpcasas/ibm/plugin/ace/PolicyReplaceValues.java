package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.github.jpcasas.ibm.plugin.utils.XmlUtils;

@Mojo(name = "ace-policy-replace", requiresProject = false)
public class PolicyReplaceValues extends AbstractMojo {

    @Parameter(property = "ibm.ace.policy.projectFolder", required = true)
    private File projectFile;

    @Parameter(property = "ibm.ace.policy.propertyFile", required = true)
    private File propertiesFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("----------------------------------------------------");
        getLog().info("   ACE - MODIFYING PROPERTIES IN POLICY             ");
        getLog().info("----------------------------------------------------");
        getLog().info(" ");

        Properties props = new Properties();
        try (Reader in = Files.newBufferedReader(propertiesFile.toPath(), StandardCharsets.UTF_8)) {
            props.load(in);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read " + propertiesFile + ": " + e.getMessage(), e);
        }

        // group the overrides by policy file so each file is parsed and written once
        Map<String, Map<String, String>> byPolicy = new TreeMap<>();
        for (String key : props.stringPropertyNames()) {
            int dot = key.indexOf('.');
            if (dot > 0 && dot < key.length() - 1) {
                byPolicy.computeIfAbsent(key.substring(0, dot), k -> new TreeMap<>()).put(key.substring(dot + 1),
                        props.getProperty(key));
            }
        }

        for (Map.Entry<String, Map<String, String>> policy : byPolicy.entrySet()) {
            File xmlpolicy = new File(projectFile, policy.getKey() + ".policyxml");
            if (!xmlpolicy.exists()) {
                getLog().debug("Policy file not found, skipping: " + xmlpolicy);
                continue;
            }
            getLog().info(" READING POLICY " + xmlpolicy);
            try {
                Document doc = XmlUtils.newDocumentBuilder().parse(xmlpolicy);
                doc.getDocumentElement().normalize();
                boolean changed = false;
                for (Map.Entry<String, String> value : policy.getValue().entrySet()) {
                    NodeList nList = doc.getElementsByTagName(value.getKey());
                    if (nList.getLength() > 0) {
                        getLog().info(" OVERWRITING " + value.getKey() + " VALUE = " + value.getValue());
                        nList.item(0).setTextContent(value.getValue());
                        changed = true;
                    } else {
                        getLog().warn(" Property " + value.getKey() + " not found in " + xmlpolicy.getName());
                    }
                }
                if (changed) {
                    Transformer transformer = XmlUtils.newTransformerFactory().newTransformer();
                    transformer.transform(new DOMSource(doc), new StreamResult(xmlpolicy));
                    getLog().info("OVERRIDE OK");
                }
            } catch (SAXException | ParserConfigurationException | TransformerException | IOException e) {
                throw new MojoExecutionException("Unable to update " + xmlpolicy + ": " + e.getMessage(), e);
            }
        }

        getLog().info("-------------------------END------------------------");
    }
}
