package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.ibm.integration.admin.model.common.LogEntry;
import com.ibm.integration.admin.model.server.DeployResult;
import com.ibm.integration.admin.proxy.IntegrationAdminException;
import com.ibm.integration.admin.proxy.IntegrationNodeProxy;
import com.ibm.integration.admin.proxy.IntegrationServerProxy;

/**
 *
 * @author Juan Pablo Casas
 */
@Mojo(name = "ace-deploy", requiresProject = false)
public class BarDeploy extends AbstractMojo {

	@Parameter(property = "ibm.ace.sec.truststore", required = false)
	private String truststore;

	@Parameter(property = "ibm.ace.sec.truststorePassword", required = false)
	private String truststorePassword;

	@Parameter(property = "ibm.ace.sec.keystore", required = false)
	private String keystore;

	@Parameter(property = "ibm.ace.sec.keystorePassword", required = false)
	private String keystorePassword;

	@Parameter(defaultValue = "", property = "ibm.ace.sec.user", required = false)
	private String user;

	@Parameter(defaultValue = "", property = "ibm.ace.sec.password", required = false)
	private String password;

	@Parameter(defaultValue = "600", property = "ibm.ace.timeout", required = false)
	private int timeout;

	@Parameter(defaultValue = "false", property = "ibm.ace.useSSL", required = false)
	private boolean useSSL;

	@Parameter(property = "ibm.ace.host")
	private String host;

	@Parameter(defaultValue = "4414", property = "ibm.ace.port")
	private int port;

	@Parameter(property = "ibm.ace.is")
	private String integrationServer;

	@Parameter(property = "ibm.ace.bar", required = false)
	private File barFileName;

	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("----------------------------------------------------");
		getLog().info("        Integration Bus - DEPLOY BAR                ");
		getLog().info("----------------------------------------------------");
		getLog().info(" ");
		
		
		if (useSSL) {
			System.setProperty("javax.net.ssl.trustStore", truststore);
			System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
			System.setProperty("javax.net.ssl.keyStore", keystore);
			System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
		}
		
		List<File> bars =  null;
		
		if(barFileName == null) {
			 bars = (List<File>) FileUtils.listFiles(new File("."), new String[] {"bar"} , true);
			
		}else {
			bars = new ArrayList<File>(); 
			bars.add(barFileName);
		}
		IntegrationNodeProxy b = null;
		DeployResult deployResult = null;
		IntegrationServerProxy eg = null;
		
		if(bars.size()>0) {
			getLog().info("Connecting to the integration node running at " + host + ":" + port + "...");
			b = new IntegrationNodeProxy(host, port, user, password, useSSL);

			getLog().info("Discovering integration server '" + integrationServer + "'...");
			

			try {
				eg = b.getIntegrationServerByName(integrationServer);
			} catch (IntegrationAdminException e) {
				getLog().error(String.format("ERROR Integration Admin Exception %s", e.getMessage()));
				throw new MojoFailureException("Integration Admin Exception", e);
			}

			if (eg == null) {
				getLog().info("Integration server not found");
			    throw new MojoExecutionException("Integration Server not found");
			}

		}
		boolean fail = false;
		for (File bar : bars) {
			
			try {
				
				getLog().info("Deploying " + bar.getAbsolutePath() + "...");

				deployResult = eg.deploy(bar.getAbsolutePath());

				getLog().info("Result deploying " + bar.getName() + " = " + b.getLastHttpResponse().getStatusCode());
				
				if (b.getLastHttpResponse().getStatusCode() > 299) {
					getLog().info("----------------------------------------------------");
					getLog().info("        Application NOT DEPLOYED                    ");
					getLog().info("----------------------------------------------------");
					LogEntry[] logs = deployResult.getLogEntry();
					getLog().info("        REASONS:                                    ");
					for (LogEntry logEntry : logs) {
						getLog().info("----------------------------------------------------");
						getLog().info(logEntry.getDetailedText());
					}

				} else {

					getLog().info("----------------------------------------------------");
					getLog().info("        Application DEPLOYED                        ");
					getLog().info("----------------------------------------------------");

				}

			} catch (IntegrationAdminException e) {
				getLog().error(String.format("ERROR Integration Admin Exception %s", e.getMessage()));
				fail = true;
			
			}

			
		}
		if(fail) throw new MojoExecutionException("Some bar files wheren't deployed");
	}
}
