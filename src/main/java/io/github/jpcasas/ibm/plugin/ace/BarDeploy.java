package io.github.jpcasas.ibm.plugin.ace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.github.jpcasas.ibm.plugin.model.DeployResult;
import io.github.jpcasas.ibm.plugin.model.LogEntry;
import io.github.jpcasas.ibm.plugin.utils.Tools;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;

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

		List<File> bars = null;

		if (barFileName == null) {
			bars = (List<File>) FileUtils.listFiles(new File("."), new String[] { "bar" }, true);

		} else {
			bars = new ArrayList<File>();
			bars.add(barFileName);
		}
		
		DeployResult deployResult = null;
		
		String type = detectDeploymentType();

		boolean fail = false;
		for (File bar : bars) {

			try {

				getLog().info("Deploying " + bar.getAbsolutePath() + "..." +" MODE: "+type);

				deployResult = deploy(bar, (type.equals("integrationNode"))?integrationServer:null);

				if (deployResult == null) {

					getLog().info("----------------------------------------------------");
					getLog().info("        Comm Error No response                      ");
					getLog().info("      STATUS:   "+( (response==null)?"":response.getStatus()+" "+response.getStatusText() )+"                      ");
					getLog().info("----------------------------------------------------");

				} else {
					getLog().info("Result deploying " + bar.getName());

					if (Integer.parseInt(deployResult.getCount()) > 0) {
						LogEntry info = deployResult.getLogEntry()[0];
						if (info.getMessage().getSeverity().equals("0")) {
							getLog().info("----------------------------------------------------");
							getLog().info("        Application DEPLOYED                        ");
							getLog().info("----------------------------------------------------");

						} else {
							getLog().info("----------------------------------------------------");
							getLog().info("        Application NOT DEPLOYED                    ");
							getLog().info("----------------------------------------------------");

						}
						printEntries(deployResult);

					}

				}

			} catch (IOException e) {
				getLog().error(String.format("ERROR IOException %s", e.getMessage()));
				fail = true;
			}

		}
		if (fail)
			throw new MojoExecutionException("Some bar files wheren't deployed");
	}

	private void printEntries(DeployResult deployResult) {
		LogEntry[] logs = deployResult.getLogEntry();
		getLog().info("        Logs:                                    ");
		for (LogEntry logEntry : logs) {
			getLog().info("----------------------------------------------------");
			getLog().info(logEntry.getDetailedText());
		}

	}

	private HttpResponse<DeployResult> response;
	private DeployResult deploy(File bar, String is) throws IOException {

		Unirest.config().verifySsl(false);
		String httpSecure = (useSSL) ? "https://" : "http://";
		String urlRequest = null;
		if(is!=null){
			urlRequest = String.format("%s%s:%s/apiv2/servers/%s/deploy", httpSecure, host, port, is);	
		}else{
			urlRequest = String.format("%s%s:%s/apiv2/deploy", httpSecure, host, port);	
		}
		
		String usrpass = Tools.base64(user + ":" + password);
		this.response = Unirest.post(urlRequest).header("accept", "application/json")
				.header("Content-Type", " application/octet-stream").header("Authorization", "Basic " + usrpass)
				.body(Tools.toByteArray(bar)).asObject(DeployResult.class);
		if (response.isSuccess())
			return response.getBody();
		return null;
	}

	private String detectDeploymentType() {
		Unirest.config().verifySsl(false);
		String httpSecure = (useSSL) ? "https://" : "http://";
		String urlRequest = String.format("%s%s:%s/apiv2/", httpSecure, host, port);
		String usrpass = Tools.base64(user + ":" + password);
		HttpResponse<JsonNode> res = Unirest.get(urlRequest).header("Authorization", "Basic " + usrpass).asJson();
		JsonNode body = res.getBody();
		if (body != null) {
			JSONObject json = body.getObject();
			if (json != null) {
				return json.getString("type");
			}
		}
		return "integrationNode";
	}

}
