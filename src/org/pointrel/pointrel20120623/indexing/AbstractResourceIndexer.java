package org.pointrel.pointrel20120623.indexing;

import java.io.IOException;

import org.pointrel.pointrel20120623.core.Session;
import org.pointrel.pointrel20120623.core.Utility;

public abstract class AbstractResourceIndexer implements ResourceIndexer {
	String contentTypeEncoded;

	public AbstractResourceIndexer(String contentType) {
		this.contentTypeEncoded = Utility.encodeContentType(contentType);
	}
	
	boolean isMatchingContentType(String resourceURI) {
		// TODO: Maybe should check for underscore and dot and so on to make sure really at end?
		return resourceURI.endsWith(contentTypeEncoded);
	}

	public boolean maybeIndexResource(Session session, String resourceURI) {
		if (!isMatchingContentType(resourceURI)) return false;
		return processResource(session, resourceURI);
	}

	// TODO: Maybe think more about passing around boolean?
	protected boolean processResource(Session session, String resourceURI) {
		byte[] content = session.getContentForURI(resourceURI);
		try {
			processResource(resourceURI, content);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	abstract protected void processResource(String resourceURI, byte[] content) throws IOException;
}
