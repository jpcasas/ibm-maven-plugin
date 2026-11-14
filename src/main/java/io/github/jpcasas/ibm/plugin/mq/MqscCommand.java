package io.github.jpcasas.ibm.plugin.mq;

import java.util.regex.Pattern;

/**
 * Builds the remote command used to run an MQSC script over SSH.
 * <p>
 * The recommended template has a single {@code %s} placeholder for the queue manager
 * ({@code runmqsc %s}); the MQSC script is then sent through stdin and is never parsed
 * by the remote shell. Legacy templates with two placeholders
 * ({@code echo "%s" | runmqsc %s}) are still accepted, but the script is shell-quoted
 * before being interpolated.
 */
public final class MqscCommand {

	/** Characters allowed by IBM MQ in object names (max 48). */
	private static final Pattern QM_NAME = Pattern.compile("[A-Za-z0-9._%/]{1,48}");

	private final String command;
	private final String stdin;

	private MqscCommand(String command, String stdin) {
		this.command = command;
		this.stdin = stdin;
	}

	public String getCommand() {
		return command;
	}

	/** Data to send through stdin, or {@code null}. */
	public String getStdin() {
		return stdin;
	}

	public static String validateQueueManagerName(String qm) {
		if (qm == null || !QM_NAME.matcher(qm).matches()) {
			throw new IllegalArgumentException("Invalid queue manager name '" + qm
					+ "': only A-Z a-z 0-9 . _ % / are allowed (max 48 characters)");
		}
		return qm;
	}

	public static boolean isLegacyTemplate(String template) {
		return countPlaceholders(template) >= 2;
	}

	public static MqscCommand build(String template, String queueManager, String mqsc) {
		validateQueueManagerName(queueManager);
		int placeholders = countPlaceholders(template);
		if (placeholders == 1) {
			return new MqscCommand(String.format(template, queueManager), mqsc);
		}
		if (placeholders == 2) {
			return new MqscCommand(legacy(template, queueManager, mqsc), null);
		}
		throw new IllegalArgumentException("Invalid mq.ssh.exec template '" + template
				+ "': expected one %s (queue manager) or two %s (script, queue manager)");
	}

	/**
	 * Legacy template: the script is placed where the first {@code %s} is. When the
	 * placeholder is wrapped in double quotes (the historic default) those quotes are
	 * replaced by a safely single-quoted string.
	 */
	private static String legacy(String template, String queueManager, String mqsc) {
		String quoted = shellQuote(mqsc);
		String t = template.replace("\"%s\"", "%s").replace("'%s'", "%s");
		if (countPlaceholders(t) != 2) {
			t = template;
		}
		return String.format(t, quoted, queueManager);
	}

	/** POSIX shell single-quoting. */
	public static String shellQuote(String s) {
		return "'" + s.replace("'", "'\\''") + "'";
	}

	private static int countPlaceholders(String template) {
		if (template == null) {
			return 0;
		}
		int count = 0;
		int idx = 0;
		while ((idx = template.indexOf("%s", idx)) >= 0) {
			count++;
			idx += 2;
		}
		return count;
	}
}
