package org.finomnis.mcdaemon.downloaders.vanilla;

import org.finomnis.mcdaemon.downloaders.MCDownloader;
import org.finomnis.mcdaemon.tools.FileTools;
import org.finomnis.mcdaemon.tools.Log;

public class VanillaDownloader implements MCDownloader {

	
	
	@Override
	public void initialize() {

		if(!FileTools.folderExists("vanilla"))
			FileTools.createFolder("vanilla");
		
		if(!FileTools.fileExists("vanilla/server.jar"))
			update();
		
	}

	@Override
	public boolean updateAvailable() {

		

		return false;
	}

	@Override
	public void update() {
		
		Log.out("Updating server.jar.");
		
	}

}
