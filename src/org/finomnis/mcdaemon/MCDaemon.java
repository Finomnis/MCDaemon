package org.finomnis.mcdaemon;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.finomnis.mcdaemon.tools.Log;

public class MCDaemon {

	private static boolean running = false;
	private static Lock lock = new ReentrantLock();	
	private static Condition runningChangedCondition = lock.newCondition();
	
	public static void start()
	{
		Log.out("Starting Daemon ...");
		
		lock.lock();
		if(running == true)
		{
			Log.err("Daemon already running!");
			lock.unlock();
			return;			
		}
		
		running = true;
		runningChangedCondition.signalAll();
		
		initialize();		
		
		lock.unlock();

		Log.out("Daemon is now running.");
		
	}
	
	public static void stop()
	{
		Log.out("Stopping Daemon ...");
		
		lock.lock();
		if(running == false)
		{
			Log.err("Daemon already stopped!");
			lock.unlock();
			return;
		}
		
		terminate();
		
		running = false;
		runningChangedCondition.signalAll();
		
		lock.unlock();
		
		Log.out("Daemon stopped.");
		
	}
	
	public static void waitForTermination()
	{
		Log.out("Waiting for Daemon to shut down ...");
		
		lock.lock();
		
		while(running)
		{
			try {
				runningChangedCondition.await();
			} catch (InterruptedException e) {
				Log.warn(e);
			}			
		}
		
		lock.unlock();
		
		Log.out("Daemon shut down.");
	}
	
	private static void initialize()
	{
		
	}
	
	private static void terminate()
	{
		
	}
	
}
