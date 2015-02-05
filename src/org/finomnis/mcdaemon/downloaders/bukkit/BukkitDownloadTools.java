package org.finomnis.mcdaemon.downloaders.bukkit;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.finomnis.mcdaemon.tools.CriticalException;
import org.finomnis.mcdaemon.tools.Log;

public class BukkitDownloadTools {

	public enum BukkitEdition
	{
		Recommended,
		Beta,
		Development
	}
	
	public static String[] fetchNewestVersionInfos(BukkitEdition edition) throws CriticalException, IOException, ParserConfigurationException
	{
		
		Log.debug("Fetching newest version information...");
		
		/* TODO */
		
		/*		
		String mainWebsiteUrl;
		switch(edition){
		case Recommended:
			mainWebsiteUrl = "http://dl.bukkit.org/downloads/craftbukkit/list/rb/";
			break;
		case Beta:
			mainWebsiteUrl = "http://dl.bukkit.org/downloads/craftbukkit/list/beta/";
			break;
		case Development:
			mainWebsiteUrl = "http://dl.bukkit.org/downloads/craftbukkit/list/dev/";
			break;
		default:
			throw new CriticalException("Should not happen.");
		}
		
		Document website = Jsoup.connect(mainWebsiteUrl).timeout(10000).get();
		
		Elements divs = website.select("div");
		
		Element downloadButton = null;//website.select("div");
		for(int i = 0; i < divs.size(); i++){
			Element el = divs.get(i);
			String className = el.attr("class");
			if(className.contains("downloadButton chan"))
			{
				downloadButton = el;
				break;
			}
		}
		
		if(downloadButton == null)
			throw new CriticalException("Unable to find downloadbutton!");
		
		String downloadUrl = downloadButton.child(0).attr("abs:href");
		
		//Log.out("Url: " + downloadUrl);
		
		Elements h3s = website.select("h3");
		Element downloadInformation = null;
		
		for(int i = 0; i < h3s.size(); i++){
			try{
				Element el = h3s.get(i);
				String text = el.textNodes().get(0).text();
				if(text.contains("Latest Artifact Information")){
					downloadInformation = el.parent().child(1);
				}
			} catch(Exception e){}
		}
		
		if(downloadInformation == null)
			throw new CriticalException("Unable to get downloadInformation!");
		
		String md5Checksum = null;
		String buildNumber = null;
		String buildString = null;
		
		for(int i = 0; i < downloadInformation.children().size(); i++)
		{
			Element child = downloadInformation.child(i);
			if(!child.tagName().equals("dt"))
				continue;
			if(!child.hasText())
				continue;
			if(child.text().contains("MD5 Checksum:")){
				md5Checksum = downloadInformation.child(i+1).text().trim();
				continue;
			}
			if(child.text().contains("Version:"))
			{
				buildString = downloadInformation.child(i+1).text().trim();
				int leftPos = buildString.indexOf("(Build #") + "(Build #".length();
				int rightPos = buildString.lastIndexOf(")");
				buildNumber = buildString.substring(leftPos, rightPos);
				continue;
			}
		}
		
		if(md5Checksum == null)
			throw new CriticalException("Unable to get md5Checksum!");
		if(buildNumber == null)
			throw new CriticalException("Unable to get buildNumber!");
		
		//Log.out(md5Checksum);
		//Log.out(buildNumber);
		
		String[] ret = new String[4];
		
		ret[0] = downloadUrl;
		ret[1] = md5Checksum;
		ret[2] = buildNumber;
		ret[3] = buildString;
		
		return ret;
		*/
		
		return null;
		
	}
	
}
