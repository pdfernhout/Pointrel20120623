package org.pointrel.pointrel20120623.demos.simplecommunityapp;

import org.pointrel.pointrel20120623.core.Session;

public interface ResourceProcessor {

	public boolean maybeProcessResource(Session session, String resourceURI);
}
