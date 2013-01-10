package org.finomnis.mcdaemon.downloaders.ftb;

import java.util.List;
import java.util.Map;

import org.finomnis.mcdaemon.tools.ConfigFile;
import org.finomnis.mcdaemon.tools.Log;

public class FTBConfigFile extends ConfigFile {

	private FTBDownloader downloader;
	
	
	public FTBConfigFile(FTBDownloader downloader)
	{
		this.downloader = downloader;
	}
	
	@Override
	protected void setDefaultValues(Map<String, String> configs) {
		configs.put("modpackName", "\"MindCrack Pack\"");
	}

	@Override
	protected String getFileName() {
		return "mcdaemon.ftb.cfg";
	}

	@Override
	protected String getConfigDescription(String config) {
		switch(config)
		{
		case "modpackName": 
			return "Which modpack to download";
		default:
			return "No description available.";
		}				
	}

	@Override
	protected String[] getValidValues(String config) {
		switch(config)
		{
		case "modpackName":
			try{
				List<String> validNamesList = downloader.getModPackNames();
				String[] validNames = new String[validNamesList.size()];
				for(int i = 0; i < validNames.length; i++)
				{
					validNames[i] = "\"" + validNamesList.get(i) + "\"";
				}
				return validNames;
			} catch (Exception e) {
				Log.warn(e);
				return new String[]{":string:"};
			}
		default:
			return null;
		}	
	}
	

}
