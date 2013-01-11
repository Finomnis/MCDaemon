package org.finomnis.mcdaemon.automation;

import java.util.Date;

import org.finomnis.mcdaemon.MCDaemon;
import org.finomnis.mcdaemon.server.ServerMonitor;
import org.finomnis.mcdaemon.tools.ConfigNotFoundException;
import org.finomnis.mcdaemon.tools.Log;

public class BackupTask implements Task {

	long timeDiff;
	Date nextUpdate;
	ServerMonitor serverMonitor;
	
	
	private void reniewNextUpdate(){
		Date now = new Date();
		nextUpdate = new Date(now.getTime() + timeDiff*60000);
	}
	
	public BackupTask(ServerMonitor serverMonitor) throws NumberFormatException, ConfigNotFoundException
	{
		this.serverMonitor = serverMonitor;
		timeDiff = Long.parseLong(MCDaemon.getConfig("backupInterval"));
		reniewNextUpdate();
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
		Log.debug("Running backup task...");
		
		if(!serverMonitor.setSaveOff())
			Log.err("Unable to set server to save-off state!");
		
		MCDaemon.runBackup();
		
		serverMonitor.setSaveOn();

	}

}
