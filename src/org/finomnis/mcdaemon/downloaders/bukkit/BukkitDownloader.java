package org.finomnis.mcdaemon.downloaders.bukkit;

import org.finomnis.mcdaemon.MCDaemon;
import org.finomnis.mcdaemon.downloaders.MCDownloader;
import org.finomnis.mcdaemon.downloaders.bukkit.BukkitDownloadTools.BukkitEdition;
import org.finomnis.mcdaemon.tools.ConfigNotFoundException;
import org.finomnis.mcdaemon.tools.CrashReportTools;
import org.finomnis.mcdaemon.tools.CriticalException;
import org.finomnis.mcdaemon.tools.DownloadTools;
import org.finomnis.mcdaemon.tools.FileTools;
import org.finomnis.mcdaemon.tools.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

public class BukkitDownloader implements MCDownloader {

	private String folderName = "bukkit/";
	private String serverName = "server.jar";
	private String serverJarName = folderName + serverName;
	private String newJarName = serverJarName + ".new";
	
	private BukkitStatusFile statusFile;
	private BukkitConfigFile configFile;
	
	private volatile boolean newJarDownloaded = false;
	private volatile boolean newestVersionInfoFetched = false;
	
	private String[] newestInfos;
	
	public BukkitDownloader()
	{
		statusFile = new BukkitStatusFile(this);
		statusFile.init();
		configFile = new BukkitConfigFile();
		configFile.init();
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
		int buildNumber = 0;
		
		Set<BukkitEdition> editions = new HashSet<BukkitEdition>();
		
		try {
			switch(configFile.getConfig("patchTo"))
			{
			case "Recommended":
				editions.add(BukkitEdition.Recommended);
				break;
			case "Beta":
				editions.add(BukkitEdition.Recommended);
				editions.add(BukkitEdition.Beta);
				break;
			case "Development":
				editions.add(BukkitEdition.Recommended);
				editions.add(BukkitEdition.Beta);
				editions.add(BukkitEdition.Development);
				break;
			default:
				throw new CriticalException("Invalid value of config 'patchTo'!");
			}
		} catch (ConfigNotFoundException e1) {
			Log.err(e1);
			throw new CriticalException("Unable to read 'patchTo'-config!");
		}
		
		try {
			for(BukkitEdition edition : editions)
			{
				String[] editionInfos = BukkitDownloadTools.fetchNewestVersionInfos(edition);
				int editionBuildNumber = Integer.parseInt(editionInfos[2]);	
				if(editionBuildNumber > buildNumber)
				{
					buildNumber = editionBuildNumber;
					infos = editionInfos;
				}
			}
		} catch (ParserConfigurationException e) {
			Log.err(e);
			throw new CriticalException("Unable to get newestVersionInfo!");
		}

		if(infos == null)
			throw new CriticalException("Unable to get download infos!");
		
		newestInfos = infos;
		
		newestVersionInfoFetched = true;
	}
	
	private void downloadNewJar() throws IOException, CriticalException {
		newJarDownloaded = false;
		
		if(!newestVersionInfoFetched)
			getNewestVersionInfos();
		
		Log.out("Downloading server executable...");

		long downloadSize = DownloadTools.getContentLength(newestInfos[0]);
		InputStream downloadStream = DownloadTools.openUrl(newestInfos[0]);
		OutputStream newJar = FileTools.openFileWrite(newJarName, false);
		FileTools.writeFromStream(downloadStream, newJar, downloadSize);
		downloadStream.close();
		newJar.close();

		if (10 > FileTools.fileSize(newJarName))
			throw new CriticalException("Unable to download server executable!");

		String fileMD5 = FileTools.md5(new File(newJarName));
		if(!fileMD5.equals(newestInfos[1]))
			throw new CriticalException("MD5-Check failed!");
		
		newJarDownloaded = true;
	}

	@Override
	public boolean updateAvailable() {

		newJarDownloaded = false;
		newestVersionInfoFetched = false;
		
		if (!FileTools.fileExists(serverJarName))
			return true;

		try {

			getNewestVersionInfos();
			
			int oldNumber = Integer.parseInt(statusFile.getConfig("newestServerBuild"));
			int newNumber = Integer.parseInt(newestInfos[2]);
			Log.debug("Comparing version numbers ... (Old: " + oldNumber + "; New: " + newNumber + ")");
			
			if(newNumber == oldNumber)
				return false;
			
			String newJarMD5 = newestInfos[1];
			String oldJarMD5 = FileTools.md5(new File(serverJarName));
			Log.debug("Comparing md5 ... (Old: '" + oldJarMD5 + "'; New: '" + newJarMD5 + "')");
			
			if (newJarMD5.equals(oldJarMD5))
			{
				statusFile.setConfig("newestServerBuild", ""+newNumber);
				return false;
			}
			
			downloadNewJar();
			
			return true;
			
		} catch (Exception e) {
			Log.err(e);
			return false;
		}

	}

	@Override
	public void update() throws IOException, CriticalException {

		
		if(!newJarDownloaded)
			downloadNewJar();

		FileTools.copyFile(newJarName, serverJarName);
		
		try {
			if(newestVersionInfoFetched) statusFile.setConfig("newestServerBuild", newestInfos[2]);
		} catch (ConfigNotFoundException e) {
			Log.err(e);
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
		
		return newestInfos[3];
	}

	@Override
	public void prepareStart() {
		
	}
}
