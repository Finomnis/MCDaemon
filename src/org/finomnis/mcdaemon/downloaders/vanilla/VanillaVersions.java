package org.finomnis.mcdaemon.downloaders.vanilla;

import org.finomnis.mcdaemon.tools.CriticalException;
import org.finomnis.mcdaemon.tools.DownloadTools;
import org.finomnis.mcdaemon.tools.FileTools;
import org.finomnis.mcdaemon.tools.Log;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class VanillaVersions {

	private JSONObject versionsJson;
	
	private VanillaVersions() throws CriticalException{
		
		versionsJson = null;
		
		try{
			String jsonVersionFileRaw = FileTools.readStreamToString(DownloadTools.openUrl("https://s3.amazonaws.com/Minecraft.Download/versions/versions.json"));
			versionsJson = (JSONObject) JSONValue.parse(jsonVersionFileRaw);
		} catch (Exception e){
			Log.err(e);
			versionsJson = null;
		}
		
		if(versionsJson == null){
			throw new CriticalException("Unable to download list of available minecraft versions!");
		}
		
		if(!versionsJson.containsKey("latest")){
			throw new CriticalException("Unable to download list of available minecraft versions! (Unexpected document structure)");
		}
		
	}

	public static VanillaVersions fetch() throws CriticalException{
		
		return new VanillaVersions();
		
	}
	
	public String getVersion(boolean enableSnapshot) throws CriticalException{
		
		String versionType = "release";
		if(enableSnapshot)
			versionType = "snapshot";
		
		// Fetch the actual version if latest version is requested
		JSONObject latest = (JSONObject) versionsJson.get("latest");
		if(!latest.containsKey(versionType))
			throw new CriticalException("Versiontype '" + versionType + "' does not have a version 'latest'!");
		
		String version = (String) latest.get(versionType);
		
		return version;
		
	}
	
	public static String getDownloadLink(String version) throws CriticalException{
		
		if(version == null)
			throw new IllegalArgumentException();
		
		return "https://s3.amazonaws.com/Minecraft.Download/versions/" + version + "/minecraft_server." + version + ".jar";

	}
	
}
