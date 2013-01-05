package org.finomnis.mcdaemon.downloaders;

public interface MCDownloader {

	public boolean isInitialized();
	public void initialize();
	public boolean updateAvailable();
	public void update();
	
}
