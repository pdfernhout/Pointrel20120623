package org.pointrel.pointrel20120623.core;

import junit.framework.TestCase;

public class UtilityTest extends TestCase {
	public void testEncodingAndDecoding() {
		String stringToEncode = "text/plain";
		String expectedEncodedResult = "text%2Fplain";
		String encodedResult = Utility.encodeContentType(stringToEncode);
		assertEquals(expectedEncodedResult, encodedResult);
		String decodedResult = Utility.decodeContentType(encodedResult);
		assertEquals(stringToEncode, decodedResult);
	}
	
	public void testGenerateUUID() {
		String uuid1 = Utility.generateUUID("test");
		String uuid2 = Utility.generateUUID("test");
		assertTrue(uuid1.startsWith("uuid://"));
		assertTrue(uuid1.endsWith(".test"));
		assertFalse(uuid1.equals(uuid2));
	}
	
	public void testContentTypeForURI() {
		String uri = SessionTest.URI_For_This_is_a_test;
		String expectedContentType = "text/plain";
		String contentType = Utility.contentTypeForURI(uri);
		assertEquals(expectedContentType, contentType);
	}
	
	public void testIsValidPointrelURI() {
		assertTrue(Utility.isValidPointrelURI(SessionTest.URI_For_This_is_a_test));
		assertTrue(Utility.isValidPointrelURIOrNull(SessionTest.URI_For_This_is_a_test));
		assertTrue(Utility.isValidPointrelURI(SessionTest.URI_For_This_is_a_test + ".more.and.more"));
		assertFalse(Utility.isValidPointrelURI(null));
		assertTrue(Utility.isValidPointrelURIOrNull(null));
		assertFalse(Utility.isValidPointrelURI("uuid://1234.test"));
		assertFalse(Utility.isValidPointrelURI("pointrel://1234.test"));
	}
	
	public void testGetExtension() {
		assertEquals("text%2Fplain", Utility.getExtension(SessionTest.URI_For_This_is_a_test));
		assertEquals("text%2Fplain" + ".more.and.more", Utility.getExtension(SessionTest.URI_For_This_is_a_test + ".more.and.more"));
	}
	
	public void testEscapeLine() {
		String input = "This is \n a test with a backslash \\";
		String expectedOutput = "This is \\n a test with a backslash \\\\";
		String output = Utility.escapeLine(input);
		assertEquals(expectedOutput, output);
	}
	
	public void testUnescapeLine() {
		String input = "This is \\n a test with a backslash \\\\";
		String expectedOutput = "This is \n a test with a backslash \\";
		String output = Utility.unescapeLine(input);
		assertEquals(expectedOutput, output);
	}
}
