package org.pointrel.pointrel20120623.demos.simplecommunityapp;

import org.pointrel.pointrel20120623.core.Session;
import org.pointrel.pointrel20120623.core.Utility;

public class AbstractResourceProcessor implements ResourceProcessor {
	String contentTypeEncoded;

	public AbstractResourceProcessor(String contentType) {
		this.contentTypeEncoded = Utility.encodeContentType(contentType);
	}
	
	boolean isMatchingContentType(String resourceURI) {
		// TODO: Maybe should check for underscore and dot and so on to make sure really at end?
		return resourceURI.endsWith(contentTypeEncoded);
	}

	public boolean maybeProcessResource(Session session, String resourceURI) {
		if (!isMatchingContentType(resourceURI)) return false;
		processResource(session, resourceURI);
		return true;
	}

	private void processResource(Session session, String resourceURI) {
		
	}
}
