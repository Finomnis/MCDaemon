package org.finomnis.mcdaemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.finomnis.mcdaemon.downloaders.MCDownloader;
import org.finomnis.mcdaemon.downloaders.bukkit.BukkitDownloader;
import org.finomnis.mcdaemon.downloaders.ftb.FTBDownloader;
import org.finomnis.mcdaemon.downloaders.vanilla.VanillaDownloader;
import org.finomnis.mcdaemon.server.ServerMonitor;
import org.finomnis.mcdaemon.server.ServerWrapper;
import org.finomnis.mcdaemon.tools.ConfigNotFoundException;
import org.finomnis.mcdaemon.tools.Log;

public class MCDaemon {

	private static boolean running = false;
	private static Lock lock = new ReentrantLock();
	private static Condition runningChangedCondition = lock.newCondition();
	private static MainConfigFile configFile = null;
	private static MCDownloader mcDownloader = null;
	private static ServerMonitor serverMonitor = null;
	//
	private static ServerWrapper serverWrapper = null;
	
	public static void start() {
		Log.out("Starting Daemon ...");

		lock.lock();
		if (running == true) {
			Log.err("Daemon already running!");
			lock.unlock();
			return;
		}

		running = true;
		runningChangedCondition.signalAll();

		initialize();

		lock.unlock();

		Log.debug("Daemon is now running.");

	}

	public static void stop() {
		Log.out("Stopping Daemon ...");

		lock.lock();
		if (running == false) {
			Log.err("Daemon already stopped!");
			lock.unlock();
			return;
		}

		terminate();

		running = false;
		runningChangedCondition.signalAll();

		lock.unlock();

		Log.debug("Daemon stopped.");

	}

	public static void waitForTermination() {
		Log.out("Waiting for Daemon to shut down ...");

		lock.lock();

		while (running) {
			try {
				runningChangedCondition.await();
			} catch (InterruptedException e) {
				Log.warn(e);
			}
		}

		lock.unlock();

		Log.debug("Daemon shut down.");
	}

	private static void initialize() {
		try {
			
			// Load config file
			configFile = new MainConfigFile();

			// Load Minecraft Downloader
			switch (configFile.getConfig("mcEdition")) {
			case "ftb":
				mcDownloader = new FTBDownloader();
				break;
			case "vanilla":
				mcDownloader = new VanillaDownloader();
				break;
			case "bukkit":
				mcDownloader = new BukkitDownloader();
				break;
			default:
				throw new RuntimeException("Unable to determine downloader!");
			}

			mcDownloader.initialize();
			
			//ProcessBuilder pb = new ProcessBuilder(mcDownloader.getInvocationCommand());
			//pb.directory(new File(mcDownloader.getFolderName()));
			//Process proc = pb.start();
			
			//serverMonitor = new ServerMonitor(mcDownloader);
			serverWrapper = new ServerWrapper(mcDownloader);			
			serverWrapper.startServer();
			
		} catch (Exception e) {
			Log.err(e);
			throw new RuntimeException("Unable to initialize");
		}

	}

	private static void terminate() {

		if(serverWrapper != null)
			serverWrapper.shutdown();
		
	}
	
	public static String getConfig(String config) throws ConfigNotFoundException
	{
	
		return configFile.getConfig(config);
		
	}
	
	public static ServerMonitor getServerMonitor()
	{
		
		return serverMonitor;
		
	}

}
