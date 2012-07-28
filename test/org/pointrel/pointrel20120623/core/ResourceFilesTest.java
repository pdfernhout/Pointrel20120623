package org.pointrel.pointrel20120623.core;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import junit.framework.TestCase;

public class ResourceFilesTest extends TestCase {
	
	final File UnitTestArchive = new File("./UnitTestArchive");
	
	// URI for "This is a test"
	public static final String URI_For_This_is_a_test = "pointrel://sha256_c7be1ed902fb8dd4d48997c6452f5d7e509fbcdbe2808b16bcf4edce4c07d14e_14.text%2Fplain";

	public void testCreation() {
		ResourceFiles instance = new ResourceFiles(UnitTestArchive);
		assertNotNull(instance);
	}
	
	public void testAddingResource() throws IOException {
		String content = "This is a test";
		String contentType = "text/plain";
		String user = "unknown_user@example.com";
		ResourceFiles instance = new ResourceFiles(UnitTestArchive);
		String uri = instance.addContent(content.getBytes("utf-8"), contentType, user, null);
		assertNotNull(uri);
		assertTrue(Utility.isValidPointrelURI(uri));
		assertEquals(URI_For_This_is_a_test, uri);
	}
	
	public void testReadingResource() throws IOException {
		String content = "This is a test";
		//String contentType = "text/plain";
		//String user = "unknown_user@example.com";
		String uri = URI_For_This_is_a_test;
		ResourceFiles instance = new ResourceFiles(UnitTestArchive);
		byte[] contentRetrieved = instance.getContentForURI(uri);
		assertNotNull(contentRetrieved);
		assertTrue(Arrays.equals(content.getBytes("utf-8"), contentRetrieved));
	}
}
