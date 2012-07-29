/**
 * 
 */
package org.pointrel.pointrel20120623.records;

class User {
	final public static String ContentType = "text/vnd.pointrel.User.json";
	final public static String Version = "20120623.0.1.0";
			
	String uuid;
	String publicKey;
	String name;
	String email;
	String homepage;
	String about;
	String sig;
	String communities;
	String interests;
	String other;
	
	// Versioning info
	String loadedFrom;
	String lastUpdatedTimestamp;
	String lastUpdater;
	String previousVersion;
	String digitalSignature;
}