package org.pointrel.pointrel20120623.demos.notebook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

class NoteVersion {
	final public static String ContentType = "text/vnd.pointrel.SimpleNotebookApp.NoteVersion.json";
	final public static String Version = "20120623.0.1.0";
	
	String uri = null;
	final String documentUUID;
	final String timestamp;
	final String userID;
	final String title;
	final String noteBody;
	final String previousVersion;
	
	NoteVersion(String documentUUID, String timestamp, String userID, String title, String noteBody, String previousVersion) {
		this.documentUUID = documentUUID;
		this.timestamp = timestamp;
		this.userID = userID;
		this.title = title;
		this.noteBody = noteBody;
		this.previousVersion = previousVersion;
	}
	
	public NoteVersion(String uri, byte[] content) throws IOException {
		this.uri = uri;
		boolean typeChecked = false;
		boolean versionChecked = false;
		String documentUUID_Read = null;
		String timestamp_Read = null;
		String userID_Read = null;
		String title_Read = null;
		String noteBody_Read = null;
		String previousVersion_Read = null;
		
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
		JsonParser jsonParser = jsonFactory.createJsonParser(inputStream);

		if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected data to start with an Object");
		}
		
		while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
			String fieldName = jsonParser.getCurrentName();
			jsonParser.nextToken();

			if (fieldName.equals("type")) {
				String type = jsonParser.getText();
				if (!ContentType.equals(type)) {
					throw new RuntimeException("Expected type of " + ContentType + "  but got : " + type);
				}
				typeChecked = true;
			} else if (fieldName.equals("version")) {
				String version = jsonParser.getText();
				if (!Version.equals(version)) {
					throw new RuntimeException("Expected version of " + Version + "  but got : " + version);
				}
				versionChecked = true;
			} else if (fieldName.equals("documentUUID")) {
				documentUUID_Read = jsonParser.getText();
			} else if (fieldName.equals("timestamp")) {
				timestamp_Read = jsonParser.getText();
			} else if (fieldName.equals("userID")) {
				userID_Read = jsonParser.getText();
			} else if (fieldName.equals("title")) {
				title_Read = jsonParser.getText();
			} else if (fieldName.equals("noteBody")) {
				noteBody_Read = jsonParser.getText();
			} else if (fieldName.equals("previousVersion")) {
				previousVersion_Read = jsonParser.getText();	
			} else {
				throw new IOException("Unrecognized field '" + fieldName + "'");
			}
		}
		jsonParser.close();
		
		if (!typeChecked) {
			throw new RuntimeException("Expected type of " + ContentType + "  but no type field");
		}
		
		if (!versionChecked) {
			throw new RuntimeException("Expected version of " + Version + "  but no version field");
		}
		
		// previousVersion can be null
		
		documentUUID = documentUUID_Read;
		timestamp = timestamp_Read;
		userID = userID_Read;
		title = title_Read;
		noteBody = noteBody_Read;
		previousVersion = previousVersion_Read;
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
			jsonGenerator.writeStringField("documentUUID", documentUUID);
			jsonGenerator.writeStringField("timestamp", timestamp);
			jsonGenerator.writeStringField("userID", userID);
			jsonGenerator.writeStringField("title", title);
			jsonGenerator.writeStringField("noteBody", noteBody);
			if (previousVersion != null) jsonGenerator.writeStringField("previousVersion", previousVersion);
			jsonGenerator.writeEndObject();
			jsonGenerator.close();
			return outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		return title + " | " + timestamp + " | " + userID;
	}
}