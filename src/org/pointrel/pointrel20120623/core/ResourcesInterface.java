package org.pointrel.pointrel20120623.core;

import java.io.IOException;

public interface ResourcesInterface {

	/*
	 * Add the content as a resource and return a URI
	 * 
	 * @content The content to add
	 * @contentType The internet content type of the content
	 * @user The user adding; currently ignored
	 * @uri The URI if it was already calculated to avoid recalculation, or null otherwise
	 */
	public abstract String addContent(byte[] content, String contentType, String user, String uri) throws IOException;

	/*
	 * Retrieve the content for a resource referenced by the URI
	 * 
	 * @param uri The uri to retrieve
	 */
	public abstract byte[] getContentForURI(String uri) throws IOException;

}