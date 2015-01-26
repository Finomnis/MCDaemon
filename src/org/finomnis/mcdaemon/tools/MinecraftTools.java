package org.finomnis.mcdaemon.tools;

import java.io.IOException;

public class MinecraftTools {
	
	public static void agreeToEula(String folderName){
		
		if(FileTools.fileExists(folderName + "eula.txt")){
			try {
				Log.debug("Agreeing to eula...");
				FileTools.replaceInFile(folderName + "eula.txt", "eula=false", "eula=true");
			} catch (IOException e) {
				Log.err(e);
			}
		}
		
	}
	
}
