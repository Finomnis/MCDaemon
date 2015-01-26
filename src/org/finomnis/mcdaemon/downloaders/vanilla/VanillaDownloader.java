package org.finomnis.mcdaemon.downloaders.vanilla;

import org.finomnis.mcdaemon.MCDaemon;
import org.finomnis.mcdaemon.downloaders.MCDownloader;
import org.finomnis.mcdaemon.tools.ConfigNotFoundException;
import org.finomnis.mcdaemon.tools.CrashReportTools;
import org.finomnis.mcdaemon.tools.CriticalException;
import org.finomnis.mcdaemon.tools.DownloadTools;
import org.finomnis.mcdaemon.tools.FileTools;
import org.finomnis.mcdaemon.tools.Log;
import org.finomnis.mcdaemon.tools.MinecraftTools;
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

import javax.xml.parsers.ParserConfigurationException;

public class VanillaDownloader implements MCDownloader {
	private String folderName = "vanilla/";
	private String serverName = "server.jar";
	private String serverJarName = folderName + serverName;
	private String serverNewJarName = folderName + "download.jar";

	private VanillaConfigFile vanillaConfig;
	private VanillaStatusFile vanillaStatusFile;

	private boolean updatePrepared = false;

	private String update_version;
	
	private VanillaVersions newestAvailableVersion;
	
	private void prepareUpdate() throws IOException, CriticalException,
			ParserConfigurationException, SAXException {
		updatePrepared = false;

		if(newestAvailableVersion == null)
			newestAvailableVersion = VanillaVersions.fetch();
		
		update_version = newestAvailableVersion.getVersion(vanillaConfig.snapshotsEnabled());
		
		String downloadUrl = VanillaVersions.getDownloadLink(update_version);

		Log.out("Downloading Minecraft " + update_version + " ...");
		Log.debug("Downloading from \"" + downloadUrl + "\" ...");

		OutputStream fStream = FileTools.openFileWrite(serverNewJarName, false);
		long downloadSize = DownloadTools.getContentLength(downloadUrl);
		InputStream urlStream = DownloadTools.openUrl(downloadUrl);
		FileTools.writeFromStream(urlStream, fStream, downloadSize);
		fStream.close();
		urlStream.close();

		Log.debug("Calculating MD5 ...");
		String jarMd5 = FileTools.md5(new File(serverNewJarName));
		Log.debug("Jar MD5: '" + jarMd5 + "'");
		Log.debug("Downloading validation MD5 ...");
		String validationMd5 = getServerJarMD5(update_version);
		if(validationMd5 != null){
			
			Log.debug("Val MD5: '" + validationMd5 + "'");
		
			if (!jarMd5.equals(validationMd5)) {
				throw new CriticalException(
						"Error downloading Modpack! (MD5 Checksum doesn't fit!)");
			}
			
		} else {
			
			Log.err("Unable to retrieve Validation MD5! Validating Modpack by size ...");
		
			Log.debug("Calculating size ...");
			long jarSize = FileTools.fileSize(serverNewJarName);
			Log.debug("Jar Size: " + jarSize);
			Log.debug("Downloading validation size ...");
			long validationSize = getServerJarSize(update_version);
			Log.debug("Val Size: " + validationSize);
						
			if (jarSize != validationSize) {
				throw new CriticalException(
						"Error downloading Modpack! (Size comparison failed!)");
			}
			
		}		
		
		updatePrepared = true;
	}

	public VanillaDownloader() throws ParserConfigurationException {
		vanillaConfig = new VanillaConfigFile(this);
		vanillaConfig.init();
		vanillaStatusFile = new VanillaStatusFile(this);
		vanillaStatusFile.init();
	}

	@Override
	public void initialize() throws IOException, CriticalException {

		if (!FileTools.folderExists(folderName))
			FileTools.createFolder(folderName);

		CrashReportTools.removeCrashReports(folderName);

		if (!FileTools.fileExists(serverJarName))
			update();

		try {
			String version = vanillaStatusFile.getConfig("activeVersion");
			setMOTD(version);
		} catch (ConfigNotFoundException e) {
			Log.err(e);
		}

	}

	private boolean checkUpdateAvailable() {
		
		newestAvailableVersion = null;
		
		if (!FileTools.fileExists(serverJarName))
			return true;

		try {

			newestAvailableVersion = VanillaVersions.fetch();
			
			String version = newestAvailableVersion.getVersion(vanillaConfig.snapshotsEnabled());
			
			if (!version.equals(vanillaStatusFile.getConfig("activeVersion")))
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

	private static String getServerJarMD5(String version) throws MalformedURLException, IOException,
			CriticalException {

		String serverJarUrl = VanillaVersions.getDownloadLink(version);

		return DownloadTools.getContentMD5(serverJarUrl);

	}

	private static long getServerJarSize(String version) throws MalformedURLException, IOException,
			CriticalException {
		
		String serverJarUrl = VanillaVersions.getDownloadLink(version);

		return DownloadTools.getContentLength(serverJarUrl);
		
	}
	
	@Override
	public void update() throws IOException, CriticalException {

		try {

			if (!updatePrepared)
				prepareUpdate();
			updatePrepared = false;

			Log.out("Extracting server files...");
			FileTools.copyFile(serverNewJarName, serverJarName);
			
			vanillaStatusFile.setConfig("activeVersion", update_version);
			setMOTD(update_version);

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

		arguments.add("-XX:+UseConcMarkSweepGC");
		arguments.add("-XX:+CMSIncrementalMode");
		arguments.add("-XX:+AggressiveOpts");

		arguments.add("-jar");
		arguments.add(serverName);
		arguments.add("nogui");

		return arguments;
	}

	private void setMOTD(String version) {

		boolean autoSetMOTDEnabled = false;
		try {
			autoSetMOTDEnabled = Boolean.parseBoolean(vanillaConfig
					.getConfig("autoSetMOTDEnabled"));
		} catch (ConfigNotFoundException e1) {
			Log.err(e1);
		}
		if (!autoSetMOTDEnabled)
			return;

		String message = "Vanilla Minecraft Server (" + version + ")";

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
		return "Minecraft " + update_version;
	}

	@Override
	public void prepareStart() {
		MinecraftTools.agreeToEula(folderName);
	}

}
