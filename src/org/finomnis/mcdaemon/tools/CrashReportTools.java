package org.finomnis.mcdaemon.tools;

import java.io.File;

public class CrashReportTools {

	
	public static boolean crashReportExists(String serverFolderName) {
		Log.debug("Searching for crash reports...");
		
		File crashReportsDir = new File(serverFolderName + "crash-reports/");
		if (!(crashReportsDir.exists() && crashReportsDir.isDirectory()))
			return false;
		File[] subFiles = crashReportsDir.listFiles();

		for (File file : subFiles) {
			if (file.isDirectory())
				continue;
			if (file.getName().endsWith(".txt"))
			{
				Log.out("Crash report found!");
				return true;
			}
		}

		return false;
	}

	public static void removeCrashReports(String serverFolderName) {
		Log.debug("Removing crash reports...");
		try {
			File processedDir = new File(serverFolderName
					+ "processed-crash-reports/");
			processedDir.mkdirs();

			File crashReportsDir = new File(serverFolderName + "crash-reports/");
			if (!(crashReportsDir.exists() && crashReportsDir.isDirectory()))
				return;
			File[] subFiles = crashReportsDir.listFiles();

			for (File file : subFiles) {
				try {
					if (file.isDirectory())
						continue;
					if (file.getName().endsWith(".txt")) {
						Log.debug("Removing crash report \"" + file.getName() + "\"");
						File newFile = new File(serverFolderName + "processed-crash-reports/" + file.getName());
						if(newFile.exists())
							file.delete();
						else
							file.renameTo(newFile);
					}
				} catch (Exception e) {
					Log.err(e);
				}
			}

		} catch (Exception e) {
			Log.err(e);
		}

	}
}
