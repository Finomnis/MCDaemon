package org.finomnis.mcdaemon.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadTools {

	public static InputStream openUrl(String url) throws MalformedURLException,
			IOException, CriticalException {

		return openUrl(new URL(url));

	}

	public static InputStream openUrl(URL url) throws IOException, CriticalException {
		
		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();

		if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
			throw new CriticalException("Unable to open URL '" + url + "'.");

		return httpConnection.getInputStream();
		
	}
	
	public static String getContentMD5(String url) throws MalformedURLException,
			IOException, CriticalException {

		return getContentMD5(new URL(url));

	}
	
	public static long getContentLength(String url) throws MalformedURLException,
			IOException, CriticalException{
		
		return getContentLength(new URL(url));
		
	}
		
	
	private static boolean isValidMD5(String md5){
		
		if(md5.length() != 32)
			return false;
		
		return md5.matches("[0123456789abcdef]{32}");
	}
	
	public static String getContentMD5(URL url) throws IOException, CriticalException {
		HttpURLConnection httpConnection = (HttpURLConnection) url
				.openConnection();

		if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
			throw new CriticalException("Unable to open URL '" + url + "'.");

		String md5 = httpConnection.getHeaderField("Content-MD5");
		
		httpConnection.disconnect();

		if(md5 == null)
			return null;
			
		md5 = md5.toLowerCase().trim();
		if(!(isValidMD5(md5)))
			return null;
		
		return md5;
	}
	
	public static long getContentLength(URL url) throws IOException, CriticalException{
		
		HttpURLConnection httpConnection = (HttpURLConnection) url
				.openConnection();

		if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
			throw new CriticalException("Unable to open URL '" + url + "'.");

		long contentLength = httpConnection.getContentLengthLong();
				
		httpConnection.disconnect();
		
		return contentLength;
		
	}

}
