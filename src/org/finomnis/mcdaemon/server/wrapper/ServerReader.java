package org.finomnis.mcdaemon.server.wrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.finomnis.mcdaemon.server.wrapper.ServerWrapper.Status;
import org.finomnis.mcdaemon.tools.Log;

public class ServerReader implements Runnable{
	
	private BufferedReader stream = null;
	private Lock streamLock = new ReentrantLock();
	private Condition streamCondition = streamLock.newCondition();
	private ServerWrapper serverWrapper = null;
	private boolean shutdownRequested = false;
	
	
	public ServerReader(ServerWrapper serverWrapper)
	{
		this.serverWrapper = serverWrapper;
	}
	
	private void deleteStream() {
		streamLock.lock();
		try {
			if(stream != null)
				stream.close();
		} catch (Exception e) {
			Log.warn(e);
		} finally {
			stream = null;
			streamLock.unlock();
			serverWrapper.setStatus(Status.stopped);
		}
	}
	
	public void setStream(InputStream stream){
		streamLock.lock();
		deleteStream();
		try {
			this.stream = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			streamCondition.signalAll();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Serious problem. Should never happen!");
		} finally {
			streamLock.unlock();
		}
	}

	public void initializeShutdown(){
		streamLock.lock();
		try{
			shutdownRequested = true;
			deleteStream();
			streamCondition.signalAll();
		}finally{
			streamLock.unlock();
		}
	}
	
	@Override
	public void run() {
		while(true)
		{
			streamLock.lock();
			try{
				while(stream == null)
				{
					if(shutdownRequested)
						return;
					try {
						streamCondition.await();
					} catch (InterruptedException e) {
						Log.warn(e);
					}
				}
				
				try{
					String msg = stream.readLine();
					while(msg != null)
					{
						processMessage(msg);
						msg = stream.readLine();
					}
				} catch (IOException e) {
					Log.warn(e);
				} finally{
					deleteStream();
				}
				
			} finally {
				streamLock.unlock();
			}
		}
	}

	private void processMessage(String msg) {
		Log.out(msg);		
	}
	
	
}
