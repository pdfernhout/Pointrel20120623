package org.pointrel.pointrel20120623.core;

import java.io.File;
import java.io.IOException;

public class ResourceFiles implements ResourcesInterface {
	final public File resourcesBaseDirectory;
	
	boolean throwExceptionWhenAddingExistingContent = false;
	
	public ResourceFiles(File resourcesBaseDirectory) {
		if (resourcesBaseDirectory == null) resourcesBaseDirectory = new File("./PointrelArchive/resources");
		if (resourcesBaseDirectory.getName() != "resources") {
			resourcesBaseDirectory = new File(resourcesBaseDirectory, "resources");
		}
		this.resourcesBaseDirectory = resourcesBaseDirectory;
	}

	/*
	 * Add the content as a resource and return a URI
	 * 
	 * @content The content to add
	 * @contentType The internet content type of the content
	 * @user The user adding; currently ignored
	 * @uri The URI if it was already calculated to avoid recalculation, or null otherwise
	 */
	/* (non-Javadoc)
	 * @see org.pointrel.pointrel20120623.core.ResourcesInterface#addContent(byte[], java.lang.String, java.lang.String, java.lang.String)
	 */
	public String addContent(byte[] content, String contentType, String user, String uri) throws IOException {
		if (uri == null) uri = Utility.calculateURI(content, contentType);
		File resourceFile = resourceFileForURI(uri, false);
		if (resourceFile.exists()) {
			if (throwExceptionWhenAddingExistingContent) {
				throw new RuntimeException("Resource file already exists: " + resourceFile);
			}
		} else {
			this.writeResourceFile(resourceFile, content);
		}
		return uri;
	}

	/*
	 * Retrieve the content for a resource referenced by the URI
	 * 
	 * @param uri The uri to retrieve
	 */
	/* (non-Javadoc)
	 * @see org.pointrel.pointrel20120623.core.ResourcesInterface#getContentForURI(java.lang.String)
	 */
	public byte[] getContentForURI(String uri) throws IOException {
		if (uri == null) return null;
		return this.loadResourceIfAvailable(uri);
	}
	
	// Supporting methods

	boolean writeResourceFile(File resourceFile, byte[] content) throws IOException {
		boolean exists = resourceFile.exists();
		if (exists) {
			System.out.println("File exists: " + resourceFile);
			return false;
		}
		
		System.out.println("Writing resource: " + resourceFile);
		
		resourceFile.getParentFile().mkdirs();
		
		Utility.write(content, resourceFile);
		
        return true;
	}

	String fileNameForURI(String uri) {
		String fileName = uri.split("://")[1];
		return fileName;
	}
	
	File archiveDirectoryForFileName(String fileName) {
		System.out.println("archiveDirectoryForFileName: " + fileName);
		String encodedContentType = Utility.getExtension(fileName);
		if (encodedContentType == null || encodedContentType.equals("")) {
			encodedContentType = "unknown"; 
		}
		String[] nameParts = fileName.split("_");
		if (nameParts.length != 3) {
			throw new RuntimeException("Unexpected resource file name format: " + fileName);
		}
		String hash = nameParts[1];
		String s1 = hash.substring(0, 2);
		String s2 = hash.substring(2, 4);
		String s3 = hash.substring(4, 6);
		String s4 = hash.substring(6, 8);
		String separator = File.separator;
		String path = encodedContentType + separator + s1 + separator + s2 + separator + s3 + separator + s4 + separator;
		return new File(this.resourcesBaseDirectory, path);
	}

	File resourceFileForURI(String uri, boolean checkIfFileExists) {
		if (uri == null) {
			throw new IllegalArgumentException("uri should not be null");
		}
		
		String fileName = fileNameForURI(uri);
		
		File directory = this.archiveDirectoryForFileName(fileName);
		File resourceFile = new File(directory, fileName);
		
		if (checkIfFileExists && !resourceFile.exists()) return null;
	    return resourceFile;		
	}

	boolean isResourceAvailable(String uri) {
		return resourceFileForURI(uri, true) != null;
		
		//String fileName = fileNameForURI(uri);
		//File directory = this.archiveDirectoryForFileName(fileName);
		//if (!directory.exists()) return false;
		//File fileToRead = new File(directory, fileName);
	    //return fileToRead.exists();
	}
	
	// Load the resource from a file if available
	byte[] loadResourceIfAvailable(String uri) throws IOException {
		if (!Utility.isValidPointrelURI(uri)) return null;
		File fileToRead = resourceFileForURI(uri, true);
		if (fileToRead == null)  return null;
		if (!fileToRead.exists()) return null;
		
		System.out.println("loading: " + uri + " from: " + fileToRead);
		
		byte[] content = Utility.toByteArray(fileToRead);
		return content;
	}
}
