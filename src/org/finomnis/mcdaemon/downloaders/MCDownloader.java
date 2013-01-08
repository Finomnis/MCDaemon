package org.finomnis.mcdaemon.downloaders;

import java.io.IOException;

import org.finomnis.mcdaemon.tools.CriticalException;

public interface MCDownloader {

	public void initialize() throws IOException, CriticalException;
	public boolean updateAvailable();
	public void update() throws IOException, CriticalException;
	
}
