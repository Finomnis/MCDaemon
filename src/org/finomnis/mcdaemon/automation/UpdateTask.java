package org.finomnis.mcdaemon.automation;

import java.util.Date;

import org.finomnis.mcdaemon.MCDaemon;
import org.finomnis.mcdaemon.tools.ConfigNotFoundException;
import org.finomnis.mcdaemon.tools.Log;

public class UpdateTask implements Task {

	long timeDiff;
	Date nextUpdate;
	
	private void reniewNextUpdate(){
		Date now = new Date();
		nextUpdate = new Date(now.getTime() + timeDiff*60000);
	}
	
	public UpdateTask() throws NumberFormatException, ConfigNotFoundException{
		timeDiff = Long.parseLong(MCDaemon.getConfig("autoPatcherInterval"));
		nextUpdate = new Date();
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
		// TODO Auto-generated method stub
		Log.out("!!!UPDATE!!!");
	}

}
