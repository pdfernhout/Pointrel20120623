package org.pointrel.pointrel20120623.core;

import java.util.ArrayList;

import junit.framework.TestCase;

public class ServerVariablesTest extends TestCase {
	// TODO: This server should be local
	final String ServerURL = "http://twirlip.com/pointrel/";
	final String TestVariableName = "unit_test_variable_for_unit_testing";
	
	public void testGettingMissingVariable() {
		Server instance = new Server(ServerURL);
		String variableValue = instance.basicGetValue("unit_test_variable_should_not_exist");
		assertNotNull(variableValue);
		assertEquals("", variableValue);
	}
	
	// Must be static as junit creates a new instance of test class for every test
	static String LastValueSet = "";
	
	public void testSettingVariable() {
		Server instance = new Server(ServerURL);
		String oldValue = instance.basicGetValue(TestVariableName);
		assertNotNull(oldValue);
		int integerValue = 0;
		if (oldValue.length() != 0) {
			integerValue = Integer.parseInt(oldValue);
		}
		integerValue++;
		String newValue = "" + integerValue;
		String userID = "unit_testing@example.com";
		String comment = "unit testing";
		boolean result = instance.basicSetValue(TestVariableName, userID, oldValue, newValue, comment);
		assertTrue(result);
		LastValueSet = newValue;
	}
	
	public void testGettingVariable() {
		Server instance = new Server(ServerURL);
		String variableValue = instance.basicGetValue(TestVariableName);
		assertNotNull(variableValue);
		assertEquals(LastValueSet, variableValue);
	}
	
	public void testSettingVariableWithWrongPreviousValue() {
		Server instance = new Server(ServerURL);
		String oldValue = "This is a wrong previous value";
		String newValue = "0";
		String userID = "unit_testing@example.com";
		String comment = "unit testing";
		boolean result = instance.basicSetValue(TestVariableName, userID, oldValue, newValue, comment);
		assertFalse(result);
	}
	
	public void testGettingAllVariableNames() {
		Server instance = new Server(ServerURL);
		ArrayList<String> names = instance.getAllVariableNames();
		assertNotNull(names);
		assertTrue(names.size() > 0);
		assertTrue(names.contains(TestVariableName));
	}
}
