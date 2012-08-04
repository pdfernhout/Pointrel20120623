package org.pointrel.pointrel20120623.indexing;

import java.io.File;

import org.pointrel.pointrel20120623.core.Session;
import org.pointrel.pointrel20120623.indexing.WorkspaceVersion;

import junit.framework.TestCase;

public class WorkspaceVersionTest extends TestCase {
	final File UnitTestArchive = new File("./UnitTestArchive");

	/*
	 * Test method for 'org.pointrel.pointrel20120623.core.Workspace.Workspace(String)'
	 */
	public void testCreatingWorkspaceVersion() {
		String uuid = "uuid:unit_test_workspace";
		WorkspaceVersion instance = new WorkspaceVersion(uuid);
		assertNotNull(instance);
	}
	
	public void testStoringWorkspaceVersion() {
		String uuid = "uuid:unit_test_workspace";
		WorkspaceVersion instance = new WorkspaceVersion(uuid);
		instance.workspaceName = "Unit test workspace";
		instance.workspaceDescription = "This is a test workspace used by the unit tests";
		
		String user = "unknown_user@example.com";
		Session session = new Session(UnitTestArchive, user);
		instance.store(session);
	}
}
