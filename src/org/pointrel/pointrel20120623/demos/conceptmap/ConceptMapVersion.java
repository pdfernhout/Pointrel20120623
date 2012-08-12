package org.pointrel.pointrel20120623.demos.conceptmap;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;


import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class ConceptMapVersion {
	final public static String ContentType = "text/vnd.pointrel.SimpleConceptMapApp.ConceptMapVersion.json";
	final public static String Version = "20120623.0.1.0";
	
	String uri = null;
	final String documentUUID;
	final String timestamp;
	final String userID;
	final String title;
	final String noteBody;
	final String previousVersion;
	
	public ArrayList<ConceptDrawable> drawables = new ArrayList<ConceptDrawable>();
	
	// From: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6352385
	private String encodeColor(Color c) {
		char[] buf = new char[7];
		buf[0] = '#';
		String s = Integer.toHexString(c.getRed());
		if (s.length() == 1) {
			buf[1] = '0';
			buf[2] = s.charAt(0);
		} else {
			buf[1] = s.charAt(0);
			buf[2] = s.charAt(1);
		}
		s = Integer.toHexString(c.getGreen());
		if (s.length() == 1) {
			buf[3] = '0';
			buf[4] = s.charAt(0);
		} else {
			buf[3] = s.charAt(0);
			buf[4] = s.charAt(1);
		}
		s = Integer.toHexString(c.getBlue());
		if (s.length() == 1) {
			buf[5] = '0';
			buf[6] = s.charAt(0);
		} else {
			buf[5] = s.charAt(0);
			buf[6] = s.charAt(1);
		}
		return String.valueOf(buf);
	}
	
	public ConceptMapVersion(String documentUUID, String timestamp, String userID, String title, String noteBody, String previousVersion) {
		if (documentUUID == null) {
			throw new RuntimeException("documentUUID should not be null");
		}
		this.documentUUID = documentUUID;
		this.timestamp = timestamp;
		this.userID = userID;
		this.title = title;
		this.noteBody = noteBody;
		this.previousVersion = previousVersion;
	}
	
	public ConceptMapVersion(String uri, byte[] content) throws IOException {
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
			} else if (fieldName.equals("drawables")) {
				while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
					String concept_Read = null;
					String uuid_Read = null;
					String color_Read = null;
					String rectangle_Read = null;
					while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
						String drawableFieldName = jsonParser.getCurrentName();
						jsonParser.nextToken();
						if (drawableFieldName.equals("uuid")) {
							uuid_Read = jsonParser.getText();
						} else if (drawableFieldName.equals("concept")) {
							concept_Read = jsonParser.getText();
						} else if (drawableFieldName.equals("color")) {
							color_Read = jsonParser.getText();
						} else if (drawableFieldName.equals("rectangle")) {
							rectangle_Read = jsonParser.getText();
						}
					}
					// TODO: Check all four fields were read
					Color color = Color.decode(color_Read);
					Rectangle rectangle = decodeRectangle(rectangle_Read);
					ConceptDrawable drawable = new ConceptDrawable(uuid_Read, concept_Read, color, rectangle);
					drawables.add(drawable);
				}
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
		
		// TODO: Maybe different error handling for this condition?
		if (documentUUID_Read == null) {
			throw new RuntimeException("The field documentUUID read should not be null");
		}
		
		// Previous version can be null
		
		documentUUID = documentUUID_Read;
		timestamp = timestamp_Read;
		userID = userID_Read;
		title = title_Read;
		noteBody = noteBody_Read;
		previousVersion = previousVersion_Read;
	}

	private Rectangle decodeRectangle(String stringWithFourIntegers) {
		String[] parts = stringWithFourIntegers.split(" ");
		if (parts.length != 4) throw new RuntimeException("Problem reading rectangle");
		Rectangle rectangle = new Rectangle(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
		return rectangle;
	}

	byte[] toJSONBytes() {
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
			
			jsonGenerator.writeArrayFieldStart("drawables");
			for (ConceptDrawable drawable: drawables) {
				jsonGenerator.writeStartObject();
				jsonGenerator.writeStringField("uuid", drawable.uuid);
				jsonGenerator.writeStringField("concept", drawable.concept);
				jsonGenerator.writeStringField("color", encodeColor(drawable.color));
				Rectangle rectangle = drawable.rectangle;
				jsonGenerator.writeStringField("rectangle", "" + rectangle.x + " " + rectangle.y + " " + rectangle.width + " " + rectangle.height);
				jsonGenerator.writeEndObject();
			}
			
			jsonGenerator.writeEndArray();
			jsonGenerator.writeEndObject();
			jsonGenerator.close();
			return outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}