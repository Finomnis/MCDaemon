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
		return "mcdaemon";
	}

	@Override
	protected String getConfigDescription(String config) {
		switch(config)
		{
		case "mcEdition": 
			return "Determines the Minecraft-Edition. Possible values:\n - vanilla\n - bukkit\n - ftb";
		default:
			return "No description available.";
		}				
	}

	@Override
	protected boolean isValid(String config, String value) {
		switch(config)
		{
		case "mcEdition":
			if(value.equals("ftb") || value.equals("vanilla") || value.equals("bukkit"))
				return true;
			return false;
		default:
			return false;
		}	
	}

}
