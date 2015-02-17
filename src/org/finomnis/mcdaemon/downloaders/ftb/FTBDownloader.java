package org.finomnis.mcdaemon.downloaders.ftb;

import org.finomnis.mcdaemon.MCDaemon;
import org.finomnis.mcdaemon.downloaders.MCDownloader;
import org.finomnis.mcdaemon.tools.ConfigNotFoundException;
import org.finomnis.mcdaemon.tools.CrashReportTools;
import org.finomnis.mcdaemon.tools.CriticalException;
import org.finomnis.mcdaemon.tools.DownloadTools;
import org.finomnis.mcdaemon.tools.FileTools;
import org.finomnis.mcdaemon.tools.Log;
import org.finomnis.mcdaemon.tools.MinecraftTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class FTBDownloader implements MCDownloader {
	private String folderName = "ftb/";
	private String serverName = "server.jar";
	private String serverJarName = folderName + serverName;
	private String serverZipName = folderName + "download.zip";

	private static final String downloadServerName = "http://new.creeperrepo.net";
	private static final String packListUrl = downloadServerName
			+ "/FTB2/static/modpacks.xml";

	private FTBConfigFile ftbConfig;
	private FTBStatusFile ftbStatusFile;

	private boolean updatePrepared = false;

	private String update_dirName;
	private String update_version;
	private String update_serverPack;
	private String update_mcVersion;
	private String update_modPackName;
	private String update_repoVersion;
	
	
	private DocumentBuilder xmlDocumentBuilder = null;

	private NodeList getModPackList() throws SAXException, IOException,
			CriticalException {
		InputStream is = DownloadTools.openUrl(packListUrl);
		Document modpackList = xmlDocumentBuilder.parse(is);
		is.close();
		return modpackList.getElementsByTagName("modpack");
	}

	private void prepareUpdate() throws IOException, CriticalException,
			ParserConfigurationException, SAXException {
		updatePrepared = false;

		update_modPackName = getModpackNameFromConfig();

		String[] properties = getModPackInfos(update_modPackName);

		update_dirName = properties[0];
		update_version = properties[1];
		update_serverPack = properties[2];
		update_mcVersion = properties[3];
		update_repoVersion = properties[4];
		
		if(update_repoVersion == null)
			update_repoVersion = update_mcVersion;
		else if (update_repoVersion.equals(""))
			update_repoVersion = update_mcVersion;
		
		String downloadUrl = getModpackUrl(update_dirName, update_repoVersion,
				update_serverPack);

		Log.out("Downloading \"" + update_modPackName + " v" + update_version
				+ " (" + update_mcVersion + ")\" ...");
		Log.debug("Downloading from \"" + downloadUrl + "\" ...");

		OutputStream fStream = FileTools.openFileWrite(serverZipName, false);
		long downloadSize = DownloadTools.getContentLength(downloadUrl);
		InputStream urlStream = DownloadTools.openUrl(downloadUrl);
		FileTools.writeFromStream(urlStream, fStream, downloadSize);
		fStream.close();
		urlStream.close();

		Log.debug("Calculating MD5 ...");
		String zipMd5 = FileTools.md5(new File(serverZipName));
		Log.debug("Zip MD5: '" + zipMd5 + "'");
		Log.debug("Downloading validation MD5 ...");
		String validationMd5 = getModpackMD5(update_dirName, update_repoVersion,
				update_serverPack);
		boolean md5CheckDisabled;
		try {
			md5CheckDisabled = Boolean.parseBoolean(ftbConfig
					.getConfig("disableMD5Check"));
		} catch (ConfigNotFoundException e) {
			Log.err(e);
			throw new RuntimeException("Should never happen!");
		}
		if(validationMd5 != null && !md5CheckDisabled){
			
			Log.debug("Val MD5: '" + validationMd5 + "'");
		
			if (!zipMd5.equals(validationMd5)) {
				throw new CriticalException(
						"Error downloading Modpack! (MD5 Checksum doesn't fit!)");
			}
			
		} else {
			
			Log.err("Unable to retrieve Validation MD5! Validating Modpack by size ...");
		
			Log.debug("Calculating size ...");
			long zipSize = FileTools.fileSize(serverZipName);
			Log.debug("Zip Size: " + zipSize);
			Log.debug("Downloading validation size ...");
			long validationSize = getModpackSize(update_dirName, update_repoVersion,
					update_serverPack);
			Log.debug("Val Size: " + validationSize);
						
			if (zipSize != validationSize) {
				throw new CriticalException(
						"Error downloading Modpack! (Size comparison failed!)");
			}
			
		}		
		
		updatePrepared = true;
	}

	public List<String> getModPackNames() throws SAXException, IOException,
			CriticalException {

		NodeList modPackList = getModPackList();

		List<String> modPackNames = new ArrayList<String>();

		for (int i = 0; i < modPackList.getLength(); i++) {

			Node modpack = modPackList.item(i);

			if (modpack.getNodeType() != Node.ELEMENT_NODE)
				continue;

			Element element = (Element) modpack;

			modPackNames.add(element.getAttribute("name"));

		}

		return modPackNames;

	}

	public FTBDownloader() throws ParserConfigurationException {
		xmlDocumentBuilder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		ftbConfig = new FTBConfigFile(this);
		ftbConfig.init();
		ftbStatusFile = new FTBStatusFile(this);
		ftbStatusFile.init();
	}

	private String getModpackNameFromConfig() {
		String name;
		try {
			name = ftbConfig.getConfig("modpackName");
		} catch (ConfigNotFoundException e) {
			Log.err(e);
			throw new RuntimeException("Should never happen!");
		}
		if (name.startsWith("\""))
			name = name.substring(1);
		if (name.endsWith("\""))
			name = name.substring(0, name.length() - 1);
		return name;
	}

	private String getActiveModpackName() {
		String name;
		try {
			name = ftbStatusFile.getConfig("activeModpack");
		} catch (ConfigNotFoundException e) {
			Log.err(e);
			throw new RuntimeException("Should never happen!");
		}
		return name;
	}

	@Override
	public void initialize() throws IOException, CriticalException {

		if (!FileTools.folderExists(folderName))
			FileTools.createFolder(folderName);

		CrashReportTools.removeCrashReports(folderName);

		if (!FileTools.fileExists(serverJarName))
			update();
		else if (!getModpackNameFromConfig().equals(getActiveModpackName())) {
			MCDaemon.runBackup();
			update();
		}

		try {
			String mcVersion = ftbStatusFile.getConfig("activeMCVersion");
			String version = ftbStatusFile.getConfig("activeModVersion");
			String modpackName = ftbStatusFile.getConfig("activeModpack");
			setMOTD(modpackName, version, mcVersion);
		} catch (ConfigNotFoundException e) {
			Log.err(e);
		}

	}

	private static String getModpackUrl(String dir, String version, String filename)
			throws IOException, CriticalException {

		return downloadServerName + "/FTB2/modpacks%5E" + dir + "%5E" + version + "%5E" + filename;

	}

	private String[] getModPackInfos(String modpackName)
			throws ParserConfigurationException, SAXException, IOException,
			CriticalException {

		InputStream is = DownloadTools.openUrl(packListUrl);
		Document modpackList = xmlDocumentBuilder.parse(is);
		is.close();

		NodeList modpacks = modpackList.getElementsByTagName("modpack");

		for (int i = 0; i < modpacks.getLength(); i++) {

			Node modpack = modpacks.item(i);

			if (modpack.getNodeType() != Node.ELEMENT_NODE)
				continue;

			Element element = (Element) modpack;

			if (element.getAttribute("name").compareTo(modpackName) != 0)
				continue;

			String[] ret = new String[5];

			ret[0] = element.getAttribute("dir");
			ret[1] = element.getAttribute("version");
			ret[2] = element.getAttribute("serverPack");
			ret[3] = element.getAttribute("mcVersion");
			ret[4] = element.getAttribute("repoVersion");

			return ret;

		}

		throw new CriticalException("Modpack not found!");

	}

	private boolean checkUpdateAvailable() {
		if (!FileTools.fileExists(serverJarName))
			return true;

		try {

			if (!getModpackNameFromConfig().equals(getActiveModpackName()))
				return true;

			String[] properties = getModPackInfos(getActiveModpackName());

			String version = properties[1];
			if (!version.equals(ftbStatusFile.getConfig("activeModVersion")))
				return true;

			return false;
		} catch (Exception e) {
			Log.err(e);
			return false;
		}
	}

	@Override
	public boolean updateAvailable() {

		if (!checkUpdateAvailable())
			return false;

		updatePrepared = false;

		try {
			prepareUpdate();
		} catch (Exception e) {
			MCDaemon.say("Error while updating server.");
			MCDaemon.say("Please contact administrator.");
			Log.err(e);
			return false;
		}

		return true;

	}

	private static String getModpackMD5(String dir, String version,
			String filename) throws MalformedURLException, IOException,
			CriticalException {

		String modpackUrl = getModpackUrl(dir, version, filename);

		return DownloadTools.getContentMD5(modpackUrl);

	}

	private static long getModpackSize(String dir,	String version,
			String filename) throws MalformedURLException, IOException,
			CriticalException {
		
		String modpackUrl = getModpackUrl(dir, version, filename);

		return DownloadTools.getContentLength(modpackUrl);
		
	}
	
	@Override
	public void update() throws IOException, CriticalException {

		try {

			if (!updatePrepared)
				prepareUpdate();
			updatePrepared = false;

			Log.debug("Delete coremods folder...");
			if (FileTools.folderExists(folderName + "coremods/"))
				if (!FileTools.delete(folderName + "coremods/"))
					throw new CriticalException(
							"Unable to delete coremods folder!");

			Log.debug("Delete mods folder...");
			if (FileTools.folderExists(folderName + "mods/"))
				if (!FileTools.delete(folderName + "mods/"))
					throw new CriticalException("Unable to delete mods folder!");

			Log.out("Extracting server files...");
			FileTools.unzip(serverZipName, folderName, true);
			
			ftbStatusFile.setConfig("activeMCVersion", update_mcVersion);
			ftbStatusFile.setConfig("activeModVersion", update_version);
			ftbStatusFile.setConfig("activeModpack", update_modPackName);
			setMOTD(update_modPackName, update_version, update_mcVersion);

		} catch (ConfigNotFoundException | ParserConfigurationException
				| SAXException e) {
			Log.err(e);
			throw new CriticalException("Unable to update!");
		}

	}

	@Override
	public String getFolderName() {
		return folderName;
	}

	@Override
	public String getServerJarName() {
		return serverJarName;
	}

	@Override
	public List<String> getInvocationCommand() {

		List<String> arguments = new ArrayList<String>();

		String path = System.getProperty("java.home") + "/bin/java";
		arguments.add(path);

		try {
			String maxMemory = MCDaemon.getConfig("serverMemory");
			arguments.add("-Xms" + maxMemory + "M");
			arguments.add("-Xmx" + maxMemory + "M");
		} catch (ConfigNotFoundException e) {
			Log.err(e);
		}

		arguments.add("-server");
		arguments.add("-XX:PermSize=256m");
		arguments.add("-d64");
		arguments.add("-XX:+UseParNewGC");
		arguments.add("-XX:+CMSIncrementalPacing");
		arguments.add("-XX:+CMSClassUnloadingEnabled");
		arguments.add("-XX:ParallelGCThreads=2");
		arguments.add("-XX:MinHeapFreeRatio=5");
		arguments.add("-XX:MaxHeapFreeRatio=10");

		arguments.add("-jar");
		arguments.add(serverName);
		arguments.add("nogui");

		return arguments;
	}

	private void setMOTD(String modPackName, String version, String mcVersion) {

		boolean autoSetMOTDEnabled = false;
		try {
			autoSetMOTDEnabled = Boolean.parseBoolean(ftbConfig
					.getConfig("autoSetMOTDEnabled"));
		} catch (ConfigNotFoundException e1) {
			Log.err(e1);
		}
		if (!autoSetMOTDEnabled)
			return;

		String message = "FTB: " + modPackName + " v" + version + " ("
				+ mcVersion + ")";

		Log.out("Setting MOTD to \"" + message + "\"");

		List<String> serverProperties = new ArrayList<String>();

		try {
			Scanner scanner = new Scanner(new FileReader(folderName
					+ "server.properties"));
			try {
				while (scanner.hasNextLine()) {
					String nextLine = scanner.nextLine();
					if (nextLine.matches(".*motd\\=.*\\n?"))
						continue;
					serverProperties.add(nextLine);
				}
			} finally {
				scanner.close();
			}
		} catch (Exception e) {
			Log.out("\"server.properties\" not found. Creating \"server.properties\"...");
		}

		serverProperties.add("motd=" + message);

		try {
			FileWriter fWriter = FileTools.openFileWriteText(folderName
					+ "server.properties", false);
			try {
				for (String line : serverProperties) {
					fWriter.write(line + "\r\n");
				}
			} finally {
				fWriter.close();
			}
		} catch (IOException e) {
			Log.err("Unable to set MOTD!");
			Log.err(e);
		}

	}



	@Override
	public boolean runEditionSpecificCrashTest() {
		boolean res = CrashReportTools.crashReportExists(folderName);
		if(res)
			CrashReportTools.removeCrashReports(folderName);
		return res;
	}

	@Override
	public String getNewVersionName() {
		if(!updatePrepared)
			return null;
		return update_modPackName + " v" + update_version + " ("
				+ update_mcVersion + ")";
	}

	@Override
	public void prepareStart() {
		MinecraftTools.agreeToEula(folderName);
	}

}
