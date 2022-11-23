package io.github.jpcasas.ibm.plugin.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.ricksbrown.cowsay.plugin.CowExecutor;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class Tools {

	public static final String MODEL = "/defaultValues/%s.properties";
	public static final String MODEL_MQSC = "/templates/%s.mustache";

	public static File searchFile(File dir, String file) {
		if (dir == null) {
			dir = new File("").getAbsoluteFile();

		}
		if (dir != null && file != null) {
			Collection<File> results = FileUtils.listFiles(dir, FileFilterUtils.nameFileFilter(file),
					TrueFileFilter.INSTANCE);
			for (Iterator<File> iterator = results.iterator(); iterator.hasNext();) {
				return iterator.next();

			}
		}
		return null;
	}

	public static ArrayList<File> searchFiles(File dir, String suffix) {
		ArrayList<File> files = new ArrayList<File>();
		if (dir != null && suffix != null) {
			Collection<File> results = FileUtils.listFiles(dir, FileFilterUtils.suffixFileFilter(suffix),
					TrueFileFilter.INSTANCE);
			files.addAll(results);
		}
		return files;
	}

	public static File searchProductYAMLFile(File dir) {
		List<File> listYamlFiles = searchYAMLFiles(dir);
		for (File yamlFile : listYamlFiles) {
			FileReader fr = null;
			BufferedReader br = null;
			try {
				fr = new FileReader(yamlFile);
				br = new BufferedReader(fr);
				String line;
				while ((line = br.readLine()) != null) {
					if (line.startsWith("product:")) {
						fr.close();
						br.close();
						return yamlFile;
					} else {
						continue;
					}
				}
			} catch (Exception ex) {
				try {
					if (fr != null)
						fr.close();
					if (br != null)
						br.close();
				} catch (IOException ioException) {
				}
			}
		}
		return null;
	}

	public static ArrayList<File> searchYAMLFiles(File dir) {
		ArrayList<File> files = new ArrayList<File>();
		if (dir != null) {
			Collection<File> results = FileUtils.listFiles(dir, new String[] { "yaml", "yml" }, true);
			files.addAll(results);
		}
		return files;
	}

	public static ArrayList<File> searchFilesByExtensions(File dir, String[] extensions) {
		ArrayList<File> files = new ArrayList<File>();
		if (dir != null && extensions != null) {
			Collection<File> results = FileUtils.listFiles(dir, extensions, true);
			files.addAll(results);
		}
		return files;
	}

	public static void throwMojoException(String string) throws MojoExecutionException {
		throw new MojoExecutionException(string);

	}

	public static void copyFiles(File pmodel, File resources, String env[], String pextension) throws IOException {
		for (int i = 0; i < env.length; i++) {
			FileUtils.copyFile(pmodel, new File(resources, env[i] + pextension));
		}

	}

	public static String executeCommand(String command, Log log)
			throws InterruptedException, IOException, MojoExecutionException {
		return executeCommand(command, log, null, true);
	}

	public static String executeCommand(String command, Log log, boolean print)
			throws InterruptedException, IOException, MojoExecutionException {
		return executeCommand(command, log, null, print);
	}

	public static String executeCommand(String command, Log log, String[] var)
			throws InterruptedException, IOException, MojoExecutionException {

		return executeCommand(command, log, var, true);
	}

	public static String executeCommand(String command, Log log, String[] var, boolean print)
			throws InterruptedException, IOException, MojoExecutionException {

		Runtime runtime = Runtime.getRuntime();
		Process p = null;
		if (var == null) {
			p = runtime.exec(command);
		} else {
			p = runtime.exec(command, var);
		}
		StringBuilder sout = new StringBuilder();
		sout.append(Tools.print(p.getInputStream(), log, Level.ALL, print));

		Tools.print(p.getErrorStream(), log, Level.SEVERE);
		p.waitFor();

		if (p.exitValue() != 0) {
			throw new MojoExecutionException("ERROR: the executed runtime command failed !");
		}
		return sout.toString();
	}

	public static void print(InputStream inputStream) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
			reader.close();
		} catch (IOException ex) {
			Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static String print(InputStream inputStream, Log log, Level level) {
		return print(inputStream, log, level, true);
	}

	public static String print(InputStream inputStream, Log log, Level level, boolean print) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (print) {
					if (Level.SEVERE == level) {
						log.error(line);
					}
					if (Level.ALL == level) {
						log.info(line);
					}
				}
				sb.append(line + "\n");
			}
			reader.close();
			return sb.toString();
		} catch (IOException ex) {
			Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
			return "";
		}
	}

	public static int executeSSH(Session session, String command, Log log, boolean printCommand, boolean checkSyntax)
			throws JSchException, IOException {
		ChannelExec channel = null;
		try {
			if (printCommand)
				log.info(command);
			channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(command);
			channel.setInputStream(null);
			channel.setErrStream(null);
			InputStream in = channel.getInputStream();
			InputStream err = channel.getErrStream();
			channel.connect();
			String out = printOutput(in);
			String error = printOutput(err);
			log.info(out);
			if (!(error != null && error.trim().equals(""))) {
				log.error(error);
			}
			if (checkSyntax) {
				if (out.contains("command has a syntax error")) {
					return 1;
				}
			}

			return channel.getExitStatus();

		} finally {
			channel.disconnect();
		}

	}

	public static String[] executeSSH(Session session, String command, Log log, boolean printCommand)
			throws JSchException, IOException {
		ChannelExec channel = null;
		try {
			if (printCommand)
				log.info(command);
			channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(command);
			channel.setInputStream(null);
			channel.setErrStream(null);
			InputStream in = channel.getInputStream();
			InputStream err = channel.getErrStream();
			channel.connect();
			String out = printOutput(in);
			String error = printOutput(err);
			log.info(out);
			if (!(error != null && error.trim().equals(""))) {
				log.error(error);
			}

			return new String[] { channel.getExitStatus() + "", out, error };

		} finally {
			channel.disconnect();
		}

	}

	private static String printOutput(InputStream in) throws IOException {

		String text = IOUtils.toString(in, StandardCharsets.UTF_8.name());

		return text;

	}

	public static int AES_KEY_SIZE = 256;
	public static int IV_SIZE = 96;
	public static int TAG_BIT_LENGTH = 128;
	public static String ALGO_TRANSFORMATION_STRING = "AES/GCM/PKCS5Padding";
	public static byte[] aadData = "com.michelin.encryption".getBytes();
	public static byte[] iv = new byte[] { -58, -108, 63, 40, -93, 7, 29, -126, -94, 27, -127, 6, -19, -17, 7, 22, 83,
			109, 113, 43, 7, 3, -69, -68, 121, -49, 16, 23, -39, 7, 104, 22, -99, 5, 23, -53, -121, -90, -53, -62, 96,
			-109, -85, -82, 59, 62, -26, 109, 112, -53, -99, -116, -65, 53, 84, 58, -63, -73, 104, 89, 77, -50, 99,
			-111, 69, -70, -113, -93, -79, 51, -10, -79, -85, -49, -49, -101, -97, 92, 7, -75, -89, -67, -114, 95, -96,
			-98, -54, 91, 35, -8, -118, -107, -14, 100, -42, -22 };
	public static byte[] key = new byte[] { -5, -103, -49, -11, 96, 38, 25, 106, -42, 97, 52, 7, 46, 68, -23, 1, -121,
			9, -97, 102, -115, 11, -3, -67, -34, 90, -98, -28, 31, -34, -4, 26 };
	private static SecretKey aesKey;

	public static String decrypt(String encryptedText, Log log) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		aesKey = new SecretKeySpec(key, "AES");

		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);

		byte[] decryptedText = aesDecrypt(Base64.getDecoder().decode(encryptedText), aesKey, gcmParamSpec, aadData,
				log);
		return new String(decryptedText);

	}

	public static byte[] aesDecrypt(byte[] encryptedMessage, SecretKey aesKey, GCMParameterSpec gcmParamSpec,
			byte[] aadData, Log log) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		Cipher c = null;

		c = Cipher.getInstance(ALGO_TRANSFORMATION_STRING); // Transformation
															// specifies
															// algortihm,
															// mode of
															// operation and
															// padding

		c.init(Cipher.DECRYPT_MODE, aesKey, gcmParamSpec, new SecureRandom());

		c.updateAAD(aadData); // Add AAD details before decrypting

		byte[] plainTextInByteArr = null;

		plainTextInByteArr = c.doFinal(encryptedMessage);

		return plainTextInByteArr;
	}

	public static void sendFile(Session session, File lfile, String rfile) throws Exception {

		FileInputStream fis = null;
		try {

			String command = "scp -t " + rfile;
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			if (checkAck(in) != 0) {
				throw new Exception("Unable get input stream");
			}

			long filesize = lfile.length();
			command = "C0644 " + filesize + " " + lfile;
			command += "\n";
			out.write(command.getBytes());
			out.flush();
			if (checkAck(in) != 0) {
				throw new Exception("Unable to execute " + command);
			}
			fis = new FileInputStream(lfile);
			IOUtils.copy(fis, out);

			out.flush();
			if (checkAck(in) != 0) {
				throw new Exception("Unable to execute " + command);
			}
			out.close();

			channel.disconnect();
			session.disconnect();

		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (Exception ee) {
			}
		}
	}

	static int checkAck(InputStream in) throws IOException {
		int b = in.read();

		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				System.out.print(sb.toString());
			}
			if (b == 2) { // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}

	public static Properties loadProperties(String route, String file) throws IOException {
		Properties prop = new Properties();
		InputStream in = Tools.class.getClass().getResourceAsStream(String.format("%s/%s", route, file));
		prop.load(in);
		in.close();
		return prop;
	}

	@SuppressWarnings("unused")
	private static String getCheckObject(String qm, String type, String queueName) {
		if (type.equalsIgnoreCase("LOCAL") || type.equalsIgnoreCase("REMOTE") || type.equalsIgnoreCase("BACKOUT")
				|| type.equalsIgnoreCase("ALIAS")) {
			return String.format("echo 'DIS Q(%s)' | runmqsc %s", queueName, qm);
		}
		return "";
	}

	public static String getMQSC(String strTemplate, Properties queuep) throws IOException {
		return template(strTemplate, queuep, null);
	}

	public static String template(String strTemplate, Properties queuep, Properties additional) throws IOException {
		MustacheFactory mf = new DefaultMustacheFactory();

		if (additional != null) {
			queuep.putAll(additional);
		}

		StringWriter command = new StringWriter();
		if (strTemplate != null) {

			Mustache mustache = mf.compile(new StringReader(strTemplate), "template");
			mustache.execute(command, queuep).flush();
			return command.toString();
		}
		return null;

	}

	public static Properties addDefaults(Properties defaults, Properties principal) {
		Properties props = new Properties();
		props.putAll(defaults);

		props.putAll(principal);

		return props;

	}

	public static void cleanComments(Properties properties) {
		Set<Object> envKeys = properties.keySet();
		for (Object object : envKeys) {
			String key = (String) object;
			String val = properties.getProperty(key);
			if (val.contains("#")) {
				properties.put(key, val.substring(0, val.indexOf("#")).trim());
			}
		}

	}

	public static void getFileIfNotExist(String repo, String qmListGroupId, String qmListArtifact, String fileToSearch)
			throws MalformedURLException, IOException {
		File file = new File(fileToSearch);
		if (!file.exists()) {
			String version = getLastVersion(repo, qmListGroupId, qmListArtifact);
			if (version != null) {
				downloadFileFromRepo(repo, qmListGroupId, qmListArtifact, version, fileToSearch);
			}
		}
		return;

	}

	public static void downloadFileFromRepo(String repo, String qmListGroupId, String qmListArtifact, String version,
			String file) throws MalformedURLException, IOException {
		String groupUrl = qmListGroupId.replaceAll("\\.", "/");
		String url = String.format("%s/%s/%s/%s/%s/%s-%s-%s.tar.gz!/%s/%s", repo, "libs-release-local", groupUrl,
				qmListArtifact, version, qmListArtifact, version, qmListArtifact, qmListArtifact, file);

		InputStream ins = new URL(url).openStream();

		// read text returned by server
		BufferedReader in = new BufferedReader(new InputStreamReader(ins));

		String line = null;
		PrintWriter pr = new PrintWriter(new File(file));

		while ((line = in.readLine()) != null) {
			pr.println(line);
		}
		pr.flush();
		pr.close();

	}

	public static String getLastVersion(String repo, String qmListGroupId, String qmListArtifact) throws IOException {

		URL url = new URL(String.format("%s/api/search/latestVersion?g=%s&a=%s&repos=%s", repo, qmListGroupId,
				qmListArtifact, "libs-release-local"));

		// read text returned by server
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		String line = in.readLine();

		return line;

	}

	public static Session getMQActiveInstance(String[] server, int port, String user, String password,
			String privateKeyLocation, boolean keyauth, String qmName, String cmd, Log log)
			throws JSchException, IOException {

		String command = String.format(cmd, "dspmq");
		for (String srv : server) {
			log.info("Checking MQ " + srv);
			Session session = getSession(srv, port, user, password, privateKeyLocation, keyauth, log);
			String[] result = executeSSH(session, command, log, false);

			if (result[0].equals("0")) {
				if (result[1].contains("STATUS(Running)")) {
					log.info("ACTIVE NODE = " + srv);
					log.info(result[1]);
					return session;
				}
			}
		}
		return null;
	}

	public static Session getSession(String server, int port, String user, String password, String privateKeyLocation,
			boolean keyauth, Log log) throws JSchException {
		Session ss = null;

		JSch jsch = new JSch();
		log.debug("USER = " + user);
		if (user == null || user.trim().equals("")) {
			log.debug("USER NOT FOUND GETTING FROM PROPS");
			user = System.getenv("LOGNAME");
			log.debug("USER FROM PROPS " + user);

			if (user == null || user.trim().equals("")) {
				log.debug("USER NOT FOUND GETTING FROM JAVA PROPS");
				user = System.getProperty("user.name");
				log.debug("USER FROM JAVA PROPS " + user);
			}
		}

		ss = jsch.getSession(user, server, port);

		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");

		if (keyauth) {
			log.debug("USING KEY AUTH ");
			File rsa = new File(privateKeyLocation);

			log.debug(String.format("File Exist %s", rsa.exists()));
			if (rsa.exists()) {
				log.debug(String.format("Route file %s", rsa.getAbsoluteFile()));
			} else {
				log.debug("PRIVATE KEY NOT FOUND");
				String home = System.getenv("HOME");
				log.debug("Abosolute " + rsa.getAbsolutePath());
				log.debug("Home user by ENVIROMENT = " + home);
				String fullPath = String.format("%s/.ssh/id_rsa", home);
				log.debug("FULL PATH ENV " + fullPath);
				rsa = new File(fullPath);
				log.debug("FULL PATH EXISTS " + rsa.exists());
				if (!rsa.exists()) {
					home = System.getProperty("user.home");
					log.debug("JAVA USER HOME = " + home);
					fullPath = String.format("%s/.ssh/id_rsa", home);
					log.debug("JAVA USER HOME FULL PATH = " + home);
					rsa = new File(fullPath);
				}
			}
			if (rsa.exists()) {
				log.debug("ADDING KEY = " + rsa.getAbsolutePath());
				jsch.addIdentity(rsa.getAbsolutePath());
			} else {
				ss.setPassword(password);
			}

		} else {
			ss.setPassword(password);
		}
		ss.setConfig(config);
		ss.connect();
		return ss;

	}

	public static void printMessage(String string, Log log) {
		log.info("---------------------------------------");
		log.info(string);
		log.info("---------------------------------------");

	}

	public static String exec = "echo \"%s\" | runmqsc %s";

	public static String getCommand(String existsMQS, String queueManagerName) {
		return String.format(exec, existsMQS, queueManagerName);
	}

	public static void uncompress(File file, File outputDirectory) throws net.lingala.zip4j.exception.ZipException {
		new net.lingala.zip4j.ZipFile(file.getAbsolutePath()).extractAll(outputDirectory.getAbsolutePath());

	}

	public static void zipFolder(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {

		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			if (fileName.endsWith("/")) {
				zipOut.putNextEntry(new ZipEntry(fileName));
				zipOut.closeEntry();
			}
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				String pre = (fileName.equals("")) ? "" : fileName + "/";
				zipFolder(childFile, pre + childFile.getName(), zipOut);
			}
			return;
		}
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		fis.close();
	}

	public static String camelCase(String chain) {
		String lowerCase = chain.toLowerCase();
		String[] tokens = lowerCase.split("_");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].length() > 1) {
				sb.append(String.format("%s%s", ("" + tokens[i].charAt(0)).toUpperCase(), tokens[i].substring(1)));
			} else {
				sb.append(tokens[i].toUpperCase());
			}

		}
		return sb.toString();
	}

	public static void writeYaml(File subsDir, String string, Object object)
			throws JsonGenerationException, JsonMappingException, IOException {
		writeWithJackson(new YAMLFactory(), subsDir, string, object);

	}

	public static void writeJSON(File folder, String filename, Object object)
			throws JsonGenerationException, JsonMappingException, IOException {
		writeWithJackson(new JsonFactory(), folder, filename, object);
	}

	private static void writeWithJackson(JsonFactory factory, File folder, String filename, Object object)
			throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper om = new ObjectMapper(factory);
		om.setSerializationInclusion(Include.NON_NULL);
		om.writeValue(new File(folder, filename), object);

	}

	

	private static CowExecutor cowExecutor = new CowExecutor();

	public static String cowsay(String message) {
		cowExecutor.setMessage(message);
		return cowExecutor.execute();
	}

    public static String base64(String string) {
        return Base64.getEncoder().encodeToString(string.getBytes());
    }

	public static byte[] toByteArray(File bar) throws IOException {
		return Files.readAllBytes(bar.toPath());
	}

}
