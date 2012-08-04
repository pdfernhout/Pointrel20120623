package org.pointrel.pointrel20120623.indexing;

import org.pointrel.pointrel20120623.core.Workspace;

public interface ResourceIndexer {

	public boolean maybeIndexResource(Workspace workspace, String resourceURI);
}
