package org.pointrel.pointrel20120623.demos.chat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

class ChatItem {
	final public static String ContentType = "text/vnd.pointrel.SimpleChatApp.ChatItem.json";
	final public static String Version = "20120623.0.1.0";
	
	final String chatUUID;
	final String timestamp;
	final String userID;
	final String chatMessage;
	
	ChatItem(String chatUUID, String timestamp, String userID, String chatMessage) {
		this.chatUUID = chatUUID;
		this.timestamp = timestamp;
		this.userID = userID;
		this.chatMessage = chatMessage;
	}
	
	public ChatItem(byte[] content) throws IOException {
		boolean typeChecked = false;
		boolean versionChecked = false;
		String chatUUID_Read = null;
		String timestamp_Read = null;
		String userID_Read = null;
		String chatMessage_Read = null;
		
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
			} else if (fieldName.equals("chatUUID")) {
				chatUUID_Read = jsonParser.getText();
			} else if (fieldName.equals("timestamp")) {
				timestamp_Read = jsonParser.getText();
			} else if (fieldName.equals("userID")) {
				userID_Read = jsonParser.getText();
			} else if (fieldName.equals("chatMessage")) {
				chatMessage_Read = jsonParser.getText();
			} else {
				throw new IOException("Unrecognized field '" + fieldName + "'");
			}
		}
		jsonParser.close();
		
		if (!typeChecked) {
			throw new RuntimeException("Expected type of " + ContentType + "  but no  field");
		}
		
		if (!versionChecked) {
			throw new RuntimeException("Expected version of " + Version + "  but no version field");
		}
		
		chatUUID = chatUUID_Read;
		timestamp = timestamp_Read;
		userID = userID_Read;
		chatMessage = chatMessage_Read;
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
			jsonGenerator.writeStringField("chatUUID", chatUUID);
			jsonGenerator.writeStringField("timestamp", timestamp);
			jsonGenerator.writeStringField("userID", userID);
			jsonGenerator.writeStringField("chatMessage", chatMessage);
			jsonGenerator.writeEndObject();
			jsonGenerator.close();
			return outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		return timestamp + " | " + userID + " | " + chatMessage;
	}

	public String getLogText() {
		String timestampForLog = timestamp.replace("T", " ");
		timestampForLog = timestampForLog.replace("Z", " GMT");
		timestampForLog = "<" + timestampForLog + ">";

		String message = chatMessage;
		if (!message.endsWith("\n")) message += "\n";
		
		String logText = userID + " "  + timestampForLog + ":\n" + message;
		return logText;
	}
}