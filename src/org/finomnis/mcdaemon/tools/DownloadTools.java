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
		HttpURLConnection httpConnection = (HttpURLConnection) url
				.openConnection();

		if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
			throw new CriticalException("Unable to open URL '" + url + "'.");

		return httpConnection.getInputStream();
	}
	
	public static String getContentMD5(String url) throws MalformedURLException,
			IOException, CriticalException {

		return getContentMD5(new URL(url));

	}
	
	public static String getContentMD5(URL url) throws IOException, CriticalException {
		HttpURLConnection httpConnection = (HttpURLConnection) url
				.openConnection();

		if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
			throw new CriticalException("Unable to open URL '" + url + "'.");

		String md5 = httpConnection.getHeaderField("Content-MD5");
		
		httpConnection.disconnect();
		
		return md5;
	}

}
