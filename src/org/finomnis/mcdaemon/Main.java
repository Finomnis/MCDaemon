package org.finomnis.mcdaemon;

import java.util.Scanner;

import org.apache.commons.daemon.*;

import org.finomnis.mcdaemon.tools.Log;

public class Main implements org.apache.commons.daemon.Daemon{

	// To run it as a process
	public static void main(String[] args) {
		
		try{
			
			Thread killThread = new Thread() {
			    public void run() {
			        MCDaemon.kill();
			    }
			};

			Runtime.getRuntime().addShutdownHook(killThread); 
			
			MCDaemon.start();
			
			Scanner sc = new Scanner(System.in);
			System.out.println("Enter 'stop' to halt!");
			
			while(!sc.nextLine().toLowerCase().equals("stop"));
			
			sc.close();
			
			MCDaemon.stop();
			MCDaemon.waitForTermination();
			
			Runtime.getRuntime().removeShutdownHook(killThread);
			
			Log.out("Daemon shut down. Program ended.");
			
		}
		catch(Exception e)
		{
			Log.err(e);
		}
		
	}
	
	// To run it with jsvc
    @Override
    public void init(DaemonContext dc) throws Exception {
    }
    @Override
    public void start() {
    	try
    	{
    		MCDaemon.start();
    	}
    	catch(Exception e)
    	{
    		Log.err(e);
    	}
    }
    @Override
    public void stop() {
        try
        {
        	MCDaemon.stop();
        }
    	catch(Exception e)
    	{
    		Log.err(e);
    	}
    }
    @Override
    public void destroy() {
    }

    // To run it with prunsrv
    public static void windowsService(String args[]) {
        String cmd = "start";
        if (args.length > 0) {
            cmd = args[0];
        }

        if ("start".equals(cmd)) {
            MCDaemon.start();
            MCDaemon.waitForTermination();
        }
        else {
            MCDaemon.stop();
        }
    }
    
}
