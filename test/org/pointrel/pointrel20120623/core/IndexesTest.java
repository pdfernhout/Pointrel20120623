package org.pointrel.pointrel20120623.core;

import java.util.ArrayList;

import junit.framework.TestCase;

public class IndexesTest extends TestCase {
	
	public void testCreation() {
		Indexes instance = new Indexes();
		assertNotNull(instance);
	}
	
	public void testAddingToIndex() {
		String indexName = "testIndex";
		String indexKey = "testKey";
		String indexValueToAdd = "testValue";
		Indexes instance = new Indexes();
		instance.indexAdd(indexName, indexKey, indexValueToAdd);
	}
	
	public void testLookingUpFromIndex() {
		String indexName = "testIndex";
		String indexKey = "testKey";
		String indexValueToAdd1 = "testValue1";
		String indexValueToAdd2 = "testValue2";
		Indexes instance = new Indexes();
		instance.indexAdd(indexName, indexKey, indexValueToAdd1);
		instance.indexAdd(indexName, indexKey, indexValueToAdd2);
		ArrayList<String> list = instance.indexGet(indexName, indexKey);
		assertNotNull(list);
		assertEquals(2, list.size());
		assertEquals(indexValueToAdd1, list.get(0));
		assertEquals(indexValueToAdd2, list.get(1));
	}
}
