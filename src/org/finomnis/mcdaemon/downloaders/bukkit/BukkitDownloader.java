package org.finomnis.mcdaemon.downloaders.bukkit;

import org.finomnis.mcdaemon.MCDaemon;
import org.finomnis.mcdaemon.downloaders.MCDownloader;
import org.finomnis.mcdaemon.tools.ConfigNotFoundException;
import org.finomnis.mcdaemon.tools.CriticalException;
import org.finomnis.mcdaemon.tools.DownloadTools;
import org.finomnis.mcdaemon.tools.FileTools;
import org.finomnis.mcdaemon.tools.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class BukkitDownloader implements MCDownloader {

	private String folderName = "bukkit/";
	private String serverName = "server.jar";
	private String serverJarName = folderName + serverName;
	private String newJarName = serverJarName + ".new";
	private final static String downloadUrl = "http://dl.bukkit.org/latest-rb/craftbukkit.jar";

	@Override
	public void initialize() throws IOException, CriticalException {

		if (!FileTools.folderExists(folderName))
			FileTools.createFolder(folderName);

		if (!FileTools.fileExists(serverJarName))
			update();

	}

	private void downloadNewJar() throws IOException, CriticalException {
		InputStream downloadStream = DownloadTools.openUrl(downloadUrl);
		OutputStream newJar = FileTools.openFileWrite(newJarName, false);
		FileTools.writeFromStream(downloadStream, newJar);
		downloadStream.close();
		newJar.close();

		if (10 > FileTools.fileSize(newJarName))
			throw new CriticalException("Unable to download server jar!");

	}

	@Override
	public boolean updateAvailable() {

		if (!FileTools.fileExists(serverJarName))
			return true;

		try {

			downloadNewJar();

			String newJarMD5 = FileTools.md5(new File(newJarName));

			String oldJarMD5 = FileTools.md5(new File(serverJarName));

			if (newJarMD5.equals(oldJarMD5))
				return false;
			else
				return true;

		} catch (Exception e) {
			Log.err(e);
			return false;
		}

	}

	@Override
	public void update() throws IOException, CriticalException {

		Log.out("Updating server jar.");

		downloadNewJar();

		FileTools.copyFile(newJarName, serverJarName);

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
