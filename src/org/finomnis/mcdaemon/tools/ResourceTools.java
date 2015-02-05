package org.finomnis.mcdaemon.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ResourceTools {
	
	public static InputStream openResource(String name){
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		return loader.getResourceAsStream(name);
		
	}
	
	public static Properties openProperties(String name){
		
		Properties properties = new Properties();
		
		try {
			InputStream resource = openResource(name);
			properties.load(resource);
			resource.close();
		} catch (IOException e) {
			Log.err(e);
			throw new RuntimeException("Critical Error!");
		}
		
		return properties;
		
	}
	
	public static String getVersionNumber(){
		
		try{
			
			Properties properties = openProperties("META-INF/maven/org.finomnis/mcdaemon/pom.properties");
			
			String version = properties.getProperty("version");
			
			return version;
			
		} catch (Exception e) {
			return "0.dev";
		}
		
	}
	
}
