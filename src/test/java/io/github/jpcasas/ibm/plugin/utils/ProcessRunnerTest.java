package io.github.jpcasas.ibm.plugin.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.jupiter.api.Test;

class ProcessRunnerTest {

	private static final boolean UNIX = File.separatorChar == '/';

	@Test
	void splitsHonouringQuotes() {
		assertEquals(Arrays.asList("ibmint", "generate", "--output", "/my dir/x", "a'b"),
				ProcessRunner.split("ibmint generate --output \"/my dir/x\" \"a'b\""));
		assertThrows(IllegalArgumentException.class, () -> ProcessRunner.split("  "));
	}

	@Test
	void capturesStdoutAndStderr() throws Exception {
		assumeTrue(UNIX);
		String out = ProcessRunner.run(Arrays.asList("sh", "-c", "echo out; echo err 1>&2"), new SystemStreamLog(),
				null, null, false, true);
		assertTrue(out.contains("out\n"));
		assertTrue(out.contains("err\n"));
	}

	@Test
	void doesNotDeadlockOnLargeStderr() throws Exception {
		assumeTrue(UNIX);
		// 1 MB on stderr would fill the pipe and hang the old implementation
		String out = ProcessRunner.run(
				Arrays.asList("sh", "-c", "head -c 1048576 /dev/zero | tr '\\0' x 1>&2; echo done"),
				new SystemStreamLog(), null, null, false, true);
		assertTrue(out.endsWith("done\n"));
	}

	@Test
	void failsOnNonZeroExit() {
		assumeTrue(UNIX);
		assertThrows(MojoExecutionException.class, () -> ProcessRunner.run(Arrays.asList("sh", "-c", "exit 4"),
				new SystemStreamLog(), null, null, false, true));
	}

	@Test
	void usesWorkingDirAndEnv() throws Exception {
		assumeTrue(UNIX);
		File tmp = new File(System.getProperty("java.io.tmpdir")).getCanonicalFile();
		String out = ProcessRunner.run(Arrays.asList("sh", "-c", "pwd; echo $FOO"), new SystemStreamLog(), tmp,
				Collections.singletonMap("FOO", "bar"), false, true);
		assertEquals(tmp.getPath() + "\nbar\n", out);
	}

	@Test
	void argumentsAreNotInterpretedByAShell() throws Exception {
		assumeTrue(UNIX);
		String out = ProcessRunner.run(Arrays.asList("echo", "$(id)", ";", "ls"), new SystemStreamLog(), null, null,
				false, true);
		assertEquals("$(id) ; ls\n", out);
	}
}
