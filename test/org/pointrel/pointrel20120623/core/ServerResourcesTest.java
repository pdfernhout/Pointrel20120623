package org.pointrel.pointrel20120623.core;

import java.util.Arrays;

import junit.framework.TestCase;

public class ServerResourcesTest extends TestCase {
	// TODO: This server should be local
	final String ServerURL = "http://twirlip.com/pointrel/";
	
	// URI for "This is a test"
	public static final String URI_For_This_is_a_test = "pointrel://sha256_c7be1ed902fb8dd4d48997c6452f5d7e509fbcdbe2808b16bcf4edce4c07d14e_14.text%2Fplain";
	public static final String Content_For_This_is_a_test = "This is a test";
	public static final String Content_Type_For_This_is_a_test = "text/plain";
	public static final String Test_user = "unknown_user@example.com";
	
	public void testUploadingResources() {
		Server instance = new Server(ServerURL);
		String result = instance.addContent(Content_For_This_is_a_test.getBytes(), Content_Type_For_This_is_a_test, Test_user, null);
		assertEquals(URI_For_This_is_a_test, result);
	}
	
	public void testGettingResourceAsString() {
		Server instance = new Server(ServerURL);
		String result = instance.getContentForURIAsString(URI_For_This_is_a_test);
		assertEquals(Content_For_This_is_a_test, result);
	}
	
	public void testGettingResourceAsByteArray() {
		Server instance = new Server(ServerURL);
		byte[] result = instance.getContentForURI(URI_For_This_is_a_test);
		assertTrue(Arrays.equals(Content_For_This_is_a_test.getBytes(), result));
	}
	


}
