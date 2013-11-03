package org.finomnis.mcdaemon.downloaders.hexxit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

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
	
	private HexxitStatusFile statusFile; 
	
	private volatile boolean newVersionDownloaded = false;
	private volatile boolean newestVersionInfoFetched = false;
	
	private volatile String[] newestInfos;
	
	public HexxitDownloader()
	{
		statusFile = new HexxitStatusFile(this);
		statusFile.init();
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
			throw new CriticalException("Unable to get download infos!");
		
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
			Log.err(e);
			return false;
		}
		
	
	}

	private void downloadNewVersion() throws MalformedURLException, IOException, CriticalException {
		newVersionDownloaded = false;
		
		if(!newestVersionInfoFetched)
			getNewestVersionInfos();
		
		Log.out("Downloading server archive...");

		InputStream downloadStream = DownloadTools.openUrl(newestInfos[0]);
		OutputStream newZip = FileTools.openFileWrite(downloadName, false);
		FileTools.writeFromStream(downloadStream, newZip);
		downloadStream.close();
		newZip.close();

		if (10 > FileTools.fileSize(downloadName))
			throw new CriticalException("Unable to download server executable!");
		
		newVersionDownloaded = true;
	}

	@Override
	public void update() throws IOException, CriticalException {
		
		if(!newVersionDownloaded)
			downloadNewVersion();

		FileTools.unzip(downloadName, folderName);
		
		try {
			if(newestVersionInfoFetched) statusFile.setConfig("currentlyInstalledVersion", newestInfos[1]);
		} catch (ConfigNotFoundException e) {
			Log.err(e);
		}

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
		arguments.add("--nojline");

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

}
