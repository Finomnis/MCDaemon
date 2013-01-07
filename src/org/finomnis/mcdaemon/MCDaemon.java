package org.finomnis.mcdaemon;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.finomnis.mcdaemon.downloaders.MCDownloader;
import org.finomnis.mcdaemon.downloaders.bukkit.BukkitDownloader;
import org.finomnis.mcdaemon.downloaders.ftb.FTBDownloader;
import org.finomnis.mcdaemon.downloaders.vanilla.VanillaDownloader;
import org.finomnis.mcdaemon.tools.Log;

public class MCDaemon {

	private static boolean running = false;
	private static Lock lock = new ReentrantLock();
	private static Condition runningChangedCondition = lock.newCondition();

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
			MainConfigFile configFile;
			configFile = new MainConfigFile();

			// Load Minecraft Downloader
			MCDownloader mcDownloader;
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
				Log.err("Unable to determine correct downloader!");
				return;
			}

			if(!mcDownloader.isInitialized())
				mcDownloader.initialize();
			
		} catch (Exception e) {
			Log.err(e);
			return;
		}

	}

	private static void terminate() {

	}

}
