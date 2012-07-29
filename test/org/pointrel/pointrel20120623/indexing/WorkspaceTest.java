package org.pointrel.pointrel20120623.indexing;

import java.io.File;

import org.pointrel.pointrel20120623.core.Session;
import org.pointrel.pointrel20120623.indexing.Workspace;

import junit.framework.TestCase;

public class WorkspaceTest extends TestCase {
	final File UnitTestArchive = new File("./UnitTestArchive");

	/*
	 * Test method for 'org.pointrel.pointrel20120623.core.Workspace.Workspace(String)'
	 */
	public void testCreatingWorkspace() {
		String uuid = "uuid:unit_test_workspace";
		Workspace instance = new Workspace(uuid);
		assertNotNull(instance);
	}
	
	public void testStoringWorkspace() {
		String uuid = "uuid:unit_test_workspace";
		Workspace instance = new Workspace(uuid);
		instance.workspaceName = "Unit test workspace";
		instance.workspaceDescription = "This is a test workspace used by the unit tests";
		
		String user = "unknown_user@example.com";
		Session session = new Session(UnitTestArchive, uuid, user);
		instance.store(session);
	}
}
