package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import io.github.jpcasas.ibm.plugin.utils.Tools;
import io.github.jpcasas.ibm.plugin.utils.XmlUtils;

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
        getLog().info("----------------------------------------------------");
        getLog().info("   ACE - GENERATE PROPERTIES FROM POLICY            ");
        getLog().info("----------------------------------------------------");
        getLog().info(" ");
        File root = new File(System.getProperty("user.dir"));

        getLog().info(" CURRENT FOLDER = " + root);
        File resources = new File(root, "properties");
        resources.mkdirs();

        File[] files = root.listFiles();
        File[] policies = files == null ? new File[0]
                : FileFilterUtils.filter(FileFilterUtils.suffixFileFilter("policyxml"), files);
        Arrays.sort(policies);
        File toWrite = new File(resources, "DEV.properties");
        try (PrintWriter pr = new PrintWriter(toWrite, StandardCharsets.UTF_8.name())) {
            for (File file : policies) {
                String policyName = file.getName().replace(".policyxml", "");
                getLog().info("POLICY NAME = " + policyName);
                Document doc = XmlUtils.newDocumentBuilder().parse(file);
                doc.getDocumentElement().normalize();
                NodeList nList = doc.getElementsByTagName("policy");
                if (nList.getLength() == 0) {
                    getLog().warn("No <policy> element in " + file);
                    continue;
                }
                NodeList items = nList.item(0).getChildNodes();
                for (int i = 0; i < items.getLength(); i++) {
                    Node item = items.item(i);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        pr.println(escape(policyName + "." + item.getNodeName(), true) + "="
                                + escape(item.getTextContent(), false));
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new MojoExecutionException("Unable to parse policy file: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write " + toWrite + ": " + e.getMessage(), e);
        }
        try {
            Tools.copyFiles(toWrite, resources, env, pextension);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to copy " + toWrite + ": " + e.getMessage(), e);
        }
        getLog().info("-------------------------END------------------------");
    }

    /** Escapes a key or value with the same rules as {@link java.util.Properties#store}. */
    static String escape(String s, boolean key) {
        Properties p = new Properties();
        p.setProperty(key ? s : "k", key ? "" : s);
        StringWriter w = new StringWriter();
        try {
            p.store(w, null);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        for (String line : w.toString().split("\\r?\\n")) {
            if (!line.startsWith("#") && !line.isEmpty()) {
                int eq = indexOfSeparator(line);
                return key ? line.substring(0, eq) : line.substring(eq + 1);
            }
        }
        return s;
    }

    private static int indexOfSeparator(String line) {
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '=') {
                return i;
            }
        }
        return line.length();
    }
}
