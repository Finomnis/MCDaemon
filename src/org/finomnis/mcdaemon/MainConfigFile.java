package org.finomnis.mcdaemon;

import java.util.Map;

import org.finomnis.mcdaemon.tools.ConfigFile;

public class MainConfigFile extends ConfigFile {

	@Override
	protected void setDefaultValues(Map<String, String> configs) {
		configs.put("mcEdition", "ftb");
	}

	@Override
	protected String getFileName() {
		return "mcdaemon.cfg";
	}

	@Override
	protected String getConfigDescription(String config) {
		switch(config)
		{
		case "mcEdition": 
			return "Determines the Minecraft-Edition.";
		default:
			return "No description available.";
		}				
	}

	@Override
	protected String[] getValidValues(String config) {
		switch(config)
		{
		case "mcEdition":
			return new String[]{"ftb", "vanilla", "bukkit"};
		default:
			return null;
		}	
	}

}
