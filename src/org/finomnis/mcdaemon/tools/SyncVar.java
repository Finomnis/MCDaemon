package org.finomnis.mcdaemon.tools;

import java.util.Date;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SyncVar<X> {

	private X val;
	private Lock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();
	
	public SyncVar(X val){
		lock.lock();
		this.val = val;
		condition.signalAll();
		lock.unlock();
	}
	
	public X get()
	{
		X res;
		lock.lock();
		res = val;
		lock.unlock();
		return res;
	}
	
	public void set(X val)
	{
		lock.lock();
		this.val = val;
		condition.signalAll();
		lock.unlock();
	}
	
	public void waitForValue(X val)
	{
		lock.lock();
		try{
			while(this.val != val)
			{
				try {
					condition.await();
				} catch (InterruptedException e) {
					Log.warn(e);
				}
			}
		} finally {
			lock.unlock();
		}
	}
	
	public boolean waitForValue(X val, long time)
	{
		Date now = new Date();
		Date deadline = new Date(now.getTime() + time);
		lock.lock();
		try{
			while(this.val != val)
			{
				try {
					if(!condition.awaitUntil(deadline))
						return false;
				} catch (InterruptedException e) {
					Log.warn(e);
				}
			}
		} finally {
			lock.unlock();
		}
		return true;
	}
	
	
}
