package org.finomnis.mcdaemon.downloaders.bukkit;

import java.util.Map;

import org.finomnis.mcdaemon.tools.ConfigFile;

public class BukkitConfigFile extends ConfigFile {

	@Override
	protected void setDefaultValues(Map<String, String> configs) {
		configs.clear();
		configs.put("patchTo", "Beta");
	}

	@Override
	protected String getFileName() {
		return "mcdaemon.bukkit.cfg";
	}

	@Override
	protected String getConfigDescription(String config) {
		switch(config){
		case "patchTo":
			return "Determines the builds that the patcher will patch to.";
		default:
			return "No description available.";
		}
	}

	@Override
	protected String[] getValidValues(String config) {
		switch(config){
		case "patchTo":
			return new String[]{"Recommended","Beta","Development"};
		default:
			return null;
		}
	}

}
