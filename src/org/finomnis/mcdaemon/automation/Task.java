package org.finomnis.mcdaemon.automation;

import java.util.Date;

public interface Task {
	
	Date getScheduleTime();
	void generateNewScheduleTime();
	void run();

}
