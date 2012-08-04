package org.pointrel.pointrel20120623.core;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

// TODO: Should do unit tests in some special directory or RAM disk?

public class SessionTest extends TestCase {
	
	final File UnitTestArchive = new File("./UnitTestArchive");
	
	// URI for "This is a test"
	public static final String URI_For_This_is_a_test = "pointrel://sha256_c7be1ed902fb8dd4d48997c6452f5d7e509fbcdbe2808b16bcf4edce4c07d14e_14.text%2Fplain";
	
	public void testCreation() {
		Session instance = new Session(UnitTestArchive, null);
		assertNotNull(instance);
	}
	
	public void testAddingResource() {
		String content = "This is a test";
		String contentType = "text/plain";
		String user = "unknown_user@example.com";
		Session instance = new Session(UnitTestArchive, user);
		String uri = instance.addContent(content, contentType);
		assertNotNull(uri);
		assertTrue(Utility.isValidPointrelURI(uri));
		assertEquals(URI_For_This_is_a_test, uri);
	}
	
	public void testReadingResource() {
		String content = "This is a test";
		//String contentType = "text/plain";
		//String user = "unknown_user@example.com";
		String uri = URI_For_This_is_a_test;
		Session instance = new Session(UnitTestArchive, null);
		byte[] contentRetrieved = instance.getContentForURI(uri);
		assertNotNull(contentRetrieved);
		assertTrue(Arrays.equals(content.getBytes(), contentRetrieved));
	}
	
	public void testBasicSettingVariable() {
		String variableName = "test001";
		String newValue = URI_For_This_is_a_test;
		String user = "unknown_user@example.com";
		String comment = "unit testing";
		Session instance = new Session(UnitTestArchive, user);
		boolean result = instance.basicSetVariable(variableName, newValue, comment);
		assertTrue(result);
	}
	
	public void testBasicGettingVariable() {
		String variableName = "test001";
		String expectedValue = URI_For_This_is_a_test;
		// String user = "unknown_user@example.com";
		Session instance = new Session(UnitTestArchive, null);
		String value = instance.basicGetVariable(variableName);
		assertNotNull(value);
		assertEquals(expectedValue, value);
	}
	
	public void testBasicSettingContentForVariable() throws UnsupportedEncodingException {
		String variableName = "test001";
		String content = "This is a test";
		String contentType = "text/plain";
		String user = "unknown_user@example.com";
		String comment = "unit testing";
		Session instance = new Session(UnitTestArchive, user);
		String uri = instance.basicSetContentForVariable(variableName, content.getBytes("utf-8"), contentType, comment, null);
		assertNotNull(uri);
		assertTrue(Utility.isValidPointrelURI(uri));
		assertEquals(URI_For_This_is_a_test, uri);
	}
	
	// Requires the testBasicSettingContentForVariable test before this to run first
	public void testBasicGettingContentForVariable() {
		String variableName = "test001";
		String content = "This is a test";
		// String user = "unknown_user@example.com";
		Session instance = new Session(UnitTestArchive, null);
		byte[] contentRetrieved = instance.basicGetContentForVariable(variableName);
		assertNotNull(contentRetrieved);
		assertTrue(Arrays.equals(content.getBytes(), contentRetrieved));
	}
		
	public void testSplit() {
		String lines0[] = "".split("\n");
		assertEquals(1, lines0.length);
		
		String lines1[] = "a\nb".split("\n");
		assertEquals(2, lines1.length);
		
		String lines2[] = "a\n\nb".split("\n");
		assertEquals(3, lines2.length);
		
		String lines3[] = "a\n ".split("\n");
		assertEquals(2, lines3.length);
		
		// Would expect this to be 2
		String lines4[] = "a\n".split("\n");
		assertEquals(1, lines4.length);
		
		String lines4a[] = "a\n".split("\n", -1);
		assertEquals(2, lines4a.length);
		
		// Would expect this to be 3 
		String lines5[] = "a\n\n".split("\n");
		assertEquals(1, lines5.length);
		
		String lines5a[] = "a\n\n".split("\n", -1);
		assertEquals(3, lines5a.length);
		
		// Would expect this to be 2
		String lines6[] = "a\n".split("[\n]");
		assertEquals(1, lines6.length);
		
		String lines6a[] = "a\n".split("[\n]", -1);
		assertEquals(2, lines6a.length);
		
		String lines7[] = "aba".split("b");
		assertEquals(2, lines7.length);
		
		String lines8[] = "abba".split("b");
		assertEquals(3, lines8.length);
		
		String lines9[] = "a".split("b");
		assertEquals(1, lines9.length);
		
		// Would expect this to be 2
		String linesA[] = "ab".split("b");
		assertEquals(1, linesA.length);
		
		String linesAa[] = "ab".split("b", -1);
		assertEquals(2, linesAa.length);
		
		// Would expect this to be 3
		String linesB[] = "abb".split("b");
		assertEquals(1, linesB.length);
		
		String linesBa[] = "abb".split("b", -1);
		assertEquals(3, linesBa.length);
	}
	
	public void testGeneratingUUID() {
		String uuid = Utility.generateUUID("test two/three");
		System.out.println("uuid: " + uuid);
		assertNotNull(uuid);
		assertTrue(uuid.startsWith("uuid://"));
		assertTrue(uuid.endsWith(".test+two%2Fthree"));
	}
	
	public void testAddingTransaction() {
		// Keeps adding the same file, so not something that would happen in practice
		String variableName = "test005";
		String user = "unknown_user@example.com";
		String uriToAdd = URI_For_This_is_a_test;
		String comment ="unit testing";
		Session instance = new Session(UnitTestArchive, user);
		String uri = instance.addSimpleTransactionToWorkspace(variableName, uriToAdd, comment);
		assertNotNull(uri);
		
		String value = instance.basicGetVariable(variableName);
		assertEquals(uri, value);
	}
	
	public void testVisitingTransactions() {
		String variableName = "test005";
		Session instance = new Session(UnitTestArchive, null);
		String uri = instance.basicGetVariable(variableName);
		final ArrayList<Transaction> transactionsEntered = new ArrayList<Transaction>();
		final ArrayList<Transaction> transactionsExited = new ArrayList<Transaction>();
		TransactionVisitor visitor = new TransactionVisitor() {
			public boolean transactionEntered(Transaction transaction) {
				// System.out.println("Entering: " + transaction);
				transactionsEntered.add(transaction);
				return false;
			}
			public boolean transactionExited(Transaction transaction) {
				// System.out.println("Exiting: " + transaction);
				transactionsExited.add(transaction);
				return false;
			}
		};
		TransactionVisitor.visitAllResourcesInATransactionTreeRecursively(instance, uri, visitor);
		assertFalse(transactionsEntered.isEmpty());
		assertEquals(transactionsEntered.size(), transactionsExited.size());
		for (int i = 0; i < transactionsEntered.size(); i++) {
			// System.out.println("Comparing :" + i);
			assertEquals(transactionsEntered.get(i), transactionsExited.get(transactionsEntered.size() - 1 - i));
		}
	}
}
