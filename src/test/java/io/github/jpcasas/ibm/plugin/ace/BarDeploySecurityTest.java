package io.github.jpcasas.ibm.plugin.ace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class BarDeploySecurityTest {

	private static void set(Object target, String field, Object value) throws ReflectiveOperationException {
		Field f = target.getClass().getDeclaredField(field);
		f.setAccessible(true);
		f.set(target, value);
	}

	private static void reply(HttpExchange ex, int status, String body) throws IOException {
		byte[] b = body.getBytes(StandardCharsets.UTF_8);
		ex.getResponseHeaders().add("Content-Type", "application/json");
		ex.sendResponseHeaders(status, b.length == 0 ? -1 : b.length);
		if (b.length > 0) {
			try (OutputStream out = ex.getResponseBody()) {
				out.write(b);
			}
		}
		ex.close();
	}

	@Test
	void doesNotFollowRedirectsWithCredentials(@TempDir Path dir) throws Exception {
		AtomicInteger stolen = new AtomicInteger();
		AtomicReference<String> authSeen = new AtomicReference<>();
		HttpServer attacker = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		attacker.createContext("/", ex -> {
			stolen.incrementAndGet();
			reply(ex, 200, "{}");
		});
		HttpServer ace = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		ace.createContext("/apiv2/", ex -> {
			authSeen.set(ex.getRequestHeaders().getFirst("Authorization"));
			// both the GET (type detection) and the POST (deploy) redirect to another host
			ex.getResponseHeaders().add("Location",
					"http://127.0.0.1:" + attacker.getAddress().getPort() + "/steal");
			reply(ex, ex.getRequestURI().getPath().endsWith("/deploy") ? 307 : 302, "");
		});
		attacker.start();
		ace.start();
		try {
			File bar = dir.resolve("app.bar").toFile();
			Files.write(bar.toPath(), new byte[] { 1, 2, 3 });
			BarDeploy mojo = new BarDeploy();
			set(mojo, "host", "127.0.0.1");
			set(mojo, "port", ace.getAddress().getPort());
			set(mojo, "timeout", 10);
			set(mojo, "user", "admin");
			set(mojo, "password", "secret");
			set(mojo, "barFileName", bar);

			assertThrows(MojoFailureException.class, mojo::execute);
			assertTrue(authSeen.get().startsWith("Basic "), "credentials sent to the configured server");
			assertEquals(0, stolen.get(), "redirect must not be followed");
		} finally {
			ace.stop(0);
			attacker.stop(0);
		}
	}

	@Test
	void integrationServerIsASinglePathSegment() {
		assertEquals("..%2Fadmin%2Fshutdown%3Fx%3D1", BarDeploy.pathSegment("../admin/shutdown?x=1"));
		assertEquals("my%20server", BarDeploy.pathSegment("my server"));
		assertEquals("IS1", BarDeploy.pathSegment("IS1"));
	}

	@Test
	void policyNamesCannotContainPaths() {
		assertTrue(PolicyReplaceValues.isSafePolicyName("UDPPolicy"));
		assertFalse(PolicyReplaceValues.isSafePolicyName("../../etc/passwd"));
		assertFalse(PolicyReplaceValues.isSafePolicyName("dir\\x"));
		assertFalse(PolicyReplaceValues.isSafePolicyName(""));
	}
}
