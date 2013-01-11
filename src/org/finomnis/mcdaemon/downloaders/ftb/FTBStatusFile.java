package org.finomnis.mcdaemon.downloaders.ftb;

import java.util.Map;

import org.finomnis.mcdaemon.tools.ConfigFile;

public class FTBStatusFile extends ConfigFile {

	FTBDownloader downloader;
	
	public FTBStatusFile(FTBDownloader downloader){
		this.downloader = downloader;
	}
	
	@Override
	protected void setDefaultValues(Map<String, String> configs) {
		configs.clear();
		configs.put("activeModpack", "null");
		configs.put("activeModVersion", "-1");
		configs.put("activeMCVersion", "null");
	}

	@Override
	protected String getFileName() {
		return downloader.getFolderName() + "status.cfg";
	}

	@Override
	protected String getConfigDescription(String config) {
		return "DO NOT CHANGE!";
	}

	@Override
	protected String[] getValidValues(String config) {
		// TODO Auto-generated method stub
		return null;
	}

}
