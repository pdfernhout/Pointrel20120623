package org.pointrel.pointrel20120623.indexing;

import org.pointrel.pointrel20120623.core.Session;

public interface ResourceIndexer {

	public boolean maybeIndexResource(Session session, String resourceURI);
}
