package org.finomnis.mcdaemon.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;



public class FileTools {

	static FileWriter openFileWrite(String path, boolean append) throws IOException
	{
		File f = new File(path);
		File dir = f.getParentFile();
		if(dir != null)
		{
			dir.mkdirs();
		}
		return new FileWriter(f, append);
	}
	
}
