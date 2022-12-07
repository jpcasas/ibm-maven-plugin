package io.github.jpcasas.ibm.plugin.mq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.MQCFST;
import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.github.jpcasas.ibm.plugin.utils.Tools;

@Mojo(name = "mq-deploy-pcf", requiresProject = false)
public class DeployMQScriptsPCF extends DeployMQScripts {

	@Parameter(defaultValue = "1414", property = "mq.port", required = false)
	private int mqPort;

	@Parameter(property = "mq.channel", required = true)
	private String channel;

	@Parameter(property = "mq.keystore", required = false)
	private String keystore;

	@Parameter(property = "mq.keystorePassword", required = false)
	private String keyStorePassword;

	@Parameter(property = "mq.cipherSuite", required = false)
	private String sslCipherSuite;

	@Parameter(defaultValue = "1000", property = "mq.timeout", required = true)
	private int timeout;

	public void execute() throws MojoExecutionException, MojoFailureException {
		
		int finalResult = 0;
		List<String> errors = new ArrayList<String>();
		getLog().info("----------------------------------------------------");
		getLog().info("\r\n" + "\r\n"
				+ "    ____             __               __  _______         __   __  _______      _____           _       __      \r\n"
				+ "   / __ \\___  ____  / /___  __  __   /  |/  / __ \\      _/_/  /  |/  / __ \\    / ___/__________(_)___  / /______\r\n"
				+ "  / / / / _ \\/ __ \\/ / __ \\/ / / /  / /|_/ / / / /    _/_/   / /|_/ / / / /    \\__ \\/ ___/ ___/ / __ \\/ __/ ___/\r\n"
				+ " / /_/ /  __/ /_/ / / /_/ / /_/ /  / /  / / /_/ /   _/_/    / /  / / /_/ /    ___/ / /__/ /  / / /_/ / /_(__  ) \r\n"
				+ "/_____/\\___/ .___/_/\\____/\\__, /  /_/  /_/\\___\\_\\  /_/     /_/  /_/\\___\\_\\   /____/\\___/_/  /_/ .___/\\__/____/  \r\n"
				+ "          /_/            /____/                                                              /_/                \r\n"
				+ "\r\n" + "");
		getLog().info("----------------------------------------------------");
		getLog().info(" ");

		File mqscripts = (operation.equals("install")) ? mqscriptsInstall : mqscriptsUninstall;

		if (mqscripts != null && mqscripts.exists()) {

			List<File> files = (List<File>) FileUtils.listFiles(mqscripts, new String[] { "mqs", "mqsc" }, true);

			for (File mqs : files) {

				try {
					String sname = FilenameUtils.removeExtension(mqs.getName());
					String mqsFileProps = String.format("%s.%s", sname, pextension);

					File mqsProperties = Tools.searchFile(project.getBasedir(), mqsFileProps);

					Properties main = new Properties();

					if (enviromentProperties != null && enviromentProperties.exists()) {
						main.load(new FileReader(enviromentProperties));
					}

					if (mqsProperties != null && mqsProperties.exists()) {

						main.load(new FileReader(mqsProperties));
					}
					InputStream is = new FileInputStream(mqs);
					String mqsModel = IOUtils.toString(is, StandardCharsets.UTF_8.name());

					String commandMqs = Tools.getMQSC(mqsModel, main);

					List<Hashtable> conns = getHashMapInfo();
					for (Hashtable connInfo : conns) {
						MQQueueManager qm = new MQQueueManager(queueManagerName, connInfo);
						PCFMessageAgent pcfA = new PCFImplementation(qm);
						PCFMessage[] results = sendEscape(pcfA, commandMqs);
						for (PCFMessage retMessage : results) {
							Enumeration en = retMessage.getParameters();
							while(en.hasMoreElements()){
								Object param = en.nextElement();
								if(param instanceof MQCFST){
									String val = ((MQCFST)param).getString();
									getLog().info(val);
								}
							}
							
							
						}
					}

					

				} catch (FileNotFoundException e) {

					getLog().error(String.format("ERROR File Not Found -- Skiping MQ Script %s", mqs), e);
					errors.add(e.getMessage());

				} catch (IOException e) {
					getLog().error(
							String.format("ERROR MQ Script %s file properties not found -- Skiping MQ Script", mqs), e);
					errors.add(e.getMessage());

				} catch (MQDataException e) {
					getLog().error("MQDataException ", e);
					errors.add(e.getMessage());
				} catch (MQException e) {
					getLog().error("MQException ", e);
					errors.add(e.getMessage());
				} 
			}

			if (finalResult != 0 || errors.size() > 0) {
				for (String string : errors) {
					getLog().error(String.format("ERROR  %s ", string));
				}
				throw new MojoFailureException("SOMETHING WENT WRONG PLEASE CHECK THE LOG");
			}
		}

	}

	public List<Hashtable> getHashMapInfo() throws MojoExecutionException {
		ArrayList<String> avalibleServers = new ArrayList<>();
		ArrayList<Hashtable> connInformation = new ArrayList<>();

		boolean isConnPossible = false;
		if (mqServer.contains(separator)) {
			String servers[] = mqServer.split(separator);
			for (String string : servers) {
				
				boolean test = Tools.isPortAvailable(string, mqPort, timeout);
				isConnPossible |= test;
				if (test)
					avalibleServers.add(string);

				getLog().info("Testing connection with server: "+string+" port: "+mqPort+ " port is open: "+((test)?"yes":"no"));
			}
		}else{

			if((isConnPossible=Tools.isPortAvailable(mqServer, mqPort, timeout))){
				avalibleServers.add(mqServer);
				
			}
		}
		if (!isConnPossible) {
			throw new MojoExecutionException(
					"No port open for any Queue Manager -  Please check host / port configuration");
		}
		for (String avServers : avalibleServers) {

			Hashtable connProperties = new Hashtable<>();
			connProperties.put("hostname", avServers);
			connProperties.put("channel", channel);
			connProperties.put("port", mqPort);
			if (sslCipherSuite != null) {
				connProperties.put("sslCipherSuite", sslCipherSuite);
				MQEnvironment.sslCipherSuite = sslCipherSuite;
			}
			if (user != null) {
				connProperties.put(MQConstants.USER_ID_PROPERTY, user);
				connProperties.put(MQConstants.PASSWORD_PROPERTY, password);
			}
			connInformation.add(connProperties);

		}

		return connInformation;
	}
	public PCFMessage[] sendEscape(PCFMessageAgent agent, String scirpt) throws PCFException, MQDataException, IOException{
		PCFMessage message = new PCFMessage(MQConstants.MQCMD_ESCAPE);
		message.addParameter(MQConstants.MQIACF_ESCAPE_TYPE, MQConstants.MQET_MQSC);
		message.addParameter(MQConstants.MQCACF_ESCAPE_TEXT, scirpt);
		return agent.send(message);
		
	}


}
