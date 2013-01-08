package org.finomnis.mcdaemon.downloaders.vanilla;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.finomnis.mcdaemon.downloaders.MCDownloader;
import org.finomnis.mcdaemon.tools.CriticalException;
import org.finomnis.mcdaemon.tools.DownloadTools;
import org.finomnis.mcdaemon.tools.FileTools;
import org.finomnis.mcdaemon.tools.Log;

public class VanillaDownloader implements MCDownloader {

	private final static String newJarName = "server.jar.new";
	private final static String downloadUrl = "https://s3.amazonaws.com/MinecraftDownload/launcher/minecraft_server.jar";
	
	@Override
	public void initialize() throws IOException, CriticalException {

		if(!FileTools.folderExists("vanilla"))
			FileTools.createFolder("vanilla");
		
		if(!FileTools.fileExists("vanilla/server.jar"))
			update();
		
	}

	private void downloadNewJar() throws IOException, CriticalException
	{
		InputStream downloadStream = DownloadTools.openUrl(downloadUrl);
		OutputStream newJar = FileTools.openFileWrite("vanilla/" + newJarName, false);
		FileTools.writeFromStream(downloadStream, newJar);
		downloadStream.close();
		newJar.close();
		
		if(10 > FileTools.fileSize("vanilla/" + newJarName))
			throw new CriticalException("Unable to download server jar!");
				
	}
	
	@Override
	public boolean updateAvailable(){

		if(!FileTools.fileExists("vanilla/server.jar"))
			return true;
			
		try {
			
			downloadNewJar();

			String newJarMD5 = FileTools.md5(new File("vanilla/" + newJarName));

			String oldJarMD5 = FileTools.md5(new File("vanilla/server.jar"));

			if(newJarMD5.equals(oldJarMD5))
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
		
		Log.out("Updating server.jar.");
		
		downloadNewJar();
		
		FileTools.copyFile("vanilla/" + newJarName, "vanilla/server.jar");
		
	}

}
