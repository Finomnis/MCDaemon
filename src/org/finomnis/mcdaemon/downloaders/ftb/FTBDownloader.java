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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class FTBDownloader implements MCDownloader {
	private String folderName = "ftb/";
	private String serverName = "server.jar";
	private String serverJarName = folderName + serverName;
	private String newJarName = serverJarName + ".new";
	
	private static final String downloadServerName = "http://www.creeperrepo.net";
	private static final String packListUrl = downloadServerName + "/static/FTB2/modpacks.xml";
	
	private ConfigFile ftbConfig;
	
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
	}
	
	@Override
	public void initialize() throws IOException, CriticalException {

		
		if(!FileTools.folderExists(folderName))
			FileTools.createFolder(folderName);
		
		if(!FileTools.fileExists(serverJarName))
			update();
		
	}

	
	@Override
	public boolean updateAvailable(){

		if(!FileTools.fileExists(serverJarName))
			return true;
			
		try {
			
			// TODO
			
			return false;
		} catch (Exception e) {
			Log.err(e);
			return false;
		}
		
	}

	@Override
	public void update() throws IOException, CriticalException {
		
		// TODO
		
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
