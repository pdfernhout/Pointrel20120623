package org.pointrel.pointrel20120623.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

public class TransactionTest extends TestCase {

	public void testToJSON() throws UnsupportedEncodingException {
		Transaction instance = new Transaction(Session.DefaultWorkspaceVariable, Utility.currentTimestamp(), "tester@example.com", "url", null, null, "unit testing");
		byte[] bytes = instance.toJSONBytes();
		assertNotNull(bytes);
		assertTrue(bytes.length > 0);
		String resultString = new String(bytes, "utf-8");
		assertNotNull(resultString);
	}

	public void testFromJSON() throws IOException {
		String timestamp = Utility.currentTimestamp();
		Transaction instance = new Transaction(Session.DefaultWorkspaceVariable, timestamp, "tester@example.com", "url", null, null, "unit testing");
		byte[] bytes = instance.toJSONBytes();
		Transaction instance2 = new Transaction(bytes);
		
		assertEquals(1, instance2.getInserts().size());
		assertEquals("url", instance2.getInserts().get(0));
		
		assertEquals(0, instance2.getRemoves().size());
		
		assertEquals(0, instance2.getIncludes().size());
		
		assertEquals("tester@example.com", instance2.getCommitter());
		
		assertEquals(timestamp, instance2.getTimestamp());
	}
}
