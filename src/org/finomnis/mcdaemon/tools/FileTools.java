package org.finomnis.mcdaemon.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class FileTools {

	static FileWriter openFileWrite(String path, boolean append)
			throws IOException {
		File f = new File(path);
		File dir = f.getParentFile();
		if (dir != null) {
			dir.mkdirs();
		}
		return new FileWriter(f, append);
	}
	
	static InputStream openFileRead(String path) throws FileNotFoundException
	{
		File f = new File(path);
		return new FileInputStream(f);
	}

	public static boolean fileExists(String path) {
		try {
			File f = new File(path);
			return f.exists() && !f.isDirectory();
		} catch (Exception e) {
			Log.warn(e);
			return false;
		}
	}

	public static boolean folderExists(String path) {
		try {
			File f = new File(path);
			return f.exists() && f.isDirectory();
		} catch (Exception e) {
			Log.warn(e);
			return false;
		}
	}

}
