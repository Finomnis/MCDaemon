package org.finomnis.mcdaemon.server;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.finomnis.mcdaemon.downloaders.MCDownloader;
import org.finomnis.mcdaemon.tools.CriticalException;
import org.finomnis.mcdaemon.tools.Log;
import org.finomnis.mcdaemon.tools.SyncVar;

public class ServerWrapper {

	public enum Status {
		running, stopped
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

	
	public ServerWrapper(MCDownloader mcDownloader) {
		this.mcDownloader = mcDownloader;
		
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

	public synchronized void startServer() throws CriticalException, IOException {
		serverLock.lock();
		try {
			if (status.get() != Status.stopped)
				throw new CriticalException("Can't start server! Already running!");
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
			status.set(Status.running);
			stillAlive.set(true);
			saveOff.set(false);
		} finally {
			serverLock.unlock();
		}
	}

	public synchronized void stopServer() {
		serverLock.lock();
		try {
			stdIn.write("stop");
			if(!status.waitForValue(Status.stopped, 30000))
			{
				serverProcess.destroy();
				if(!status.waitForValue(Status.stopped, 10000))
				{
					Log.err("Unable to stop server!");
				}
			}
			serverProcess = null;
			status.set(Status.stopped);
		} finally {
			serverLock.unlock();
		}
	}

	public synchronized void shutdown() {
		serverLock.lock();
		try {
			shutdownRequested.set(true);
			if (status.get() != Status.stopped)
				stopServer();
			stdIn.initializeShutdown();
			stdOut.initializeShutdown();
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
	}

	protected void setSaveOff(boolean val) {
		saveOff.set(val);
	}

}
