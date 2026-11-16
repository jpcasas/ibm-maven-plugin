package io.github.jpcasas.ibm.plugin.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.tools.ant.types.Commandline;

/**
 * Runs local processes safely: arguments are passed as a list (no shell), stderr is
 * merged into stdout so the child can never block on a full pipe, and the output is
 * streamed to the Maven log while it is produced.
 */
public final class ProcessRunner {

	private ProcessRunner() {
	}

	/**
	 * Splits a command line into arguments honouring single and double quotes, so paths
	 * with spaces can be quoted. No shell expansion is performed.
	 */
	public static List<String> split(String commandLine) {
		if (commandLine == null || commandLine.trim().isEmpty()) {
			throw new IllegalArgumentException("Empty command line");
		}
		return Arrays.asList(Commandline.translateCommandline(commandLine));
	}

	public static String run(String commandLine, Log log, File workingDir) throws IOException, MojoExecutionException {
		return run(split(commandLine), log, workingDir, null, true, true);
	}

	/**
	 * @param command        program and arguments
	 * @param log            Maven log
	 * @param workingDir     working directory or {@code null} for the current one
	 * @param extraEnv       variables added to the inherited environment, may be {@code null}
	 * @param print          log each output line at info level
	 * @param failOnError    throw {@link MojoExecutionException} when the exit code is not 0
	 * @return the combined stdout/stderr output
	 */
	public static String run(List<String> command, Log log, File workingDir, Map<String, String> extraEnv,
			boolean print, boolean failOnError) throws IOException, MojoExecutionException {
		ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true);
		if (workingDir != null) {
			pb.directory(workingDir);
		}
		if (extraEnv != null) {
			pb.environment().putAll(extraEnv);
		}
		log.debug("Executing: " + command);
		Process p = pb.start();
		p.getOutputStream().close();

		StringBuilder out = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(p.getInputStream(), Charset.defaultCharset()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (print) {
					log.info(line);
				}
				out.append(line).append('\n');
			}
		}

		int exit;
		try {
			exit = p.waitFor();
		} catch (InterruptedException e) {
			p.destroyForcibly();
			Thread.currentThread().interrupt();
			throw new MojoExecutionException("Interrupted while running " + command.get(0), e);
		}
		if (failOnError && exit != 0) {
			throw new MojoExecutionException(
					String.format("Command '%s' failed with exit code %d", String.join(" ", command), exit));
		}
		return out.toString();
	}
}
