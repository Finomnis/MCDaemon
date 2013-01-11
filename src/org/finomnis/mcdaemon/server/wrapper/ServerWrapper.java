package org.finomnis.mcdaemon.server.wrapper;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.finomnis.mcdaemon.downloaders.MCDownloader;
import org.finomnis.mcdaemon.server.ServerMonitor;
import org.finomnis.mcdaemon.tools.CriticalException;
import org.finomnis.mcdaemon.tools.Log;
import org.finomnis.mcdaemon.tools.SyncVar;

public class ServerWrapper {

	public enum Status {
		running, stopped, starting
	};

	private Lock serverLock;

	private SyncVar<Boolean> stillAlive;
	private SyncVar<Status> status;
	private SyncVar<Boolean> saveOff;
	private SyncVar<Boolean> shutdownRequested;

	Process serverProcess;
	private ServerWriter stdIn;
	private Thread stdInThread;
	private ServerReader stdOut;
	private Thread stdOutThread;

	private MCDownloader mcDownloader;
	private ServerMonitor serverMonitor;

	private Date lastStartTime = null;
	private Date lastActivity = new Date();
	
	public ServerWrapper(MCDownloader mcDownloader, ServerMonitor serverMonitor) {
		this.mcDownloader = mcDownloader;
		this.serverMonitor = serverMonitor;

		serverLock = new ReentrantLock();

		stillAlive = new SyncVar<Boolean>(false);
		status = new SyncVar<Status>(Status.stopped);
		saveOff = new SyncVar<Boolean>(false);
		shutdownRequested = new SyncVar<Boolean>(false);

		serverProcess = null;
		stdIn = new ServerWriter();
		stdInThread = new Thread(stdIn);
		stdOut = new ServerReader(this);
		stdOutThread = new Thread(stdOut);

		stdInThread.start();
		stdOutThread.start();
	}

	public void killServer() {
		Log.out("Killing server...");
		if (serverProcess != null)
			serverProcess.destroy();
	}

	public synchronized void startServer() throws CriticalException,
			IOException {
		Log.out("Starting server...");
		serverLock.lock();
		try {
			if (status.get() != Status.stopped)
				throw new CriticalException(
						"Can't start server! Already running!");
			if (shutdownRequested.get())
				throw new CriticalException(
						"Can't start server! System shutdown in progress!");
			String folder = mcDownloader.getFolderName();
			List<String> command = mcDownloader.getInvocationCommand();
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.directory(new File(folder));
			pb.redirectErrorStream(true);
			serverProcess = pb.start();
			stdIn.setStream(serverProcess.getOutputStream());
			stdOut.setStream(serverProcess.getInputStream());

			status.set(Status.starting);
			lastStartTime = new Date();
			lastActivity = new Date();
			stillAlive.set(true);
			saveOff.set(false);
		} finally {
			serverLock.unlock();
		}
	}

	public synchronized void stopServer() {
		Log.out("Stopping server...");
		serverLock.lock();
		try {
			if (status.get() != Status.stopped && serverProcess != null) {
				stdIn.write("stop");
				if (!status.waitForValue(Status.stopped, 10000)) {
					stdIn.write("stop");
					while (status.get() != Status.stopped) {
						if(status.get() == Status.starting)
							break;
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							Log.warn(e);
						}
						if (getServerInactiveTime() > 30000) {
							break;
						}
					}
					if (status.get() != Status.stopped) {
						Log.out("Unable to stop server gracefully. Killing server process ...");
						serverProcess.destroy();
						if (!status.waitForValue(Status.stopped, 10000)) {
							Log.err("Unable to stop server!");
						}
					}

				}
			}
			serverProcess = null;
			status.set(Status.stopped);
		} finally {
			serverLock.unlock();
			Log.out("Server stopped.");
		}
	}

	public synchronized void shutdown() {
		Log.out("Shutting down serverWrapper...");
		serverLock.lock();
		try {
			if (!shutdownRequested.get()) {
				shutdownRequested.set(true);
				if (status.get() != Status.stopped)
					stopServer();
				stdIn.initializeShutdown();
				stdOut.initializeShutdown();
			}
			try {
				stdInThread.join();
			} catch (InterruptedException e) {
				Log.err(e);
			}
			try {
				stdOutThread.join();
			} catch (InterruptedException e) {
				Log.err(e);
			}
		} finally {
			serverLock.unlock();
		}
	}

	protected void setStillAlive(boolean val) {
		stillAlive.set(val);
	}

	protected void setStatus(Status val) {
		status.set(val);
		if (val == Status.stopped)
			serverMonitor.requestHealthCheck();
	}

	protected void setSaveOff(boolean val) {
		saveOff.set(val);
	}

	public Status getStatus() {
		return status.get();
	}

	public boolean stillAliveTest() {
		stillAlive.set(false);
		stdIn.write("seed");
		return stillAlive.waitForValue(true, 20000);
	}

	public Date getLastStartTime() {
		return lastStartTime;
	}

	public long getServerInactiveTime() {
		return new Date().getTime() - lastActivity.getTime();
	}

	public void setServerWasActive() {

		lastActivity = new Date();
		
	}

	public Date getLastStatusChangeDate()
	{
		return status.getLastChangeDate();
	}
	
	public boolean setSaveOff(){
		stdIn.write("save-off");
		stdIn.write("save-all");
		return saveOff.waitForValue(true, 30000);
	}
	
	public boolean setSaveOn(){
		stdIn.write("save-on");
		return saveOff.waitForValue(false, 30000);
	}

	public boolean getSaveOff() {
		return saveOff.get();
	}
	
}
