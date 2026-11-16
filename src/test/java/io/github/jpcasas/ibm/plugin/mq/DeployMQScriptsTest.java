package io.github.jpcasas.ibm.plugin.mq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DeployMQScriptsTest {

	private static void write(Path p, String s) throws IOException {
		p.toFile().getParentFile().mkdirs();
		Files.write(p, s.getBytes(StandardCharsets.UTF_8));
	}

	private static DeployMQScriptsSSH mojo(Path dir) {
		DeployMQScriptsSSH m = new DeployMQScriptsSSH();
		MavenProject project = new MavenProject();
		project.setFile(dir.resolve("pom.xml").toFile());
		m.project = project;
		m.pextension = "properties";
		m.environmentProperties = dir.resolve("env.properties").toFile();
		m.mqscriptsInstall = dir.resolve("install").toFile();
		m.mqscriptsUninstall = dir.resolve("uninstall").toFile();
		m.operation = "install";
		m.separator = ",";
		return m;
	}

	@Test
	void rendersScriptsWithEnvironmentAndOwnProperties(@TempDir Path dir) throws Exception {
		write(dir.resolve("env.properties"), "MAXDEPTH=5000\nPREFIX=DEV\n");
		write(dir.resolve("install/02-second.mqs"), "DEFINE QLOCAL('{{PREFIX}}.B') MAXDEPTH({{MAXDEPTH}})");
		write(dir.resolve("install/01-first.mqsc"), "DEFINE QLOCAL('{{PREFIX}}.A') MAXDEPTH({{MAXDEPTH}})");
		write(dir.resolve("config/01-first.properties"), "MAXDEPTH=10\n");

		Map<File, String> scripts = mojo(dir).loadScripts();
		assertEquals(Arrays.asList("01-first.mqsc", "02-second.mqs"),
				new ArrayList<>(scripts.keySet()).stream().map(File::getName).collect(java.util.stream.Collectors.toList()));
		assertEquals(Arrays.asList("DEFINE QLOCAL('DEV.A') MAXDEPTH(10)", "DEFINE QLOCAL('DEV.B') MAXDEPTH(5000)"),
				new ArrayList<>(scripts.values()));
	}

	@Test
	void missingFolderIsEmpty(@TempDir Path dir) throws Exception {
		assertTrue(mojo(dir).loadScripts().isEmpty());
	}

	@Test
	void invalidOperation(@TempDir Path dir) {
		DeployMQScriptsSSH m = mojo(dir);
		m.operation = "drop";
		assertThrows(MojoExecutionException.class, m::loadScripts);
	}

	@Test
	void splitsServers(@TempDir Path dir) {
		DeployMQScriptsSSH m = mojo(dir);
		m.mqServer = "mq1, mq2 ,,mq3";
		assertEquals(Arrays.asList("mq1", "mq2", "mq3"), m.servers());
		m.separator = "|";
		m.mqServer = "mq1|mq2";
		assertEquals(Arrays.asList("mq1", "mq2"), m.servers());
	}
}
