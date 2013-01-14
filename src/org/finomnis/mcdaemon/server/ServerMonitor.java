package org.finomnis.mcdaemon.server;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.finomnis.mcdaemon.MCDaemon;
import org.finomnis.mcdaemon.downloaders.MCDownloader;
import org.finomnis.mcdaemon.server.wrapper.ServerWrapper;
import org.finomnis.mcdaemon.server.wrapper.ServerWrapper.Status;
import org.finomnis.mcdaemon.tools.ConfigNotFoundException;
import org.finomnis.mcdaemon.tools.Log;
import org.finomnis.mcdaemon.tools.SyncVar;

public class ServerMonitor implements Runnable {

	private static long startupTimeout = 120;

	private enum Task {
		checkHealth, save_off, save_on, restart, shutdown
	}

	private BlockingQueue<Task> tasks;
	private MCDownloader mcDownloader;
	private ServerWrapper serverWrapper;

	private volatile Status targetStatus = Status.running;
	private volatile SyncVar<Status> acceptedStatus = new SyncVar<Status>(
			Status.running);

	public ServerMonitor(MCDownloader mcDownloader) {
		try {
			startupTimeout = Integer.parseInt(MCDaemon
					.getConfig("crashTestStartupTimeout"));
		} catch (NumberFormatException | ConfigNotFoundException e) {
			Log.warn(e);
		}
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
			case shutdown:
				Log.debug("serverMonitor caught stopmessage.");
				serverWrapper.shutdown();
				return;
			case checkHealth:
				Log.debug("Running health check...");
				if (targetStatus == Status.running) {
					ensureServerRunning();
					acceptedStatus.set(Status.running);
				} else {
					ensureServerStopped();
					acceptedStatus.set(Status.stopped);
				}
				continue;
			case restart:
				serverWrapper.stopServer();
				if (targetStatus == Status.running)
					tryStartServer();
			default:
				continue;
			}

		}
	}

	private void ensureServerStopped() {

		if (serverWrapper.getStatus() != Status.stopped)
			serverWrapper.stopServer();

	}

	public void enterMaintenanceMode() {

		targetStatus = Status.stopped;
		requestHealthCheck();
		acceptedStatus.waitForValue(Status.stopped);

	}

	public void exitMaintenanceMode() {

		targetStatus = Status.running;
		requestHealthCheck();

	}

	private void ensureServerRunning() {

		switch (serverWrapper.getStatus()) {
		case stopped:
			Log.out("Server seems to not run.");
			tryStartServer();
			break;
		case starting:
			if (serverWrapper.getServerInactiveTime() > startupTimeout * 1000
					|| mcDownloader.runEditionSpecificCrashTest()) {
				Log.out("Server seems like it crashed during startup. Restarting server ...");
				serverWrapper.stopServer();
				tryStartServer();
			} else {
				Log.debug("Server seems to be starting without error.");
			}
			break;
		case running:
			boolean seedCheckEnabled = false;
			boolean serverCrashed = false;
			
			if (mcDownloader.runEditionSpecificCrashTest()) {
				serverCrashed = true;
			} else {

				try {
					seedCheckEnabled = Boolean.parseBoolean(MCDaemon
							.getConfig("seedCrashTestEnabled"));
				} catch (ConfigNotFoundException e) {
					Log.warn(e);
				}
				if (seedCheckEnabled) {
					if (!serverWrapper.stillAliveTest()) {
						serverCrashed = true;
					}
				}
			}

			if(serverCrashed)
			{
				Log.out("Server seems like it crashed. Restarting server ...");
				serverWrapper.stopServer();
				tryStartServer();
			}
			else
			{
				Log.debug("Server seems running and healthy.");
			}
			
			break;
		default:
			break;
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
		tasks.add(Task.shutdown);
	}

	public ServerWrapper getWrapper() {
		return serverWrapper;
	}

	public boolean setSaveOff() {
		return serverWrapper.setSaveOff();
	}

	public void setSaveOn() {
		// Additional Healthcheck
		Date startDate = new Date();
		Status serverStatus = serverWrapper.getStatus();
		// Save-off check at save-on function to prevent restarting of server
		// while backing up
		if (serverWrapper.getSaveOff() == false
				&& serverStatus == Status.running
				&& serverWrapper.getLastStatusChangeDate().getTime() < startDate
						.getTime() - 10000) {
			Log.out("Unable to set server to save-off mode. Server seems to have crashed. Restarting server ...");
			tasks.add(Task.restart);
			return;
		}
		if (serverWrapper.setSaveOn())
			return;
		if (serverStatus == Status.running
				&& serverWrapper.getLastStatusChangeDate().getTime() < startDate
						.getTime() - 1000) {
			Log.out("Unable to set server to save-on mode. Server seems to have crashed. Restarting server ...");
			tasks.add(Task.restart);
		}
	}

}
