package org.finomnis.mcdaemon.downloaders.hexxit;

import java.util.Map;

import org.finomnis.mcdaemon.tools.ConfigFile;

public class HexxitStatusFile extends ConfigFile {

	HexxitDownloader downloader;
	
	public HexxitStatusFile(HexxitDownloader downloader)
	{
		this.downloader = downloader;
	}
	
	@Override
	protected void setDefaultValues(Map<String, String> configs) {
		configs.clear();
		configs.put("currentlyInstalledVersion", "-1");
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
		return null;
	}

}
