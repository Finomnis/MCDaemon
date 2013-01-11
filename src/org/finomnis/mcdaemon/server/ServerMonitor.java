package org.finomnis.mcdaemon.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.finomnis.mcdaemon.MCDaemon;
import org.finomnis.mcdaemon.downloaders.MCDownloader;
import org.finomnis.mcdaemon.server.wrapper.ServerWrapper;
import org.finomnis.mcdaemon.tools.ConfigNotFoundException;
import org.finomnis.mcdaemon.tools.Log;

public class ServerMonitor implements Runnable {

	private enum Task {
		checkHealth, save_off, save_on, restart, stop
	}

	private BlockingQueue<Task> tasks;
	private MCDownloader mcDownloader;
	private ServerWrapper serverWrapper;

	public ServerMonitor(MCDownloader mcDownloader) {
		this.mcDownloader = mcDownloader;
		this.serverWrapper = new ServerWrapper(this.mcDownloader, this);
		this.tasks = new LinkedBlockingQueue<Task>();
		this.tasks.add(Task.checkHealth);
	}

	@Override
	public void run() {
		Task task;
		while (true) {
			try {
				task = tasks.take();
			} catch (InterruptedException e) {
				Log.warn(e);
				continue;
			}

			switch (task) {
			case stop:
				Log.debug("serverMonitor caught stopmessage.");
				serverWrapper.shutdown();
				return;
			case checkHealth:
				Log.debug("Running health check...");
				switch (serverWrapper.getStatus()) {
				case stopped:
					Log.out("Server seems to not run. Starting server ...");
					tryStartServer();
					break;
				case starting:
					if(serverWrapper.getServerInactiveTime() > 60000) {
						Log.out("Server seems like it crashed during startup. Restarting server ...");
						serverWrapper.stopServer();
						tryStartServer();
					} else {
						Log.debug("Server seems to be starting without error.");
					}
					break;
				case running:
					boolean seedCheckEnabled = false;
					try {
						seedCheckEnabled = Boolean.parseBoolean(MCDaemon.getConfig("seedCrashTestEnabled"));
					} catch (ConfigNotFoundException e) {
						Log.warn(e);
					}
					if (seedCheckEnabled) {
						if (!serverWrapper.stillAliveTest()) {
							Log.out("Server seems like it crashed. Restarting server ...");
							serverWrapper.stopServer();
							tryStartServer();
						} else {
							Log.debug("Server seems running and healthy.");
						}
					} else {
						Log.debug("Server seems running.");
					}
					break;
				default:
					break;
				}
				continue;
			case restart:
				serverWrapper.stopServer();
				tryStartServer();
			default:
				continue;
			}

		}
	}

	private void tryStartServer() {
		try {
			serverWrapper.startServer();
		} catch (Exception e) {
			Log.err("Unable to start server!");
			Log.err(e);
		}
	}

	public void requestHealthCheck() {
		if (!tasks.contains(Task.checkHealth))
			tasks.add(Task.checkHealth);
	}

	public void initShutdown() {
		tasks.clear();
		tasks.add(Task.stop);
	}

	public ServerWrapper getWrapper() {
		return serverWrapper;
	}

}
