package org.finomnis.mcdaemon.downloaders.hexxit;

import java.io.IOException;

import org.finomnis.mcdaemon.tools.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HexxitDownloadTools {

	public static String[] fetchNewestVersionInfos() throws IOException
	{
		String[] result = new String[2];
		
		Log.debug("Fetching newest version information...");
		String websiteUrl = "http://www.technicpack.net/hexxit/";
		Document website = Jsoup.connect(websiteUrl).timeout(10000).get();
		
		String downloadUrl = null;
		Elements urls = website.select("a");
		for(int i = 0; i < urls.size(); i++){
			Element el = urls.get(i);
			String href = el.attr("href");
			if(href.contains("http://mirror.technicpack.net/Technic/servers/hexxit"))
			{
				downloadUrl = href;
				break;
			}
		}
		
		Log.debug("Hexxit downloadUrl: " + downloadUrl);
		
		result[0] = downloadUrl;
		
		// Calculate name
		int splitPos = downloadUrl.lastIndexOf('/');
		String fileName = downloadUrl.substring(splitPos + 1);
		
		splitPos = fileName.lastIndexOf('.');
		String versionName = fileName.substring(0,splitPos);
		Log.debug("Hexxit Version: " + versionName);

		result[1] = versionName;

		return result;
	}
	
	
	
}
