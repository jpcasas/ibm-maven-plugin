package io.github.jpcasas.ibm.plugin.ace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class PolicyAndBarPropertiesTest {

	@Test
	void policyValuesSurvivePropertiesRoundTrip() throws IOException {
		String[] values = { "C:\\certs\\key.jks", "a=b:c", "  leading", "#notacomment", "ñandú" };
		for (String v : values) {
			String line = PolicyGenerateProperties.escape("MyPolicy.key name", true) + "="
					+ PolicyGenerateProperties.escape(v, false);
			Properties p = new Properties();
			p.load(new StringReader(line));
			assertEquals(v, p.getProperty("MyPolicy.key name"), line);
		}
	}

	@Test
	void extractsDeploymentDescriptorBlock() {
		String[] output = { "BAR file: x.bar", "  Deployment descriptor:", "    MyFlow#Input.queueName = IN",
				"    MyFlow#additionalInstances = 0", "  Other section", "    ignored = 1" };
		assertEquals(Arrays.asList("MyFlow#Input.queueName = IN", "MyFlow#additionalInstances = 0"),
				BarReadProperties.extractDeploymentDescriptor(output));
	}

	@Test
	void keywordString() {
		HashMap<String, String> info = new LinkedHashMap<>();
		info.put("BuildUser", "jenkins");
		String s = SetKeyWords.getKeyWordsString(info, "1.2.3");
		assertTrue(s.startsWith("$MQSI_VERSION=1.2.3 MQSI$ "));
		assertTrue(s.contains("$MQSI BuildUser=jenkins MQSI$"));
	}
}
