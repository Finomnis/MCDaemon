package org.finomnis.mcdaemon.automation;

import java.util.Date;

import org.finomnis.mcdaemon.MCDaemon;
import org.finomnis.mcdaemon.tools.ConfigNotFoundException;

public class HealthCheckTask implements Task {

	long timeDiff = 30;
	Date nextUpdate;

	private void reniewNextUpdate() {
		Date now = new Date();
		nextUpdate = new Date(now.getTime() + timeDiff * 1000);
	}

	public HealthCheckTask() throws NumberFormatException, ConfigNotFoundException {
		timeDiff = Integer.parseInt(MCDaemon.getConfig("crashTestInterval"));
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
		MCDaemon.scheduleHealthCheck();
	}

}
