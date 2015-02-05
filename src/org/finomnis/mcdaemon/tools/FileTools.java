package org.finomnis.mcdaemon.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.DatatypeConverter;

public class FileTools {

	public static boolean delete(String file) {
		return delete(new File(file));
	}

	public static boolean delete(File file) {
		if (file.isDirectory()) {
			File[] subFiles = file.listFiles();
			for (File subFile : subFiles) {
				if (!delete(subFile))
					return false;
			}
			return file.delete();
		} else {
			return file.delete();
		}
	}

	public static FileWriter openFileWriteText(File f, boolean append)
			throws IOException {
		File dir = f.getParentFile();
		if (dir != null) {
			dir.mkdirs();
		}
		return new FileWriter(f, append);
	}

	public static FileWriter openFileWriteText(String path, boolean append)
			throws IOException {
		File f = new File(path);
		return openFileWriteText(f, append);
	}

	public static FileOutputStream openFileWrite(File f, boolean append)
			throws FileNotFoundException {
		File dir = f.getParentFile();
		if (dir != null) {
			dir.mkdirs();
		}
		return new FileOutputStream(f, append);
	}

	public static FileOutputStream openFileWrite(String path, boolean append)
			throws FileNotFoundException {
		File f = new File(path);
		return openFileWrite(f, append);
	}

	public static void writeFromStream(InputStream iStream, OutputStream fWriter)
			throws IOException {

		byte[] buf = new byte[4096];

		while (true) {

			int len = iStream.read(buf);
			if (len < 0)
				break;
			fWriter.write(buf, 0, len);

		}

	}
		
	private final static int logIntervalPercent = 1;
	
	public static void writeFromStream(InputStream iStream, OutputStream fWriter, long dataSize)
			throws IOException {
		
		byte[] buf = new byte[4096];

		int nextLogPercentage = logIntervalPercent;
		long alreadyWritten = 0;
		while (true) {

			int len = iStream.read(buf);
			if (len < 0)
				break;
			fWriter.write(buf, 0, len);
			alreadyWritten += len;
			if(alreadyWritten*100 >= nextLogPercentage * dataSize){
				Log.debug(nextLogPercentage + " %");
				nextLogPercentage += logIntervalPercent;
			}

		}

	}

	public static long fileSize(File f) {

		return f.length();

	}

	public static long fileSize(String filename) {

		return fileSize(new File(filename));

	}

	public static FileInputStream openFileRead(File file)
			throws FileNotFoundException {
		return new FileInputStream(file);
	}

	public static FileInputStream openFileRead(String path)
			throws FileNotFoundException {
		File f = new File(path);
		return openFileRead(f);
	}

	public static String md5(byte[] data) throws CriticalException {

		MessageDigest md5Calculator;
		
		try {
			md5Calculator = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new CriticalException("Unable to initialize MD5 calculator!");
		}
		
		return DatatypeConverter.printHexBinary(md5Calculator.digest(data)).toLowerCase();

	}

	public static String md5(String str) throws CriticalException {

		return md5(str.getBytes());

	}

	public static String md5(File f) throws IOException, CriticalException {

		InputStream iStream = openFileRead(f);
		
		String md5String =  md5(iStream);
		
		iStream.close();
		
		return md5String;
		
	}

	public static void copyFile(String inFile, String outFile)
			throws IOException {

		InputStream iStream = openFileRead(inFile);
		OutputStream oStream = openFileWrite(outFile, false);
		writeFromStream(iStream, oStream);
		iStream.close();
		oStream.close();

	}

	public static String md5(InputStream iStream) throws IOException,
			CriticalException {

		MessageDigest md5Calculator;
		
		try {
			md5Calculator = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new CriticalException("Unable to initialize MD5 calculator!");
		}
		
		byte[] buf = new byte[4096];
		
		while(true) {
			int len = iStream.read(buf);
			
			if(len < 0) break;
			
			md5Calculator.update(buf, 0, len);
		}
		
		return DatatypeConverter.printHexBinary(md5Calculator.digest()).toLowerCase();

	}

	public static boolean fileExists(String path) {
		try {
			File f = new File(path);
			return f.exists() && !f.isDirectory();
		} catch (Exception e) {
			Log.warn(e);
			return false;
		}
	}

	public static boolean folderExists(String path) {
		try {
			File f = new File(path);
			return f.exists() && f.isDirectory();
		} catch (Exception e) {
			Log.warn(e);
			return false;
		}
	}

	public static void createFolder(String path) {

		File f = new File(path);
		f.mkdirs();

	}
	
	public static String readStreamToString(InputStream inputStream){
		
		java.util.Scanner scanner = new java.util.Scanner(inputStream, "UTF-8");
		scanner.useDelimiter("\\A");
		String str = scanner.hasNext() ? scanner.next() : "";
		scanner.close();
		return str;
		
	}

	public static void writeStringToFile(String fileName, String content) throws IOException{
		
		FileOutputStream outputStream = openFileWrite(fileName, false);
		outputStream.write(content.getBytes());
		
	}
	
	public static void replaceInFile(String fileName, String origin, String replacement) throws IOException{
		
		String fileContent = readStreamToString(openFileRead(fileName));
		fileContent = fileContent.replace(origin, replacement);
		writeStringToFile(fileName, fileContent);		
		
	}

	public static void unzip(String zipName, String folderName) throws IOException
	{
		
		unzip(zipName, folderName, false);
		
	}
	
	public static void unzip(String zipName, String folderName, boolean replaceJarNames)
			throws IOException {

		ZipInputStream zipStream = new ZipInputStream(openFileRead(zipName));

		ZipEntry entry = zipStream.getNextEntry();

		File parentFolder = new File(folderName);

		while (entry != null) {

			String fileName = entry.getName();

			File newFile = new File(folderName + fileName);

			if (entry.isDirectory()) {
				newFile.mkdirs();

			} else {

				if (replaceJarNames && fileName.endsWith(".jar")
						&& newFile.getParentFile().equals(parentFolder)) {
					fileName = "server.jar";
					newFile = new File(folderName + fileName);
				}

				OutputStream outStream = openFileWrite(newFile, false);

				byte[] buf = new byte[4096];

				while (true) {

					int len = zipStream.read(buf);
					if (len <= 0)
						break;
					outStream.write(buf, 0, len);

				}

				outStream.close();

			}

		entry = zipStream.getNextEntry();
		}

		zipStream.closeEntry();

		zipStream.close();

	}
	
}
