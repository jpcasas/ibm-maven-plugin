package io.github.jpcasas.ibm.plugin.mq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;


import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.github.jpcasas.ibm.plugin.utils.Tools;

@Mojo(name = "mq-deploy-ssh", requiresProject = false)
public class DeployMQScriptsSSH extends DeployMQScripts {

	

	@Parameter(defaultValue = "echo \"%s\" | runmqsc %s", property = "mq.ssh.exec", required = false)
	private String exec;

	@Parameter(defaultValue = "bash --login -c \"%s\"", property = "mq.ssh.runc", required = false)
	private String runc;

	@Parameter(defaultValue = "~/.ssh/id_rsa", property = "mq.ssh.privatekeylocation", required = false)
	private String privateKeyLocation;

	private Session ss = null;

	@Parameter(defaultValue = "true", property = "mq.key.auth", required = false)
	private boolean auth;

	@Parameter(defaultValue = "22", property = "mq.ssh.port", required = true)
	private int sshPort;

	

	public void execute() throws MojoExecutionException, MojoFailureException {
		Tools.exec = exec;
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

					String cmd = Tools.getCommand(commandMqs, queueManagerName);

					// CHECK IF IS MULTI-INSTANCE QM
					if (mqServer.contains(",")) {
						ss = Tools.getMQActiveInstance(mqServer.split(","), sshPort, user, password, privateKeyLocation,
								auth, queueManagerName, runc, getLog());
					} else {
						ss = (ss == null)
								? Tools.getSession(mqServer, sshPort, user, password, privateKeyLocation, auth,
										getLog())
								: ss;

					}
					if (ss != null) {
						finalResult += Tools.executeSSH(ss, cmd, getLog(), true, true);
					} else {

					}

				} catch (FileNotFoundException e) {

					getLog().error(String.format("ERROR File Not Found -- Skiping MQ Script %s", mqs), e);
					errors.add(e.getMessage());

				} catch (IOException e) {
					getLog().error(
							String.format("ERROR MQ Script %s file properties not found -- Skiping MQ Script", mqs), e);
					errors.add(e.getMessage());

				} catch (JSchException e) {
					getLog().error(String.format("ERROR Connecting QM -> %s Server -> %s ", queueManagerName, mqServer),
							e);
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

}
