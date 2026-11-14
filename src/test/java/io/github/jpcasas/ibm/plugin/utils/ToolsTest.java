package io.github.jpcasas.ibm.plugin.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;

import io.github.jpcasas.ibm.plugin.model.MessageAssembly;
import io.github.jpcasas.ibm.plugin.model.eclipse.ProjectDescriptor;

class ToolsTest {

	private static File write(Path dir, String name, String content) throws IOException {
		File f = dir.resolve(name).toFile();
		f.getParentFile().mkdirs();
		Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
		return f;
	}

	@Test
	void templateRendersAndDoesNotMutateInput() throws IOException {
		Properties values = new Properties();
		values.put("QUEUE_NAME", "MY.QUEUE");
		Properties extra = new Properties();
		extra.put("MAXDEPTH", "5000");
		String out = Tools.template("DEFINE QLOCAL('{{QUEUE_NAME}}') MAXDEPTH({{MAXDEPTH}})", values, extra);
		assertEquals("DEFINE QLOCAL('MY.QUEUE') MAXDEPTH(5000)", out);
		assertEquals(1, values.size(), "input properties must not be modified");
	}

	@Test
	void bundledMqTemplatesRender() throws IOException {
		Properties p = new Properties();
		p.put("QUEUE_NAME", "Q1");
		assertTrue(Tools.template(Tools.readResource("mq/create_replace_queue.mustache"), p, null).contains("Q1"));
		assertTrue(Tools.template(Tools.readResource("mq/delete_queue.mustache"), p, null).contains("Q1"));
	}

	@Test
	void readResourceFailsClearly() {
		assertThrows(IOException.class, () -> Tools.readResource("does/not/exist"));
	}

	@Test
	void camelCase() {
		assertEquals("BuildUserId", Tools.camelCase("BUILD_USER_ID"));
		assertEquals("XValue", Tools.camelCase("X_VALUE"));
	}

	@Test
	void base64IsUtf8() {
		assertEquals("dXNlcjpww6Fzcw==", Tools.base64("user:páss"));
	}

	@Test
	void parsesEclipseProject(@TempDir Path dir) throws Exception {
		File f = write(dir, ".project", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<projectDescription>\n"
				+ "  <name>MyApp</name>\n  <comment></comment>\n"
				+ "  <projects>\n    <project>MyLib</project>\n  </projects>\n"
				+ "  <natures>\n    <nature>com.ibm.etools.msgbroker.tooling.applicationNature</nature>\n"
				+ "    <nature>com.ibm.etools.msgbroker.tooling.messageBrokerProjectNature</nature>\n  </natures>\n"
				+ "</projectDescription>\n");
		ProjectDescriptor d = Tools.parseEclipseProject(f);
		assertEquals("MyApp", d.getName());
		assertEquals(Arrays.asList("MyLib"), d.getProjects());
		assertEquals(2, d.getNatures().size());
		assertTrue(d.getNatures().contains("com.ibm.etools.msgbroker.tooling.applicationNature"));
	}

	@Test
	void parsesExampleProjects() throws Exception {
		ProjectDescriptor d = Tools.parseEclipseProject(new File("examples/02-BuildGoals/MyApp/.project"));
		assertEquals("MyApp", d.getName());
		d = Tools.parseEclipseProject(new File("examples/02-BuildGoals/MyPolicy/.project"));
		assertEquals("MyPolicy", d.getName());
	}

	@Test
	void rejectsXxe(@TempDir Path dir) throws Exception {
		File secret = write(dir, "secret.txt", "TOP-SECRET");
		File f = write(dir, ".project", "<?xml version=\"1.0\"?>\n<!DOCTYPE p [<!ENTITY x SYSTEM \""
				+ secret.toURI() + "\">]>\n<projectDescription><name>&x;</name></projectDescription>");
		assertThrows(SAXException.class, () -> Tools.parseEclipseProject(f));
	}

	@Test
	void parsesMessageAssembly(@TempDir Path dir) throws Exception {
		File f = write(dir, "test.mxml", "<?xml version=\"1.0\"?>\n<messageAssembly><target><nodePath>"
				+ "<application>App</application><messageFlow>Flow</messageFlow><node>Node</node>"
				+ "</nodePath></target></messageAssembly>");
		MessageAssembly ma = Tools.parseMessageAssembly(f);
		assertEquals("App", ma.getApplication());
		assertEquals("Flow", ma.getFlow());
		assertEquals("Node", ma.getNode());
	}

	@Test
	void copyFilesPerEnvironment(@TempDir Path dir) throws IOException {
		File model = write(dir, "DEV.properties", "a=1");
		Tools.copyFiles(model, dir.toFile(), "QA, PRD,", ".properties");
		assertTrue(dir.resolve("QA.properties").toFile().exists());
		assertTrue(dir.resolve("PRD.properties").toFile().exists());
	}

	@Test
	void searchFiles(@TempDir Path dir) throws IOException {
		write(dir, "a/one.msgflow", "x");
		write(dir, "b/c/two.msgflow", "x");
		write(dir, "b/three.subflow", "x");
		assertEquals(2, Tools.searchFiles(dir.toFile(), "msgflow").size());
		assertEquals("three.subflow", Tools.searchFile(dir.toFile(), "three.subflow").getName());
		assertEquals(null, Tools.searchFile(dir.toFile(), "missing"));
		assertTrue(Tools.searchFiles(new File(dir.toFile(), "nope"), "msgflow").isEmpty());
	}

	@Test
	void zipRoundTrip(@TempDir Path dir) throws IOException {
		File src = dir.resolve("src").toFile();
		write(src.toPath(), "META-INF/a.txt", "hello");
		write(src.toPath(), "b/c.txt", "world");
		File zip = dir.resolve("out.zip").toFile();
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip))) {
			Tools.zipFolder(src, "", out);
		}
		File dest = dir.resolve("dest").toFile();
		Tools.uncompress(zip, dest);
		assertEquals("hello", new String(Files.readAllBytes(dest.toPath().resolve("META-INF/a.txt")),
				StandardCharsets.UTF_8));
		assertEquals("world", new String(Files.readAllBytes(dest.toPath().resolve("b/c.txt")), StandardCharsets.UTF_8));
	}

	@Test
	void buildClasspathResolvesWildcards(@TempDir Path dir) throws IOException {
		write(dir, "common/classes/IntegrationAPI.jar", "");
		write(dir, "server/classes/junit-4.13.jar", "");
		write(dir, "server/classes/other.jar", "");
		String cp = Tools.buildClasspath("%1$s/common/classes/IntegrationAPI.jar:%1$s/server/classes/junit-*.jar",
				dir.toString());
		assertTrue(cp.contains("IntegrationAPI.jar"));
		assertTrue(cp.contains("junit-4.13.jar"));
		assertFalse(cp.contains("other.jar"));
	}

	@Test
	void portAvailability() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			assertTrue(Tools.isPortAvailable("127.0.0.1", socket.getLocalPort(), 2000));
		}
		int closed;
		try (ServerSocket socket = new ServerSocket(0)) {
			closed = socket.getLocalPort();
		}
		assertFalse(Tools.isPortAvailable("127.0.0.1", closed, 2000));
	}
}
