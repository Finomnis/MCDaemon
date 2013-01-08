package org.finomnis.mcdaemon;

import java.util.Map;

import org.finomnis.mcdaemon.tools.ConfigFile;

public class MainConfigFile extends ConfigFile {

	@Override
	protected void setDefaultValues(Map<String, String> configs) {
		configs.put("mcEdition", "vanilla");
		configs.put("backupEnabled", "true");
		configs.put("backupInterval", "720");
		configs.put("backupScript", "\"backup.sh\"");
		configs.put("autoPatcherInterval", "30");
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
			return "Which server to download.";
		case "backupEnabled":
			return "Enables/disables autobackup.";
		case "backupInterval":
			return "In which intervall to backup. (in minutes)";
		case "backupScript":
			return "Location of the backup-script";
		case "autoPatcherInterval":
			return "In which intervall to update. (in minutes)";
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
		case "backupEnabled":
			return new String[]{":bool:"};
		case "backupInterval":
			return new String[]{":int:"};
		case "backupScript":
			return new String[]{":path:"};
		case "autoPatcherInterval":
			return new String[]{":int:"};
		default:
			return null;
		}	
	}

}
