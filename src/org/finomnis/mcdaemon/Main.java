package org.finomnis.mcdaemon;

import org.finomnis.mcdaemon.tools.Log;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try{
			
			throw new RuntimeException("TestException");
		}
		catch(Exception e)
		{
			
			Log.warn(e);
			
		}
		
	}

}
