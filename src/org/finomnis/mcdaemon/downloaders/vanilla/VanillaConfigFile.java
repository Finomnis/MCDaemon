package org.finomnis.mcdaemon.downloaders.vanilla;

import java.util.Map;

import org.finomnis.mcdaemon.tools.ConfigFile;
import org.finomnis.mcdaemon.tools.ConfigNotFoundException;
import org.finomnis.mcdaemon.tools.CriticalException;

public class VanillaConfigFile extends ConfigFile {

	VanillaDownloader downloader;
	
	public VanillaConfigFile(VanillaDownloader downloader){
		this.downloader = downloader;
	}
	
	@Override
	protected void setDefaultValues(Map<String, String> configs) {
		configs.put("upgradeToSnapshots", "false");
		configs.put("autoSetMOTDEnabled", "true");
	}

	@Override
	protected String getFileName() {
		return "mcdaemon.vanilla.cfg";
	}

	@Override
	protected String getConfigDescription(String config) {
		switch(config)
		{
		case "upgradeToSnapshots":
			return "Includes upgrading to snapshot versions.";
		case "autoSetMOTDEnabled":
			return "Sets MOTD to \"Vanilla Minecraft Server (*version*)\"";
		default:
			return "No description available.";
		}				
	}

	@Override
	protected String[] getValidValues(String config) {
		switch(config)
		{
		case "upgradeToSnapshots":
			return new String[]{":bool:"};
		case "autoSetMOTDEnabled":
			return new String[]{":bool:"};
		default:
			return null;
		}	
	}
	
	public boolean snapshotsEnabled() throws CriticalException{
		try {
			boolean upgradeToSnapshots = Boolean.parseBoolean(
					getConfig("upgradeToSnapshots"));
			return upgradeToSnapshots;
		} catch (ConfigNotFoundException e1) {
			throw new CriticalException("Should never happen!");
		}
	}
	

}
