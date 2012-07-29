/**
 * 
 */
package org.pointrel.pointrel20120623.demos.simplecommunityapp;

import java.util.ArrayList;

public class Community {
	final String communityUUID;
	// String publicKey;
	String name = "Test Name Community (TODO)";
	String about = "This is a great place to live (TODO)";
	
	final ArrayList<User> users = new ArrayList<User>();
	final ArrayList<Membership> memberships = new ArrayList<Membership>();
	final ArrayList<DocumentVersion> documentVersions = new ArrayList<DocumentVersion>();
	final ArrayList<Message> messages = new ArrayList<Message>();
	final ArrayList<TagEvent> tagEvents = new ArrayList<TagEvent>();
	final ArrayList<CommunityAssociation> communityAssociations = new ArrayList<CommunityAssociation>();

	public Community(String communityUUID) {
		this.communityUUID = communityUUID;
	}
}