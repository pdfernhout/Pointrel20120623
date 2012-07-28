package org.pointrel.pointrel20120623.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/*
 * Workspaces are used by one or more people to 
 * collect, share, organize, discuss, and publish information.
 * Each unique workspace is defined by a unique UUID.
 */
public class Workspace {
	final public static String ContentType = "text/vnd.pointrel.Workspace.json";
	final public static String Version = "20120623.0.1.0";
	
	String workspaceUUID;
	String workspaceVariable;
	String workspaceName;
	String workspaceDescription;
	// String myUserID;
	
	// Versioning info
	String loadedFrom;
	String derivedFrom;
	String timestampWhenStored;
	String userWhoStored;
	
	Indexes indexes = new Indexes();
	
	public Workspace(String uuid) {
		this.workspaceUUID = uuid;
	}

	public void store(Session session) {
		
	}
	
	public byte[] toJSONBytes() {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			JsonFactory jsonFactory = new JsonFactory();
			JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(outputStream, JsonEncoding.UTF8);

			jsonGenerator.useDefaultPrettyPrinter();
			  
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringField("type", ContentType);
			jsonGenerator.writeStringField("version", Version);
			jsonGenerator.writeStringField("uuid", workspaceUUID);
			jsonGenerator.writeStringField("name", workspaceName);
			jsonGenerator.writeStringField("description", workspaceDescription);
			// TODO: More details about who is making the change when and from what previous version?
			// jsonGenerator.writeStringField("timestamp", timestamp);
			// jsonGenerator.writeStringField("user", user);
			// jsonGenerator.writeStringField("previous", previous);
			jsonGenerator.writeEndObject();
			jsonGenerator.close();
			return outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
