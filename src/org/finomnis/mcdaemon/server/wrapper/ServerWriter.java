package org.finomnis.mcdaemon.server.wrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.finomnis.mcdaemon.tools.Log;

public class ServerWriter implements Runnable {

	private OutputStream stream = null;
	private Lock streamLock = new ReentrantLock();
	private BlockingQueue<String> queue = new LinkedBlockingQueue<String>();


	public void setStream(OutputStream stream) {
		streamLock.lock();
		deleteStream();
		this.stream = stream;
		streamLock.unlock();
	}

	public void initializeShutdown() {
		queue.add("::stop::");
	}

	private void deleteStream() {
		streamLock.lock();
		try {
			if(stream != null)
				stream.close();
		} catch (Exception e2) {
			Log.warn(e2);
		} finally {
			stream = null;
			streamLock.unlock();
		}
	}

	public void write(String msg){
		if(msg.equals("::stop::"))
			msg = ":stop:";
		queue.add(msg);
	}
	
	@Override
	public void run() {
		try {
			String command;
			while (true) {
				try {
					command = queue.take();
				} catch (InterruptedException e) {
					Log.warn(e);
					continue;
				}
				
				if (command.equals("::stop::"))
					return;
				
				streamLock.lock();
				try {
					if (stream != null) {
						try {
							stream.write(command.getBytes());
							stream.write((int) ('\r'));
							stream.write((int) ('\n'));
							stream.flush();
						} catch (IOException e) {
							Log.warn(e);
							deleteStream();
						}
					}
				} finally {
					streamLock.unlock();
				}
			}
		} finally {
			deleteStream();
		}
	}

}
