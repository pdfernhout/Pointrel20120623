package org.pointrel.pointrel20120623.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Utility {
	public static Charset utf8Charset = Charset.forName("UTF-8");
	
	// Use a line separator compatible with text/plain internet content types:
	// http://tools.ietf.org/html/rfc2046#section-4.1.1
	public static String LineBreak = "\r\n";

	public static String currentTimestamp() {
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date now = new Date();
		return formatter.format(now);
	}

	/*
	 * Inspuired by Google Guava' Throwables.propagate: 
	 * Propagates throwable as-is if it is an 
	 * instance of RuntimeException or Error, or else as a last resort, 
	 * wraps it in a RuntimeException then propagates.
	 * 
	 * @param throwable An exception
	 */
	static RuntimeException propagate(Throwable throwable) {
		// Throwables.propagate(throwable);
		if (throwable instanceof Error) {
			throw (Error) throwable;
		}
		if (throwable instanceof RuntimeException) {
			throw (RuntimeException) throwable;
		}
		throw new RuntimeException(throwable);
	}

	public static void write(byte[] content, File fileToWrite) throws IOException {
		// Files.write(content, fileToWrite);
		FileOutputStream fos = new FileOutputStream(fileToWrite);
		fos.write(content);
		fos.close();
	}

	public static void appendToLogFile(String logEntry, File logFile, String header) throws IOException {
		boolean exists = logFile.exists();
		boolean writeHeader = false;
		if (exists) {
			System.out.println("Appending to: " + logFile + " log as: " + logFile);
		} else {
			System.out.println("Creating: " + logFile + " log as: " + logFile);
			writeHeader = true;
		}
		// Files.append(logEntry, logFile, utf8Charset);
		FileOutputStream outputStream = new FileOutputStream(logFile, true);
		if (writeHeader && header != null) {
			outputStream.write(header.getBytes("UTF-8"));
			outputStream.write(LineBreak.getBytes("UTF-8"));
		}
		outputStream.write(logEntry.getBytes("UTF-8"));
		outputStream.close();
	}

//	public static String lastLine(File logFile) {
//		boolean exists = logFile.exists();
//		if (!exists) return null;
//
//		List<String> lines;
//		try {
//			lines = Files.readLines(logFile, utf8Charset);
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw Utility.propagate(e);
//		}
//		if (lines.size() == 0) return null;
//		String lastLine = lines.get(lines.size() - 1);
//		// Guava seems to remove the newlines itself
//		//if (lastLine.charAt(uri.length() -1) != '\n') {
//		//	throw new RuntimeException("Expected newline at end of line: " + lastLine);			
//		//} else {
//		//	lastLine = lastLine.substring(0, uri.length() - 1);
//		//}
//		return lastLine;
//	}
	
	// Derived: http://stackoverflow.com/questions/686231/java-quickly-read-the-last-line-of-a-text-file
	// Improved to ensure the last line converts correctly from UTF-8 multi-byte characters
	public static String lastLine(File logFile) {
		if (!logFile.exists()) return null;
		try {
			RandomAccessFile fileHandler = new RandomAccessFile(logFile, "r");
			long fileLength = logFile.length() - 1;
			ArrayList<Byte> bytesReversed = new ArrayList<Byte>();

			for (long filePointer = fileLength; filePointer != -1; filePointer--) {
				fileHandler.seek(filePointer);
				int readByte = fileHandler.readByte();

				if (readByte == 0xA) {
					if (filePointer == fileLength) {
						continue;
					} else {
						break;
					}
				} else if (readByte == 0xD) {
					if (filePointer == fileLength - 1) {
						continue;
					} else {
						break;
					}
				}

				bytesReversed.add((byte)readByte);
			}

			byte[] bytes = new byte[bytesReversed.size()];
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = bytesReversed.get(bytes.length - 1 - i);
			}
			String lastLine = new String(bytes, "UTF-8");
			return lastLine;
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (java.io.IOException e) {
			e.printStackTrace();
			return null;
		}
	}


	public static byte[] toByteArray(File fileToRead) throws IOException {
		// return Files.toByteArray(fileToRead);
		// From:
		// http://stackoverflow.com/questions/6058003/beautiful-way-to-read-file-into-byte-array-in-java
		RandomAccessFile f = new RandomAccessFile(fileToRead, "r");
		try {
			long longlength = f.length();
			int length = (int) longlength;
			if (length != longlength) throw new IOException("File size >= 2 GB");
			byte[] data = new byte[length];
			f.readFully(data);
			return data;
		} finally {
			f.close();
		}
	}

	public static String hashForContent(byte[] content) {
	    MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw propagate(e);
		}
	
	    byte[] hash = md.digest(content);
	
	    StringBuffer sb = new StringBuffer();
	    for(byte b : hash) {
	        sb.append(String.format("%02x", b));
	    }
	
	    return sb.toString();
	}

	public static String getExtension(String fileName) {
		// Since content types can have multiple dots, 
		// and the whole content type is generally what we are interested in,
		// we need to give back everything from the first dot
		String extension = null;
		int firstDot = fileName.indexOf('.');
	
		if (firstDot > 0 && firstDot < fileName.length() - 1) {
			// Could have .toLowerCase()
			extension = fileName.substring(firstDot + 1);
		} else if (firstDot == fileName.length() - 1) {
			extension = "";
		}
		return extension;
	}

	public static String encodeContentType(String contentType) {
		try {
			return URLEncoder.encode(contentType, "utf8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw propagate(e);
		}
	}
	
	public static String decodeContentType(String contentType) {
		try {
			return URLDecoder.decode(contentType, "utf8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw propagate(e);
		}
	}
	
	public static String contentTypeForURI(String uri) {
		String extension = getExtension(uri);
		if (extension == null) return "";
		return decodeContentType(extension);
	}
	
	public static String escapeLine(String line) {
		String result = line.replaceAll("\\\\", "\\\\\\\\");
		result = result.replaceAll("\n", "\\\\n");
		return result;
	}
	
	public static String unescapeLine(String line) {
		String result = line.replaceAll("\\\\n", "\n");;
		result = result.replaceAll("\\\\\\\\", "\\\\");
		return result;
	}

	public static String generateUUID(String contentType) {
		UUID uuid = UUID.randomUUID();
		if (contentType == null || contentType.length() == 0) {
			return "uuid://" + uuid;
		} else {
			return "uuid://" + uuid + "." + encodeContentType(contentType);
		}
	}
	
	/* 
	 * Turn an array of bytes with embedded newlines into a list of strings, optionally removing and checking a header;
	 * unescapes each line for backslashes and embedded newlines
	 * 
	 * @param content the array of bytes to parse
	 * @param headerToRemove if headerToRemove is not null, 
	 * remove it from the list and check that it matches, 
	 * throwing an exception if it does not
	*/
//	public static ArrayList<String> listForContent(byte[] content, String headerToRemove) {
//		if (content == null) return new ArrayList<String>();
//		String contentAsString;
//		try {
//			contentAsString = new String(content, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//			return null;
//		}
//		// Need the -1 so split does not discard empty tokens at end
//		String[] listItems = contentAsString.split(LineBreak, -1);
//		@SuppressWarnings("unchecked")
//		ArrayList<String> result = new ArrayList();
//		for (String item: listItems) {
//			result.add(unescapeLine(item));
//		}
//		if (headerToRemove != null) {
//			String headerRead = result.remove(0);
//			if (!headerRead.equals(headerToRemove)) {
//				throw new RuntimeException("headerRead: \"" + headerRead + "\" was not the same as expected: \"" + headerToRemove + "\"");
//			}
//		}
//		return result;
//	}
	
	/* 
	 * Turn an list of strings into an array of bytes, optionally adding a header;
	 * escapes all backslashes and newlines
	 * 
	 * @param list the list of strings to join
	 * @param headerToAdd the header to add if not null
	*/
//	public static byte[] contentForList(ArrayList<String> list, String headerToAdd) {
//		if (list == null) return new byte[0];
//		StringBuilder stringBuilder = new StringBuilder();
//		boolean first = true;
//		if (headerToAdd != null) {
//			stringBuilder.append(headerToAdd);
//			first = false;
//		}
//		for (String item: list) {
//			if (!first) {
//				stringBuilder.append(LineBreak);
//				first = false;
//			}
//			//if (item != null && item.indexOf('\n') != -1) {
//			//	throw new RuntimeException("list item can't have newline: " + item);
//			//}
//			if (item != null) {
//				stringBuilder.append(escapeLine(item));
//			}
//		}
//		try {
//			return stringBuilder.toString().getBytes("utf-8");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}

	public static String calculateURI(byte[] content, String contentType) {
		String contentHash = hashForContent(content);
		String encodedContentType = encodeContentType(contentType);
		String uri = "pointrel://" +  "sha256_" + contentHash + "_" + content.length + "." + encodedContentType;
		return uri;
	}
	
	public static String ExpectedPointrelURIStart = "pointrel://sha256_";

	public static boolean isValidPointrelURI(String uri) {
		if (uri == null) return false;
		if (!uri.startsWith(ExpectedPointrelURIStart)) return false;
		if (uri.length() < ExpectedPointrelURIStart.length() + 64 + 1 + 1) return false;
		if (uri.lastIndexOf("_") != ExpectedPointrelURIStart.length() + 64) return false;
		return true;
	}
	
	public static boolean isValidPointrelURIOrNull(String uri) {
		if (uri == null) return true;
		return isValidPointrelURI(uri);
	}
}
