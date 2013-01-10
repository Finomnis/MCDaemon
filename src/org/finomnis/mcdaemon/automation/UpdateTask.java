package org.finomnis.mcdaemon.automation;

import java.util.Date;

import org.finomnis.mcdaemon.MCDaemon;
import org.finomnis.mcdaemon.downloaders.MCDownloader;
import org.finomnis.mcdaemon.tools.ConfigNotFoundException;
import org.finomnis.mcdaemon.tools.Log;

public class UpdateTask implements Task {

	long timeDiff;
	Date nextUpdate;
	MCDownloader mcDownloader;
	
	private void reniewNextUpdate(){
		Date now = new Date();
		nextUpdate = new Date(now.getTime() + timeDiff*60000);
	}
	
	public UpdateTask(MCDownloader mcDownloader) throws NumberFormatException, ConfigNotFoundException{
		this.mcDownloader = mcDownloader;
		timeDiff = Long.parseLong(MCDaemon.getConfig("autoPatcherInterval"));
		nextUpdate = new Date(new Date().getTime() + 30000);
	}
	
	@Override
	public Date getScheduleTime() {
		return nextUpdate;
	}

	@Override
	public void generateNewScheduleTime() {
		reniewNextUpdate();
	}

	@Override
	public void run() {
		Log.debug("Checking for update...");
		if(mcDownloader.updateAvailable())
		{
			Log.out("!!!UPDATE!!!");
		}
		else
		{
			Log.debug("Already at newest version.");
		}
	}

}
