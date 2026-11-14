package io.github.jpcasas.ibm.plugin.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import io.github.jpcasas.ibm.plugin.model.MessageAssembly;
import io.github.jpcasas.ibm.plugin.model.eclipse.ProjectDescriptor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * File, template and project-descriptor helpers shared by the goals.
 * Process execution lives in {@link ProcessRunner} and SSH in {@link SshClient}.
 */
public final class Tools {

	private Tools() {
	}

	public static File searchFile(File dir, String file) {
		if (dir == null) {
			dir = new File("").getAbsoluteFile();
		}
		if (file != null && dir.isDirectory()) {
			Collection<File> results = FileUtils.listFiles(dir, FileFilterUtils.nameFileFilter(file),
					TrueFileFilter.INSTANCE);
			Iterator<File> it = results.iterator();
			if (it.hasNext()) {
				return it.next();
			}
		}
		return null;
	}

	public static ArrayList<File> searchFiles(File dir, String suffix) {
		ArrayList<File> files = new ArrayList<>();
		if (dir != null && suffix != null && dir.isDirectory()) {
			files.addAll(FileUtils.listFiles(dir, FileFilterUtils.suffixFileFilter(suffix), TrueFileFilter.INSTANCE));
		}
		return files;
	}

	public static void copyFiles(File pmodel, File resources, String environments, String pextension)
			throws IOException {
		for (String env : environments.split(",")) {
			if (!env.trim().isEmpty()) {
				FileUtils.copyFile(pmodel, new File(resources, env.trim() + pextension));
			}
		}
	}

	/** Reads a classpath resource as UTF-8 text. */
	public static String readResource(String name) throws IOException {
		try (InputStream in = Tools.class.getClassLoader().getResourceAsStream(name)) {
			if (in == null) {
				throw new IOException("Resource not found in plugin: " + name);
			}
			return IOUtils.toString(in, StandardCharsets.UTF_8);
		}
	}

	public static String getMQSC(String strTemplate, Properties queuep) throws IOException {
		return template(strTemplate, queuep, null);
	}

	/**
	 * Renders a Mustache template. The given properties are not modified.
	 */
	public static String template(String strTemplate, Properties values, Properties additional) throws IOException {
		if (strTemplate == null) {
			return null;
		}
		Properties scope = new Properties();
		if (values != null) {
			scope.putAll(values);
		}
		if (additional != null) {
			scope.putAll(additional);
		}
		Mustache mustache = new DefaultMustacheFactory().compile(new StringReader(strTemplate), "template");
		StringWriter out = new StringWriter();
		mustache.execute(out, scope).flush();
		return out.toString();
	}

	public static void uncompress(File file, File outputDirectory) throws IOException {
		try (net.lingala.zip4j.ZipFile zfile = new net.lingala.zip4j.ZipFile(file.getAbsolutePath())) {
			zfile.extractAll(outputDirectory.getAbsolutePath());
		}
	}

