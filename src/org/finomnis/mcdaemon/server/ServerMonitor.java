package org.finomnis.mcdaemon.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.finomnis.mcdaemon.downloaders.MCDownloader;

public class ServerMonitor implements Runnable{

	private enum Task{
		checkHealth,
		save_off,
		save_on,
		stop
	}
	
	private BlockingQueue<Task> tasks;
	private MCDownloader mcDownloader;
	private ServerWrapper serverWrapper;

	public ServerMonitor(MCDownloader mcDownloader) {
		this.mcDownloader = mcDownloader;
		this.serverWrapper = new ServerWrapper(this.mcDownloader);
		this.tasks = new LinkedBlockingQueue<Task>();
		this.tasks.add(Task.checkHealth);
	}

	@Override
	public void run() {
		
	}
	
	

}
