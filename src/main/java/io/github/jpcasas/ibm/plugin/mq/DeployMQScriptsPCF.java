package io.github.jpcasas.ibm.plugin.mq;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.MQCFST;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;

import io.github.jpcasas.ibm.plugin.utils.ServerCredentials;
import io.github.jpcasas.ibm.plugin.utils.SslContexts;
import io.github.jpcasas.ibm.plugin.utils.Tools;

/**
 * Runs MQSC scripts through a client connection (PCF escape command).
 */
@Mojo(name = "mq-deploy-pcf", requiresProject = false, threadSafe = true)
public class DeployMQScriptsPCF extends DeployMQScripts {

	@Parameter(defaultValue = "1414", property = "mq.port", required = false)
	private int mqPort;

	@Parameter(property = "mq.channel", required = true)
	private String channel;

	/** Keystore (JKS/PKCS12) used as key and trust store for TLS channels. */
	@Parameter(property = "mq.keystore", required = false)
	private String keystore;

	@Parameter(property = "mq.keystorePassword", required = false)
	private String keyStorePassword;

	@Parameter(property = "mq.cipherSuite", required = false)
	private String sslCipherSuite;

	@Parameter(defaultValue = "1000", property = "mq.timeout", required = true)
	private int timeout;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("----------------------------------------------------");
		getLog().info("        IBM MQ - Deploy MQ Scripts (PCF)            ");
		getLog().info("----------------------------------------------------");

		MqscCommand.validateQueueManagerName(queueManagerName);
		Map<File, String> scripts = loadScripts();
		if (scripts.isEmpty()) {
			return;
		}
		List<Hashtable<String, Object>> connections = getConnectionProperties();
		List<String> errors = new ArrayList<>();

		for (Hashtable<String, Object> conn : connections) {
			MQQueueManager qm = null;
			PCFMessageAgent agent = null;
			try {
				qm = new MQQueueManager(queueManagerName, conn);
				agent = new PCFImplementation(qm);
				for (Map.Entry<File, String> script : scripts.entrySet()) {
					getLog().info("Running " + script.getKey().getName() + " on " + conn.get(MQConstants.HOST_NAME_PROPERTY));
					try {
						for (PCFMessage response : sendEscape(agent, script.getValue())) {
							Enumeration<?> en = response.getParameters();
							while (en.hasMoreElements()) {
								Object param = en.nextElement();
								if (param instanceof MQCFST) {
									getLog().info(((MQCFST) param).getString());
								}
							}
						}
					} catch (MQDataException | IOException e) {
						getLog().error("MQ script " + script.getKey() + " failed", e);
						errors.add("MQ script " + script.getKey() + " failed: " + e.getMessage());
					}
				}
			} catch (MQException | MQDataException e) {
				getLog().error("Unable to connect to " + conn.get(MQConstants.HOST_NAME_PROPERTY), e);
				errors.add("Connection to " + conn.get(MQConstants.HOST_NAME_PROPERTY) + " failed: " + e.getMessage());
			} finally {
				if (agent != null) {
					try {
						agent.disconnect();
					} catch (MQDataException e) {
						getLog().debug("Error disconnecting PCF agent", e);
					}
				}
				if (qm != null) {
					try {
						qm.disconnect();
					} catch (MQException e) {
						getLog().debug("Error disconnecting queue manager", e);
					}
				}
			}
		}
		failIfErrors(errors);
	}

	public List<Hashtable<String, Object>> getConnectionProperties() throws MojoExecutionException {
		List<String> available = new ArrayList<>();
		for (String server : servers()) {
			boolean open = Tools.isPortAvailable(server, mqPort, timeout);
			getLog().info("Testing connection with server: " + server + " port: " + mqPort + " port is open: "
					+ (open ? "yes" : "no"));
			if (open) {
				available.add(server);
			}
		}
		if (available.isEmpty()) {
			throw new MojoExecutionException(
					"No port open for any Queue Manager - Please check host / port configuration");
		}

		ServerCredentials creds = credentials();
		List<Hashtable<String, Object>> result = new ArrayList<>();
		for (String server : available) {
			Hashtable<String, Object> props = new Hashtable<>();
			props.put(MQConstants.HOST_NAME_PROPERTY, server);
			props.put(MQConstants.CHANNEL_PROPERTY, channel);
			props.put(MQConstants.PORT_PROPERTY, mqPort);
			if (SslContexts.notBlank(sslCipherSuite)) {
				props.put(MQConstants.SSL_CIPHER_SUITE_PROPERTY, sslCipherSuite);
				if (SslContexts.notBlank(keystore)) {
					try {
						props.put(MQConstants.SSL_SOCKET_FACTORY_PROPERTY, SslContexts
								.create(keystore, keyStorePassword, keystore, keyStorePassword).getSocketFactory());
					} catch (GeneralSecurityException | IOException e) {
						throw new MojoExecutionException("Unable to load keystore " + keystore, e);
					}
				}
			}
			if (creds.hasUsername()) {
				props.put(MQConstants.USER_ID_PROPERTY, creds.getUsername());
				props.put(MQConstants.USE_MQCSP_AUTHENTICATION_PROPERTY, true);
				if (creds.getPassword() != null) {
					props.put(MQConstants.PASSWORD_PROPERTY, creds.getPassword());
				}
			}
			result.add(props);
		}
		return result;
	}

	public PCFMessage[] sendEscape(PCFMessageAgent agent, String script) throws MQDataException, IOException {
		PCFMessage message = new PCFMessage(MQConstants.MQCMD_ESCAPE);
		message.addParameter(MQConstants.MQIACF_ESCAPE_TYPE, MQConstants.MQET_MQSC);
		message.addParameter(MQConstants.MQCACF_ESCAPE_TEXT, script);
		return agent.send(message);
	}
}
