package io.github.jpcasas.ibm.plugin.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Builds an {@link SSLContext} from optional trust/key stores without touching the global
 * {@code javax.net.ssl.*} system properties (which would leak into the whole Maven JVM).
 */
public final class SslContexts {

	private SslContexts() {
	}

	/**
	 * @return a context using the given stores; JVM defaults are used for a store whose path is blank
	 */
	public static SSLContext create(String truststore, String truststorePassword, String keystore,
			String keystorePassword) throws GeneralSecurityException, IOException {
		TrustManagerFactory tmf = null;
		if (notBlank(truststore)) {
			tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(load(truststore, truststorePassword));
		}
		KeyManagerFactory kmf = null;
		if (notBlank(keystore)) {
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(load(keystore, keystorePassword), chars(keystorePassword));
		}
		SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(kmf == null ? null : kmf.getKeyManagers(), tmf == null ? null : tmf.getTrustManagers(), null);
		return ctx;
	}

	public static boolean notBlank(String s) {
		return s != null && !s.trim().isEmpty();
	}

	private static char[] chars(String s) {
		return s == null ? null : s.toCharArray();
	}

	private static KeyStore load(String path, String password) throws GeneralSecurityException, IOException {
		String lower = path.toLowerCase();
		String type = lower.endsWith(".jks") ? "JKS"
				: (lower.endsWith(".p12") || lower.endsWith(".pfx")) ? "PKCS12" : KeyStore.getDefaultType();
		KeyStore ks = KeyStore.getInstance(type);
		try (InputStream in = Files.newInputStream(Paths.get(SshClient.expandHome(path)))) {
			ks.load(in, chars(password));
		}
		return ks;
	}
}
