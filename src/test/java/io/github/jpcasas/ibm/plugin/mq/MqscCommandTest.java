package io.github.jpcasas.ibm.plugin.mq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MqscCommandTest {

	@Test
	void singlePlaceholderSendsScriptThroughStdin() {
		MqscCommand cmd = MqscCommand.build("runmqsc %s", "QM1", "DEFINE QLOCAL('A') REPLACE");
		assertEquals("runmqsc QM1", cmd.getCommand());
		assertEquals("DEFINE QLOCAL('A') REPLACE", cmd.getStdin());
	}

	@Test
	void legacyTemplateQuotesTheScript() {
		MqscCommand cmd = MqscCommand.build("echo \"%s\" | runmqsc %s", "QM1", "DEFINE QLOCAL('A') $(rm -rf /)");
		assertEquals("echo 'DEFINE QLOCAL('\\''A'\\'') $(rm -rf /)' | runmqsc QM1", cmd.getCommand());
		assertNull(cmd.getStdin());
	}

	@Test
	void detectsLegacyTemplates() {
		assertTrue(MqscCommand.isLegacyTemplate("echo \"%s\" | runmqsc %s"));
		assertFalse(MqscCommand.isLegacyTemplate("runmqsc %s"));
	}

	@Test
	void rejectsUnsafeQueueManagerNames() {
		assertThrows(IllegalArgumentException.class, () -> MqscCommand.build("runmqsc %s", "QM1; reboot", "x"));
		assertThrows(IllegalArgumentException.class, () -> MqscCommand.validateQueueManagerName(""));
		assertThrows(IllegalArgumentException.class, () -> MqscCommand.validateQueueManagerName(null));
		assertEquals("QM.TEST_1", MqscCommand.validateQueueManagerName("QM.TEST_1"));
	}

	@Test
	void rejectsTemplatesWithoutPlaceholder() {
		assertThrows(IllegalArgumentException.class, () -> MqscCommand.build("runmqsc", "QM1", "x"));
	}

	@Test
	void shellQuote() {
		assertEquals("'it'\\''s'", MqscCommand.shellQuote("it's"));
	}
}
