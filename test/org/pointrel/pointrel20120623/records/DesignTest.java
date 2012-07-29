package org.pointrel.pointrel20120623.records;

import java.util.ArrayList;

import junit.framework.TestCase;

public class DesignTest extends TestCase {
	
	class HyperdocumentElementVersion {
		String hyperdocumentUUID;
		String elementID;
		String committer;
		String timestamp;
		String resourceUUID;
		
	}
	
	class MyRecord extends HyperdocumentElementVersion {
		String title;
		String body;
		ArrayList<String> otherStuff = new ArrayList<String>();
		
	}
	
	public void testDesign() {
		// Just thinking about design here...
		
		
	}
}
