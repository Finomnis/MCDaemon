package org.finomnis.mcdaemon.downloaders.vanilla;

import java.util.Map;

import org.finomnis.mcdaemon.tools.ConfigFile;

public class VanillaStatusFile extends ConfigFile {

	VanillaDownloader downloader;
	
	public VanillaStatusFile(VanillaDownloader downloader){
		this.downloader = downloader;
	}
	
	@Override
	protected void setDefaultValues(Map<String, String> configs) {
		configs.clear();
		configs.put("activeVersion", "-1");
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
