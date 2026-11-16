package io.github.jpcasas.ibm.plugin.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.junit.jupiter.api.Test;

class ServerCredentialsTest {

	// test fixture, not a real credential
	private static final String SETTINGS_PASSWORD = "dummy";

	private static Settings settings() {
		Server s = new Server();
		s.setId("mq-dev");
		s.setUsername("mqadmin");
		s.setPassword(SETTINGS_PASSWORD);
		s.setPrivateKey("/keys/id_ed25519");
		Settings settings = new Settings();
		settings.addServer(s);
		return settings;
	}

	@Test
	void explicitValuesWithoutServerId() throws Exception {
		ServerCredentials c = ServerCredentials.resolve(null, "u", "p", settings(), null);
		assertEquals("u", c.getUsername());
		assertEquals("p", c.getPassword());
	}

	@Test
	void readsServerFromSettings() throws Exception {
		ServerCredentials c = ServerCredentials.resolve("mq-dev", null, "", settings(), null);
		assertEquals("mqadmin", c.getUsername());
		assertEquals(SETTINGS_PASSWORD, c.getPassword());
		assertEquals("/keys/id_ed25519", c.getPrivateKey());
	}

	@Test
	void explicitValuesWin() throws Exception {
		ServerCredentials c = ServerCredentials.resolve("mq-dev", "other", null, settings(), null);
		assertEquals("other", c.getUsername());
		assertEquals(SETTINGS_PASSWORD, c.getPassword());
	}

	@Test
	void unknownServerFails() {
		assertThrows(MojoExecutionException.class,
				() -> ServerCredentials.resolve("nope", null, null, settings(), null));
	}

	@Test
	void blankUser() {
		assertFalse(ServerCredentials.of(" ", null).hasUsername());
	}
}
