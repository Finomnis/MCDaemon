package org.finomnis.mcdaemon.downloaders;

import java.io.IOException;

import java.util.List;
import org.finomnis.mcdaemon.tools.CriticalException;

public interface MCDownloader {

	public String getFolderName();
	public String getServerJarName();
	public void initialize() throws IOException, CriticalException;
	public boolean updateAvailable();
	public void update() throws IOException, CriticalException;
	public List<String> getInvocationCommand();
	public boolean runEditionSpecificCrashTest();
	
}
