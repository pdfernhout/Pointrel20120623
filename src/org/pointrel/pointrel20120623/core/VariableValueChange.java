package org.pointrel.pointrel20120623.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

//TODO: Store count of previous changes
public class VariableValueChange {
	final public static String ContentType = "text/vnd.pointrel.VariableValueChange.json";
	final public static String Version = "20120623.0.1.0";

	final private String variableName;
	final private String timestamp;
	final private String user;
	final private String newValue;
	final private String previousURI;
	final private String comment;
	
	public VariableValueChange(String variableName, String timestamp, String user, String newValue, String previousURI, String comment) {
		// Check for a newline, which is not allowed in variable names or values
		if (variableName.indexOf('\n') != -1) {
			throw new IllegalArgumentException("variable name can not have a newline in it: " + variableName);
		}
		if (variableName.indexOf('\r') != -1) {
			throw new IllegalArgumentException("variable name can not have a return in it: " + variableName);
		}
		if (newValue.indexOf('\n') != -1) {
			throw new IllegalArgumentException("variable value can not have a newline in it: " + newValue);
		}
		if (newValue.indexOf('\r') != -1) {
			throw new IllegalArgumentException("variable value can not have a return in it: " + newValue);
		}
		if (comment.indexOf('\n') != -1) {
			throw new IllegalArgumentException("comment can not have a newline in it: " + comment);
		}
		if (comment.indexOf('\r') != -1) {
			throw new IllegalArgumentException("comment can not have a return in it: " + comment);
		}		
		
		this.variableName = variableName;
		this.timestamp = timestamp;
		this.user = user;
		this.newValue = newValue;
		this.previousURI = previousURI;
		this.comment = comment;
	}

	public VariableValueChange(byte[] content) throws IOException {
		boolean typeChecked = false;
		boolean versionChecked = false;
		String variableName_Read = null;
		String timestamp_Read = null;
		String user_Read = null;
		String newValue_Read = null;
		String previousURI_Read = null;
		String comment_Read = null;
		
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
		JsonParser jsonParser = jsonFactory.createJsonParser(inputStream);
		
		String debugString = new String(content);
		debugString += "";

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
			} else if (fieldName.equals("variableName")) {
				variableName_Read = jsonParser.getText();
			} else if (fieldName.equals("timestamp")) {
				timestamp_Read = jsonParser.getText();
			} else if (fieldName.equals("user")) {
				user_Read = jsonParser.getText();
			} else if (fieldName.equals("newValue")) {
				newValue_Read = jsonParser.getText();
			} else if (fieldName.equals("previousURI")) {
				previousURI_Read = jsonParser.getText();
			} else if (fieldName.equals("comment")) {
				comment_Read = jsonParser.getText();
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
		
		variableName = variableName_Read;
		timestamp = timestamp_Read;
		user = user_Read;
		newValue = newValue_Read;
		previousURI = previousURI_Read;
		comment = comment_Read;
	}

	public byte[] toJSONBytes() {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			JsonFactory jsonFactory = new JsonFactory();
			JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(outputStream, JsonEncoding.UTF8);

			// jsonGenerator.useDefaultPrettyPrinter();
			  
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringField("type", ContentType);
			jsonGenerator.writeStringField("version", Version);
			jsonGenerator.writeStringField("variableName", variableName);
			jsonGenerator.writeStringField("timestamp", timestamp);
			jsonGenerator.writeStringField("user", user);
			jsonGenerator.writeStringField("newValue", newValue);
			jsonGenerator.writeStringField("previousURI", previousURI);
			jsonGenerator.writeStringField("comment", comment);
			jsonGenerator.writeEndObject();
			jsonGenerator.close();
			return outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getLastSegmentOfVariableName() {
		int lastDot = variableName.lastIndexOf(".");
		if (lastDot == -1) return null;
		return variableName.substring(lastDot + 1);
	}

	public String getNewValue() {
		return newValue;
	}

	public String getPreviousURI() {
		return previousURI;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getUser() {
		return user;
	}

	public String getVariableName() {
		return variableName;
	}
	
	public String getComment() {
		return comment;
	}
}
