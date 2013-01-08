package org.finomnis.mcdaemon.downloaders;

public interface MCDownloader {

	public void initialize();
	public boolean updateAvailable();
	public void update();
	
}
