package org.finomnis.mcdaemon.downloaders.hexxit;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.finomnis.mcdaemon.MCDaemon;
import org.finomnis.mcdaemon.downloaders.MCDownloader;
import org.finomnis.mcdaemon.tools.ConfigNotFoundException;
import org.finomnis.mcdaemon.tools.CrashReportTools;
import org.finomnis.mcdaemon.tools.CriticalException;
import org.finomnis.mcdaemon.tools.DownloadTools;
import org.finomnis.mcdaemon.tools.FileTools;
import org.finomnis.mcdaemon.tools.Log;

public class HexxitDownloader implements MCDownloader {

	private String folderName = "hexxit/";
	private String serverName = "Hexxit.jar";
	private String serverJarName = folderName + serverName;
	private String downloadName = folderName + "hexxit_update.zip";
	private String serverPropertiesName = folderName + "server.properties";
	private String serverPropertiesBackupName = serverPropertiesName + ".backup";
	
	private HexxitStatusFile statusFile; 
	private HexxitConfigFile hexxitConfig;
	
	private volatile boolean newVersionDownloaded = false;
	private volatile boolean newestVersionInfoFetched = false;
	
	private volatile String[] newestInfos;
	
	public HexxitDownloader()
	{
		statusFile = new HexxitStatusFile(this);
		statusFile.init();
		hexxitConfig = new HexxitConfigFile();
		hexxitConfig.init();
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
	public void initialize() throws IOException, CriticalException {
		if (!FileTools.folderExists(folderName))
			FileTools.createFolder(folderName);

		CrashReportTools.removeCrashReports(folderName);
		
		if (!FileTools.fileExists(serverJarName))
			update();
	}
	
	private void getNewestVersionInfos() throws CriticalException, IOException
	{
		newestVersionInfoFetched = false;
		String[] infos = null;
		
		infos = HexxitDownloadTools.fetchNewestVersionInfos();

		if(infos == null)
			throw new CriticalException("Unable to fetch version infos!");
		
		newestInfos = infos;
		
		newestVersionInfoFetched = true;
	}
	
	@Override
	public boolean updateAvailable() {
		
		newVersionDownloaded = false;
		newestVersionInfoFetched = false;
		
		if (!FileTools.fileExists(serverJarName))
			return true;

		try{
			getNewestVersionInfos();
			
			String currentVersion = statusFile.getConfig("currentlyInstalledVersion");
			String newVersion = newestInfos[1];
			
			if(currentVersion.compareTo(newVersion) == 0)
				return false;
			
			downloadNewVersion();
			
			return true;
			
		} catch (Exception e) {
			Log.err("Unable to check for update! (" + e.getMessage() + ")");
			return false;
		}
		
	
	}

	private void downloadNewVersion() throws MalformedURLException, IOException, CriticalException {
		newVersionDownloaded = false;
		
		if(!newestVersionInfoFetched)
			getNewestVersionInfos();
		
		Log.out("Downloading server archive...");

		long downloadSize = DownloadTools.getContentLength(newestInfos[0]);
		InputStream downloadStream = DownloadTools.openUrl(newestInfos[0]);
		OutputStream newZip = FileTools.openFileWrite(downloadName, false);
		FileTools.writeFromStream(downloadStream, newZip, downloadSize);
		downloadStream.close();
		newZip.close();

		if (10 > FileTools.fileSize(downloadName))
			throw new CriticalException("Unable to download server executable!");
		
		newVersionDownloaded = true;
	}

	@Override
	public void update() throws IOException, CriticalException {
		
		// backup server.properties
		if(FileTools.fileExists(serverPropertiesName))
		{
			FileTools.copyFile(serverPropertiesName, serverPropertiesBackupName);
		}
		
		if(!newVersionDownloaded)
			downloadNewVersion();

		FileTools.unzip(downloadName, folderName);

		// revert to previous server.properties
		if(FileTools.fileExists(serverPropertiesBackupName))
		{
			FileTools.copyFile(serverPropertiesBackupName, serverPropertiesName);
		}
		
		try {
			if(newestVersionInfoFetched) statusFile.setConfig("currentlyInstalledVersion", newestInfos[1]);
		} catch (ConfigNotFoundException e) {
			Log.err(e);
		}
		
		setMOTD(newestInfos[1]);

	}
	
	
	private void setMOTD(String message) {

		boolean autoSetMOTDEnabled = false;
		try {
			autoSetMOTDEnabled = Boolean.parseBoolean(hexxitConfig
					.getConfig("autoSetMOTDEnabled"));
		} catch (ConfigNotFoundException e1) {
			Log.err(e1);
		}
		if (!autoSetMOTDEnabled)
			return;
				
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
	public List<String> getInvocationCommand() {

		List<String> arguments = new ArrayList<String>();

		String path = System.getProperty("java.home") + "/bin/java";
		arguments.add(path);

		arguments.add("-server");
		
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
		arguments.add("--nojline");
		arguments.add("nogui");

		return arguments;
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
		if(!newestVersionInfoFetched)
			return null;
		
		return newestInfos[1];
	}

	@Override
	public void prepareStart() {
	}

}
