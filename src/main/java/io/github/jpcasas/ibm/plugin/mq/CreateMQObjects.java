package io.github.jpcasas.ibm.plugin.mq;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Mojo(name = "mq-create-objects", requiresProject = false)

public class CreateMQObjects extends AbstractMojo {

	@Parameter(property = "mq.user", required = false)
	private String user;

	@Parameter(property = "mq.password", required = false)
	private String password;

	@Parameter(property = "mq.channel", required = false)
	private String channel;

	@Parameter(property = "mq.port", required = false)
	private int port;

	@Parameter(defaultValue = "${project}", required = true)
	private MavenProject project;

	@Parameter(property = "mq.server", required = false)
	public String mqServer;

	@Parameter(property = "mq.queueManager", required = true)
	public String queueManagerName;

	@Parameter(defaultValue = "true", property = "mq.key.auth", required = false)
	private boolean auth;

	@Parameter(defaultValue = "false", property = "mq.delete", required = false)
	private boolean delete;

	@Parameter(defaultValue = "scripts/mq.json", property = "mq.config", required = false)
	private String configFileName;

	public void printQueue(String name) throws MQException, MQDataException, IOException {

		PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);

		pcfCmd.addParameter(MQConstants.MQCA_Q_NAME, name);
		PCFMessageAgent agent = createAgent();
		try {
			PCFMessage[] response = agent.send(pcfCmd);
			for (PCFMessage message : response) {
				getLog().debug(message.toString());
			}

		} catch (PCFException pcfe) {
			if (pcfe.reasonCode == MQConstants.MQRCCF_OBJECT_ALREADY_EXISTS) {
				getLog().info("The queue already exists on the queue manager.");
			} else {
				throw pcfe;
			}
		}
	}

	public void setAuthrec(String profile, String group, String objtype, ArrayList<String> authadd)
			throws MQException, MQDataException, IOException, MojoFailureException {

		getLog().info("set authrec " + profile + " ...");
		int objectType;
		if (objtype.equals("QUEUE")) {
			objectType = MQConstants.MQOT_Q;
		} else if (objtype.equals("TOPIC")) {
			objectType = MQConstants.MQOT_TOPIC;
		} else {
			throw new MojoFailureException("objtype must be QUEUE or TOPIC");
		}
		int[] authList = new int[authadd.size()];
		int i = 0;
		for (String auth : authadd) {
			if (auth.equals("ALTUSR")) {
				authList[i] = MQConstants.MQAUTH_ALT_USER_AUTHORITY;
				i++;
			} else if (auth.equals("BROWSE")) {
				authList[i] = MQConstants.MQAUTH_BROWSE;
				i++;
			} else if (auth.equals("CHG")) {
				authList[i] = MQConstants.MQAUTH_CHANGE;
				i++;
			} else if (auth.equals("CLR")) {
				authList[i] = MQConstants.MQAUTH_CLEAR;
				i++;
			} else if (auth.equals("CONNECT")) {
				authList[i] = MQConstants.MQAUTH_CONNECT;
				i++;
			} else if (auth.equals("CRT")) {
				authList[i] = MQConstants.MQAUTH_CREATE;
				i++;
			} else if (auth.equals("DLT")) {
				authList[i] = MQConstants.MQAUTH_DELETE;
				i++;
			} else if (auth.equals("DSP")) {
				authList[i] = MQConstants.MQAUTH_DISPLAY;
				i++;
			} else if (auth.equals("GET")) {
				authList[i] = MQConstants.MQAUTH_INPUT;
				i++;
			} else if (auth.equals("INQ")) {
				authList[i] = MQConstants.MQAUTH_INQUIRE;
				i++;
			} else if (auth.equals("PUT")) {
				authList[i] = MQConstants.MQAUTH_OUTPUT;
				i++;
			} else if (auth.equals("PASALL")) {
				authList[i] = MQConstants.MQAUTH_PASS_ALL_CONTEXT;
				i++;
			} else if (auth.equals("PASSID")) {
				authList[i] = MQConstants.MQAUTH_PASS_IDENTITY_CONTEXT;
				i++;
			} else if (auth.equals("SET")) {
				authList[i] = MQConstants.MQAUTH_SET;
				i++;
			} else if (auth.equals("SETALL")) {
				authList[i] = MQConstants.MQAUTH_SET_ALL_CONTEXT;
				i++;
			} else if (auth.equals("SETID")) {
				authList[i] = MQConstants.MQAUTH_SET_IDENTITY_CONTEXT;
				i++;
			} else if (auth.equals("SUB")) {
				authList[i] = MQConstants.MQAUTH_SUBSCRIBE;
				i++;
			} else if (auth.equals("RESUME")) {
				authList[i] = MQConstants.MQAUTH_RESUME;
				i++;
			} else if (auth.equals("PUB")) {
				authList[i] = MQConstants.MQAUTH_PUBLISH;
				i++;
			} else if (auth.equals("SYSTEM")) {
				authList[i] = MQConstants.MQAUTH_SYSTEM;
				i++;
			} else if (auth.equals("CTRL")) {
				authList[i] = MQConstants.MQAUTH_CONTROL;
				i++;
			} else if (auth.equals("CTRLX")) {
				authList[i] = MQConstants.MQAUTH_CONTROL_EXTENDED;
				i++;
			} else if (auth.equals("ALL")) {
				authList[i] = MQConstants.MQAUTH_ALL;
				i++;
			} else if (auth.equals("ALLADM")) {
				authList[i] = MQConstants.MQAUTH_ALL_ADMIN;
				i++;
			} else if (auth.equals("ALLMQI")) {
				authList[i] = MQConstants.MQAUTH_ALL_MQI;
				i++;
			}
		}

		PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_SET_AUTH_REC);
		pcfCmd.addParameter(MQConstants.MQCACF_AUTH_PROFILE_NAME, profile);
		pcfCmd.addParameter(MQConstants.MQIACF_OBJECT_TYPE, objectType);
		pcfCmd.addParameter(MQConstants.MQCACF_GROUP_ENTITY_NAMES, new String[] { group });
		pcfCmd.addParameter(MQConstants.MQIACF_AUTH_ADD_AUTHS, authList);
		PCFMessageAgent agent = createAgent();
		try {
			agent.send(pcfCmd);

		} catch (PCFException pcfe) {
			if (pcfe.reasonCode == MQConstants.MQRCCF_OBJECT_ALREADY_EXISTS) {
				throw new MojoFailureException("The topic already exists on the queue manager.");
			} else if (pcfe.reasonCode == MQConstants.MQRCCF_OBJECT_BEING_DELETED) {
				throw new MojoFailureException("The object is being deleted.");
			} else if (pcfe.reasonCode == MQConstants.MQRC_UNKNOWN_ENTITY) {
				throw new MojoFailureException("Userid not authorized, or unknown. ");
			} else if (pcfe.reasonCode == MQConstants.MQRCCF_AUTH_VALUE_ERROR) {
				throw new MojoFailureException("Invalid authorization. ");
			} else if (pcfe.reasonCode == MQConstants.MQRCCF_AUTH_VALUE_MISSING) {
				throw new MojoFailureException("Authorization missing. ");
			} else if (pcfe.reasonCode == MQConstants.MQRCCF_ENTITY_NAME_MISSING) {
				throw new MojoFailureException("Entity name missing. ");
			} else if (pcfe.reasonCode == MQConstants.MQRCCF_OBJECT_TYPE_MISSING) {
				throw new MojoFailureException("Object type missing.");
			} else if (pcfe.reasonCode == MQConstants.MQRCCF_PROFILE_NAME_ERROR) {
				throw new MojoFailureException("Invalid profile name. ");
			} else {
				throw pcfe;
			}
		}

	}

	public void createTopic(String name, String string, String descr) throws MQException, MQDataException, IOException {

		getLog().info("Creating topic " + name + " ...");
		PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_CREATE_TOPIC);
		pcfCmd.addParameter(MQConstants.MQCA_TOPIC_NAME, name);
		pcfCmd.addParameter(MQConstants.MQCA_TOPIC_STRING, string);
		if (descr != null) {
			pcfCmd.addParameter(MQConstants.MQCA_TOPIC_DESC, descr);
		}
		PCFMessageAgent agent = createAgent();
		try {
			agent.send(pcfCmd);

		} catch (PCFException pcfe) {
			if (pcfe.reasonCode == MQConstants.MQRCCF_OBJECT_ALREADY_EXISTS) {
				getLog().info("The topic already exists on the queue manager.");
			} else {
				throw pcfe;
			}
		}

	}

	public void createAlias(String name, String description, String target, String targtype, boolean get, boolean put)
			throws MQException, MQDataException, IOException, MojoFailureException {

		getLog().info("Creating alias " + name + " ...");

		PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_CREATE_Q);

		pcfCmd.addParameter(MQConstants.MQCA_Q_NAME, name);
		pcfCmd.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_ALIAS);
		if (description != null) {
			pcfCmd.addParameter(MQConstants.MQCA_Q_DESC, description);
		}
		if (targtype.equals("QUEUE")) {
			pcfCmd.addParameter(MQConstants.MQIA_BASE_TYPE, MQConstants.MQOT_Q);
		} else if (targtype.equals("TOPIC")) {
			pcfCmd.addParameter(MQConstants.MQIA_BASE_TYPE, MQConstants.MQOT_TOPIC);
		} else {
			throw new MojoFailureException("targtype must be QUEUE or TOPIC");
		}
		pcfCmd.addParameter(MQConstants.MQCA_BASE_OBJECT_NAME, target);
		if (get) {
			pcfCmd.addParameter(MQConstants.MQIA_INHIBIT_GET, MQConstants.MQQA_GET_ALLOWED);
		} else {
			pcfCmd.addParameter(MQConstants.MQIA_INHIBIT_GET, MQConstants.MQQA_GET_INHIBITED);
		}
		if (put) {
			pcfCmd.addParameter(MQConstants.MQIA_INHIBIT_PUT, MQConstants.MQQA_PUT_ALLOWED);
		} else {
			pcfCmd.addParameter(MQConstants.MQIA_INHIBIT_PUT, MQConstants.MQQA_PUT_INHIBITED);
		}
		PCFMessageAgent agent = createAgent();
		try {
			agent.send(pcfCmd);

		} catch (PCFException pcfe) {
			if (pcfe.reasonCode == MQConstants.MQRCCF_OBJECT_ALREADY_EXISTS) {
				getLog().info("The alias already exists on the queue manager.");
			} else {
				throw pcfe;
			}
		}

	}

	public void createSubscription(String name, String topicstr, String topicobj, String dest, String selector,
			String scope) throws MQException, MQDataException, IOException, MojoFailureException {

		getLog().info("Creating subscription " + name + " ...");
		PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_CREATE_SUBSCRIPTION);

		pcfCmd.addParameter(MQConstants.MQCACF_SUB_NAME, name);
		pcfCmd.addParameter(MQConstants.MQCA_TOPIC_STRING, topicstr);
		pcfCmd.addParameter(MQConstants.MQCA_TOPIC_NAME, topicobj);
		pcfCmd.addParameter(MQConstants.MQCACF_DESTINATION, dest);
		pcfCmd.addParameter(MQConstants.MQCACF_SUB_SELECTOR, selector);
		if (scope.equals("ALL")) {
			pcfCmd.addParameter(MQConstants.MQIACF_SUBSCRIPTION_SCOPE, MQConstants.MQTSCOPE_ALL);
		} else if (scope.equals("QUEUE")) {
			pcfCmd.addParameter(MQConstants.MQIACF_SUBSCRIPTION_SCOPE, MQConstants.MQTSCOPE_QMGR);
		} else {
			throw new MojoFailureException("scope must be ALL or QUEUE");
		}
		PCFMessageAgent agent = createAgent();
		try {
			agent.send(pcfCmd);
		} catch (PCFException pcfe) {
			if (pcfe.reasonCode == MQConstants.MQRCCF_OBJECT_ALREADY_EXISTS) {
				getLog().info("The subscription already exists on the queue manager.");
			}
			if (pcfe.reasonCode == MQConstants.MQRCCF_SUB_ALREADY_EXISTS) {
				getLog().info("The subscription already exists on the queue manager.");
			} else {
				throw pcfe;
			}
		}
	}

	public void createQueue(String name, String description, String boqname, Integer bothresh)
			throws MQException, MQDataException, IOException {

		getLog().info("Creating queue " + name + " ...");

		PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_CREATE_Q);

		pcfCmd.addParameter(MQConstants.MQCA_Q_NAME, name);
		pcfCmd.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_LOCAL);
		if (description != null) {
			pcfCmd.addParameter(MQConstants.MQCA_Q_DESC, description);
		}
		if (boqname != null) {
			pcfCmd.addParameter(MQConstants.MQCA_BACKOUT_REQ_Q_NAME, boqname);
		}
		if (bothresh != null) {
			pcfCmd.addParameter(MQConstants.MQIA_BACKOUT_THRESHOLD, bothresh);
		}
		PCFMessageAgent agent = createAgent();
		try {
			agent.send(pcfCmd);

		} catch (PCFException pcfe) {
			if (pcfe.reasonCode == MQConstants.MQRCCF_OBJECT_ALREADY_EXISTS) {
				getLog().info("The queue already exists on the queue manager.");
			} else {
				throw pcfe;
			}
		}
	}

	public void deleteQueue(String name) throws MQException, MQDataException, IOException {

		getLog().info("Deleting queue " + name + " ...");

		PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_DELETE_Q);

		pcfCmd.addParameter(MQConstants.MQCA_Q_NAME, name);
		PCFMessageAgent agent = createAgent();
		try {
			agent.send(pcfCmd);

		} catch (PCFException pcfe) {
			if (pcfe.reasonCode == MQConstants.MQRCCF_UNKNOWN_OBJECT_NAME) {
				getLog().info("Unknown object");
			} else {
				throw pcfe;
			}
		}
	}

	public void deleteTopic(String name) throws MQException, MQDataException, IOException {

		getLog().info("Deleting queue " + name + " ...");

		PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_DELETE_TOPIC);

		pcfCmd.addParameter(MQConstants.MQCA_TOPIC_NAME, name);
		PCFMessageAgent agent = createAgent();
		try {
			agent.send(pcfCmd);

		} catch (PCFException pcfe) {
			if (pcfe.reasonCode == MQConstants.MQRCCF_UNKNOWN_OBJECT_NAME) {
				getLog().info("Unknown object");
			} else {
				throw pcfe;
			}
		}
	}

	public void deleteAlias(String name) throws MQException, MQDataException, IOException {

		deleteQueue(name);
	}

	public void deleteSubscription(String name) throws MQException, MQDataException, IOException {

		getLog().info("Deleting queue " + name + " ...");

		PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_DELETE_SUBSCRIPTION);

		pcfCmd.addParameter(MQConstants.MQCACF_SUB_NAME, name);
		PCFMessageAgent agent = createAgent();
		try {
			agent.send(pcfCmd);

		} catch (PCFException pcfe) {
			if (pcfe.reasonCode == MQConstants.MQRCCF_UNKNOWN_OBJECT_NAME) {
				getLog().info("Unknown object");
			} else {
				throw pcfe;
			}
		}
	}

	public PCFMessageAgent createAgent() throws MQException, MQDataException {

		MQQueueManager qm = null;
		if (mqServer == null) {
			qm = new MQQueueManager(queueManagerName);
		} else {
			Hashtable<String, Comparable<?>> props = new Hashtable<String, Comparable<?>>();

			props.put(MQConstants.TRANSPORT_PROPERTY, MQConstants.TRANSPORT_MQSERIES_CLIENT);
			props.put(MQConstants.HOST_NAME_PROPERTY, mqServer);
			props.put(MQConstants.CHANNEL_PROPERTY, channel);
			props.put(MQConstants.PORT_PROPERTY, port);
			props.put(MQConstants.USER_ID_PROPERTY, user);
			props.put(MQConstants.USE_MQCSP_AUTHENTICATION_PROPERTY, Boolean.valueOf(auth));
			props.put(MQConstants.PASSWORD_PROPERTY, password);
			qm = new MQQueueManager(queueManagerName, props);
		}
		PCFMessageAgent agent = new PCFMessageAgent(qm);
		return agent;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			byte[] encoded = Files.readAllBytes(Paths.get(configFileName));
			JSONObject jsonObject = new JSONObject(new String(encoded));
			if (!delete) {
				if (jsonObject.has("queues")) {
					JSONArray queues = jsonObject.getJSONArray("queues");
					for (int i = 0; i < queues.length(); i++) {
						JSONObject queue = queues.getJSONObject(i);
						String descr = null;
						String boqname = null;
						Integer bothresh = null;
						if (queue.has("descr")) {
							descr = queue.getString("descr");
						}
						if (queue.has("boqname")) {
							boqname = queue.getString("boqname");
						}
						if (queue.has("boqname")) {
							bothresh = queue.getInt("bothresh");
						}
						createQueue(queue.getString("name"), descr, boqname, bothresh);
					}
				}

				if (jsonObject.has("topics")) {
					JSONArray topics = jsonObject.getJSONArray("topics");
					for (int i = 0; i < topics.length(); i++) {
						JSONObject topic = topics.getJSONObject(i);
						String descr = null;
						if (topic.has("descr")) {
							descr = topic.getString("descr");
						}
						createTopic(topic.getString("name"), topic.getString("string"), descr);
					}
				}
				if (jsonObject.has("aliases")) {
					JSONArray aliases = jsonObject.getJSONArray("aliases");
					for (int i = 0; i < aliases.length(); i++) {
						JSONObject alias = aliases.getJSONObject(i);
						String description = null;
						if (alias.has("description")) {
							description = alias.getString("description");
						}
						createAlias(alias.getString("name"), description, alias.getString("target"),
								alias.getString("targtype"), alias.getBoolean("get"), alias.getBoolean("put"));
					}
				}
				if (jsonObject.has("subscriptions")) {
					JSONArray subscriptions = jsonObject.getJSONArray("subscriptions");
					for (int i = 0; i < subscriptions.length(); i++) {
						JSONObject subscription = subscriptions.getJSONObject(i);
						createSubscription(subscription.getString("name"), subscription.getString("topicstr"),
								subscription.getString("topicobj"), subscription.getString("dest"),
								subscription.getString("selector"), subscription.getString("scope"));
					}
				}
				if (jsonObject.has("authrecs")) {
					JSONArray authrecs = jsonObject.getJSONArray("authrecs");
					for (int i = 0; i < authrecs.length(); i++) {
						JSONObject authrec = authrecs.getJSONObject(i);
						JSONArray authaddjson = authrec.getJSONArray("setauth");
						ArrayList<String> authadd = new ArrayList<String>();
						if (authaddjson != null) {
							for (int j = 0; j < authaddjson.length(); j++) {
								authadd.add(authaddjson.getString(j));
							}
						}
						setAuthrec(authrec.getString("profile"), authrec.getString("group"),
								authrec.getString("objtype"), authadd);
					}
				}
			} else {
				if (jsonObject.has("subscriptions")) {
					JSONArray subscriptions = jsonObject.getJSONArray("subscriptions");
					for (int i = 0; i < subscriptions.length(); i++) {
						JSONObject subscription = subscriptions.getJSONObject(i);
						deleteSubscription(subscription.getString("name"));
					}
				}
				if (jsonObject.has("aliases")) {
					JSONArray aliases = jsonObject.getJSONArray("aliases");
					for (int i = 0; i < aliases.length(); i++) {
						JSONObject alias = aliases.getJSONObject(i);
						deleteAlias(alias.getString("name"));
					}
				}
				if (jsonObject.has("queues")) {
					JSONArray queues = jsonObject.getJSONArray("queues");
					for (int i = 0; i < queues.length(); i++) {
						JSONObject queue = queues.getJSONObject(i);
						deleteQueue(queue.getString("name"));
					}
				}

				if (jsonObject.has("topics")) {
					JSONArray topics = jsonObject.getJSONArray("topics");
					for (int i = 0; i < topics.length(); i++) {
						JSONObject topic = topics.getJSONObject(i);
						deleteTopic(topic.getString("name"));
					}
				}
			}

		} catch (MQDataException e) {
			e.printStackTrace();
			throw new MojoFailureException(e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			throw new MojoFailureException(e.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			throw new MojoFailureException(e.toString());
		} catch (MQException e) {
			e.printStackTrace();
			throw new MojoFailureException(e.toString());
		}
		return;
	}
}
