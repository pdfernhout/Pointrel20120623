package org.pointrel.pointrel20120623.core;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

public class VariableLogsTest extends TestCase {
	
	final File UnitTestArchive = new File("./UnitTestArchive");
	
	public void testCreation() {
		VariableLogs instance = new VariableLogs(UnitTestArchive);
		assertNotNull(instance);
	}
	
	public void testWritingVariableDirectly() {
		String variableName = "test001";
		String newValue = SessionTest.URI_For_This_is_a_test;
		String user = "unknown_user@example.com";
		String comment = "unit testing VariablesTest";
		VariableLogs instance = new VariableLogs(UnitTestArchive);
		String logEntry = instance.writeVariable(variableName, user, null, newValue, comment);
		assertNotNull(logEntry);
	}
	
	public void testSettingVariable() {
		String variableName = "test001";
		String newValue = SessionTest.URI_For_This_is_a_test;
		String user = "unknown_user@example.com";
		String comment = "unit testing VariablesTest";
		VariableLogs instance = new VariableLogs(UnitTestArchive);
		boolean result = instance.basicSetValue(variableName, user, null, newValue, comment);
		assertTrue(result);
	}
	
	// Assuming this test runs after the other
	public void testReadingVariableDirectly() {
		String variableName = "test001";
		// String user = "unknown_user@example.com";
		VariableLogs instance = new VariableLogs(UnitTestArchive);
		String value = instance.readFromFileIfAvailable(variableName);
		assertNotNull(value);
		System.out.println("got: " + value);
		assertTrue(Utility.isValidPointrelURI(value));
	}
	
	public void testGettingVariable() {
		String variableName = "test001";
		String expectedValue = SessionTest.URI_For_This_is_a_test;
		// String user = "unknown_user@example.com";
		VariableLogs instance = new VariableLogs(UnitTestArchive);
		String value = instance.basicGetValue(variableName);
		assertNotNull(value);
		assertEquals(expectedValue, value);
	}
	
	public void testListingVariables() {
		VariableLogs instance = new VariableLogs(UnitTestArchive);
		ArrayList<String> variables = instance.getAllVariableNames();
		assertNotNull(variables);
		for (String variableName: variables) {
			System.out.println("Variable: " + variableName);
		}
	}
}