	public static void zipFolder(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			if (fileName.endsWith("/")) {
				zipOut.putNextEntry(new ZipEntry(fileName));
				zipOut.closeEntry();
			}
			File[] children = fileToZip.listFiles();
			if (children != null) {
				for (File childFile : children) {
					String pre = fileName.isEmpty() ? "" : fileName + "/";
					zipFolder(childFile, pre + childFile.getName(), zipOut);
				}
			}
			return;
		}
		zipOut.putNextEntry(new ZipEntry(fileName));
		Files.copy(fileToZip.toPath(), zipOut);
		zipOut.closeEntry();
	}

	public static String camelCase(String chain) {
		String lowerCase = chain.toLowerCase();
		String[] tokens = lowerCase.split("_");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].length() > 1) {
				sb.append(String.format("%s%s", ("" + tokens[i].charAt(0)).toUpperCase(), tokens[i].substring(1)));
			} else {
				sb.append(tokens[i].toUpperCase());
			}

		}
		return sb.toString();
	}

	public static String base64(String string) {
		return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
	}

	public static byte[] toByteArray(File bar) throws IOException {
		return Files.readAllBytes(bar.toPath());
	}

	public static ProjectDescriptor parseEclipseProject(File projectf)
			throws ParserConfigurationException, SAXException, IOException {
		ProjectDescriptor prodesc = new ProjectDescriptor();
		Document doc = XmlUtils.newDocumentBuilder().parse(projectf);
		doc.getDocumentElement().normalize();

		NodeList namelist = doc.getElementsByTagName("name");
		if (namelist.getLength() > 0) {
			prodesc.setName(namelist.item(0).getTextContent().trim());
		}
		prodesc.setProjects(texts(doc.getElementsByTagName("project")));
		prodesc.setNatures(texts(doc.getElementsByTagName("nature")));
		return prodesc;
	}

	private static ArrayList<String> texts(NodeList nodes) {
		ArrayList<String> list = new ArrayList<>();
		for (int i = 0; i < nodes.getLength(); i++) {
			String text = nodes.item(i).getTextContent();
			if (text != null && !text.trim().isEmpty()) {
				list.add(text.trim());
			}
		}
		return list;
	}

	/** @return {@code true} if a TCP connection to host:port can be opened within the timeout (ms) */
	public static boolean isPortAvailable(String host, int port, int timeout) {
		try (Socket s = new Socket()) {
			s.connect(new InetSocketAddress(host, port), timeout);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static boolean isMqsiprofileSet() {
		return getInstallationFolder() != null;
	}

	public static String getInstallationFolder() {
		return System.getenv("MQSI_BASE_FILEPATH");
	}

	public static boolean commandExists(String command) {
		String install = getInstallationFolder();
		if (install == null) {
			return false;
		}
		File bin = new File(new File(install, "server"), "bin");
		return new File(bin, command).exists() || new File(bin, command + ".exe").exists()
				|| new File(bin, command + ".cmd").exists() || new File(bin, command + ".bat").exists();
	}

	public static MessageAssembly parseMessageAssembly(File file)
			throws ParserConfigurationException, SAXException, IOException {
		MessageAssembly messageAssembly = new MessageAssembly();
		Document doc = XmlUtils.newDocumentBuilder().parse(file);
		doc.getDocumentElement().normalize();
		NodeList namelist = doc.getElementsByTagName("target");
		if (namelist.getLength() > 0) {
			NodeList targetChilds = namelist.item(0).getChildNodes();
			for (int i = 0; i < targetChilds.getLength(); i++) {
				Node node = targetChilds.item(i);
				if (node.getNodeName().equals("nodePath")) {
					NodeList childs = node.getChildNodes();
					for (int j = 0; j < childs.getLength(); j++) {
						messageAssembly.set(childs.item(j).getNodeName(), childs.item(j).getTextContent());
					}
				}
			}
		}
		return messageAssembly;
	}

	public static void createJar(File testProjectFolder, String projectTestName, String cp) throws IOException {
		File buildFile = File.createTempFile("build", ".xml");
		try {
			try (InputStream is = Tools.class.getClassLoader().getResourceAsStream("ant/build.xml");
					OutputStream fos = new FileOutputStream(buildFile)) {
				if (is == null) {
					throw new IOException("ant/build.xml not found in plugin");
				}
				IOUtils.copy(is, fos);
			}
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
		} finally {
			Files.deleteIfExists(buildFile.toPath());
		}
	}

	/**
	 * Expands a ':'-separated classpath template ({@code %1$s} = ACE installation folder),
	 * resolving wildcards in the file name part (e.g. {@code junit-*.jar}).
	 */
	public static String buildClasspath(String classp, String installationFolder) {
		String replace = String.format(classp, installationFolder);
		HashSet<String> set = new HashSet<>();
		for (String path : replace.split(":")) {
			if (new File(path).exists()) {
				set.add(path);
			} else {
				String[] folderWithWild = getFolderWithWild(path);
				File folder = folderWithWild == null ? null : new File(folderWithWild[0]);
				if (folder != null && folder.isDirectory()) {
					Collection<File> files = FileUtils.listFiles(folder,
							WildcardFileFilter.builder().setWildcards(folderWithWild[1]).get(), null);
					set.addAll(files.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
				}
			}
		}
		return String.join(":", set);
	}

	public static String[] getFolderWithWild(String path) {
		int lastIndex = Math.max(path.lastIndexOf('/'), path.lastIndexOf(File.separatorChar));
		if (lastIndex > 0) {
			return new String[] { path.substring(0, lastIndex), path.substring(lastIndex + 1) };
		}
		return null;
	}
}
