package org.finomnis.mcdaemon.downloaders.ftb;

import org.finomnis.mcdaemon.MCDaemon;
import org.finomnis.mcdaemon.downloaders.MCDownloader;
import org.finomnis.mcdaemon.tools.ConfigFile;
import org.finomnis.mcdaemon.tools.ConfigNotFoundException;
import org.finomnis.mcdaemon.tools.CriticalException;
import org.finomnis.mcdaemon.tools.DownloadTools;
import org.finomnis.mcdaemon.tools.FileTools;
import org.finomnis.mcdaemon.tools.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
	
	private static final String downloadServerName = "http://www.creeperrepo.net";
	private static final String packListUrl = downloadServerName + "/static/FTB2/modpacks.xml";
	
	private ConfigFile ftbConfig;
	private FTBStatusFile ftbStatusFile;
	
	private DocumentBuilder xmlDocumentBuilder = null;
	
	private NodeList getModPackList() throws SAXException, IOException, CriticalException
	{
		InputStream is = DownloadTools.openUrl(packListUrl);
		Document modpackList = xmlDocumentBuilder.parse(is);
		is.close();
		return modpackList.getElementsByTagName("modpack");
	}
	
	public List<String> getModPackNames() throws SAXException, IOException, CriticalException{
		
		NodeList modPackList = getModPackList();
		
		List<String> modPackNames = new ArrayList<String>();
		
		for(int i = 0; i < modPackList.getLength(); i++)
		{
			
			Node modpack = modPackList.item(i);
			
			if(modpack.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) modpack;
			
			modPackNames.add(element.getAttribute("name"));
			
		}
		
		return modPackNames;
	
	}
	
	public FTBDownloader() throws ParserConfigurationException
	{
		xmlDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
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
		if(name.startsWith("\""))
			name = name.substring(1);
		if(name.endsWith("\""))
			name = name.substring(0, name.length()-1);
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

		
		if(!FileTools.folderExists(folderName))
			FileTools.createFolder(folderName);
		
		if(!FileTools.fileExists(serverJarName))
			update();
		
		if(!getModpackNameFromConfig().equals(getActiveModpackName()))
			update();
		
	}
	
	public static String getDate() throws IOException, CriticalException {

		InputStream httpStream = DownloadTools.openUrl(downloadServerName + "/getdate");

		Scanner httpScanner = new Scanner(httpStream);

		httpScanner.useDelimiter("\\Z");

		if (!httpScanner.hasNext()) {
			httpScanner.close();
			throw new RuntimeException("Unable to read date!");
		}

		String date = httpScanner.next();

		httpScanner.close();

		return date;

	}
	
	private String getStaticUrlMD5() throws IOException, CriticalException {

		String date = getDate();

		String md5String = "mcepoch1" + date;

		return FileTools.md5(md5String);

	}
	
	private String getModpackUrl(String dir, String version,
			String filename) throws IOException, CriticalException {

		return downloadServerName + "/direct/FTB2/" + getStaticUrlMD5()
				+ "/modpacks%5E" + dir + "%5E" + version + "%5E" + filename;

	}

	protected String[] getModPackInfos(String modpackName) throws ParserConfigurationException, SAXException, IOException, CriticalException{
		
		InputStream is = DownloadTools.openUrl(downloadServerName + "/static/FTB2/modpacks.xml");
		Document modpackList = xmlDocumentBuilder.parse(is);
		is.close();
		
		NodeList modpacks = modpackList.getElementsByTagName("modpack");
		
		for(int i = 0; i < modpacks.getLength(); i++)
		{
			
			Node modpack = modpacks.item(i);
			
			if(modpack.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) modpack;
			
			if(element.getAttribute("name").compareTo(modpackName) != 0)
				continue;
			
			String[] ret = new String[4];
			
			ret[0] = element.getAttribute("dir");
			ret[1] = element.getAttribute("version");
			ret[2] = element.getAttribute("serverPack");
			ret[3] = element.getAttribute("mcVersion");
			
			return ret;
			
		}
		
		throw new CriticalException("Modpack not found!");
		
	}
	
	@Override
	public boolean updateAvailable(){

		if(!FileTools.fileExists(serverJarName))
			return true;
			
		try {
			
			if(!getModpackNameFromConfig().equals(getActiveModpackName()))
				return true;
			
			String[] properties = getModPackInfos(getActiveModpackName());
			
			String version = properties[1];
			if(!version.equals(ftbStatusFile.getConfig("activeModVersion")))
				return true;
			
			return false;
		} catch (Exception e) {
			Log.err(e);
			return false;
		}
		
	}
	
	public static String getModpackMD5(String dir, String version,
			String filename) throws MalformedURLException, IOException, CriticalException {

		String md5Url = downloadServerName + "/md5/FTB2/" + "modpacks%5E" + dir + "%5E"
				+ version + "%5E" + filename;

		Scanner scanner = new Scanner(DownloadTools.openUrl(md5Url));

		scanner.useDelimiter("\\Z");

		if (!scanner.hasNext()) {
			scanner.close();
			throw new RuntimeException("Unable to read md5!");
		}

		String md5 = scanner.next();

		scanner.close();

		return md5;

	}

	@Override
	public void update() throws IOException, CriticalException {
		
		
		try {
			
			String[] properties = getModPackInfos(getModpackNameFromConfig());
			
			String dirName = properties[0];
			String version = properties[1];
			String serverPack = properties[2];
			String mcVersion = properties[3];
			
			String downloadUrl = getModpackUrl(dirName, version, serverPack);
		
			Log.out("Downloading \"" + getModpackNameFromConfig() + " v" + version + " (" + mcVersion + ")\" ...");
			Log.debug("Downloading from \"" + downloadUrl + "\" ...");
			
			/*OutputStream fStream = FileTools.openFileWrite(serverZipName, false);
			InputStream urlStream = DownloadTools.openUrl(downloadUrl);
			FileTools.writeFromStream(urlStream, fStream);
			fStream.close();
			urlStream.close();			
			*/
			Log.debug("Calculating MD5...");
			String zipMd5 = FileTools.md5(new File(serverZipName));
			Log.debug("Zip MD5: '" + zipMd5 + "'");
			Log.debug("Downloading validation MD5 ...");
			String validationMd5 = getModpackMD5(dirName, version, serverPack);
			Log.debug("Val MD5: '" + validationMd5 + "'");
			
			if(!zipMd5.equals(validationMd5)){
				throw new CriticalException("Error downloading Modpack! (MD5 Checksum doesn't fit!");
			}

			if(FileTools.folderExists(folderName + "coremods/"))
				if(!FileTools.delete(folderName + "coremods/"))
					throw new CriticalException("Unable to delete coremods folder!");;
			if(FileTools.folderExists(folderName + "mods/"))
				if(!FileTools.delete(folderName + "mods/"))
					throw new CriticalException("Unable to delete mods folder!");
			
			FileTools.unzip(serverZipName, folderName);
			
			
			
			ftbStatusFile.setConfig("activeMCVersion", mcVersion);
			ftbStatusFile.setConfig("activeModVersion", version);
			ftbStatusFile.setConfig("activeModpack", getModpackNameFromConfig());
			
			
		} catch (ConfigNotFoundException | ParserConfigurationException | SAXException e) {
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
		
		arguments.add("-XX:+UseConcMarkSweepGC");
		arguments.add("-XX:+CMSIncrementalMode");
		arguments.add("-XX:+AggressiveOpts");

		arguments.add("-jar");
		arguments.add(serverName);
		
		
		return arguments;
	}

}
