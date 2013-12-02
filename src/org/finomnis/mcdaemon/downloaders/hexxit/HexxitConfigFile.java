package org.finomnis.mcdaemon.downloaders.hexxit;

import java.util.Map;

import org.finomnis.mcdaemon.tools.ConfigFile;

public class HexxitConfigFile extends ConfigFile {

	@Override
	protected void setDefaultValues(Map<String, String> configs) {
		configs.put("autoSetMOTDEnabled", "true");
	}

	@Override
	protected String getFileName() {
		return "mcdaemon.hexxit.cfg";
	}

	@Override
	protected String getConfigDescription(String config) {
		switch(config)
		{
		case "autoSetMOTDEnabled":
			return "Sets MOTD to display the current Hexxit Server Version";
		default:
			return "No description available.";
		}			
	}

	@Override
	protected String[] getValidValues(String config) {
		switch(config)
		{
		case "autoSetMOTDEnabled":
			return new String[]{":bool:"};
		default:
			return null;
		}	
	}

}
