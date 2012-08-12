package org.pointrel.pointrel20120623.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.PriorityQueue;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

// TODO: Store count of previous transactions
public class Transaction {
	final public static String ContentType = "text/vnd.pointrel.Transaction.json";
	final public static String Version = "20120623.0.4.0";
	
	// TODO: Maybe should not store workspace here but should just check it, and also pass it in when writing?
	final String workspace;
	final String timestamp;
	final String committer;
	final String comment;
	
	// Need uri here so can check it when visiting transactions, but it is awkward to have it final
	String uri = null;

	// Make these PriorityQueues to keep them sorted so one canonical representation of a transaction
	final private PriorityQueue<String> inserts = new PriorityQueue<String>();
	final private PriorityQueue<String> removes = new PriorityQueue<String>();

	// A list of previous transactions that can be considered to be merged into this one
	// Usually there is just one entry here
	final private PriorityQueue<String> priors = new PriorityQueue<String>();
	
	public Transaction(String workspace, String timestamp, String committer, Collection<String> inserts, Collection<String> removes, Collection<String> priors, Collection<String> merges, String comment) {
		this.workspace = workspace;
		this.timestamp = timestamp;
		this.committer = committer;
		if (inserts != null) this.inserts.addAll(inserts);
		if (removes != null) this.removes.addAll(removes);
		if (removes != null) this.priors.addAll(removes);
		this.comment = comment;
	}
	
	// Convenience constructor
	public Transaction(String workspace, String timestamp, String committer, String insert, String previous, String comment) {
		this(workspace, timestamp, committer, nullOrList(insert), null, nullOrList(previous), null, comment);
	}
	
	public static Collection<String> nullOrList(String value) {
		if (value == null) return null;
		return Arrays.asList(value);
	}

	public Transaction(byte[] content, String transactionURI) throws IOException {
		boolean typeChecked = false;
		boolean versionChecked = false;
		String workspace_Read = null;
		String timestamp_Read = null;
		String committer_Read = null;
		String comment_Read = null;
		
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
			} else if (fieldName.equals("workspace")) {
				workspace_Read = jsonParser.getText();
			} else if (fieldName.equals("timestamp")) {
				timestamp_Read = jsonParser.getText();
			} else if (fieldName.equals("committer")) {
				committer_Read = jsonParser.getText();
			} else if (fieldName.equals("comment")) {
				comment_Read = jsonParser.getText();
			} else if (fieldName.equals("inserts")) {
				parseURIArray(jsonParser, inserts);
			} else if (fieldName.equals("removes")) {
				parseURIArray(jsonParser, removes);
			} else if (fieldName.equals("priors")) {
				parseURIArray(jsonParser, priors);	
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
		
		if (workspace_Read == null) {
			throw new RuntimeException("Transaction workspace should not be null");
		}
		if (timestamp_Read == null) {
			throw new RuntimeException("Transaction timestamp should not be null");
		}
		if (committer_Read == null) {
			throw new RuntimeException("Transaction committer should not be null");
		}
		if (comment_Read == null) {
			throw new RuntimeException("Transaction comment should not be null");
		}
		
		// inserts can be missing, if none
		// removes can be missing, if none
		// priors can be missing, if none

		workspace = workspace_Read;
		timestamp = timestamp_Read;
		committer = committer_Read;
		comment = comment_Read;
		uri = transactionURI;
	}

	private void parseURIArray(JsonParser jsonParser, PriorityQueue<String> queue) throws IOException, JsonParseException {
		while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
			String uriRead = jsonParser.getText();
			queue.add(uriRead);
		}
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
			jsonGenerator.writeStringField("workspace", workspace);
			jsonGenerator.writeStringField("timestamp", timestamp);
			jsonGenerator.writeStringField("committer", committer);
			jsonGenerator.writeStringField("comment", comment);
			// jsonGenerator.writeStringField("signature", signature);
			if (!inserts.isEmpty()) writeJSONStringArray(jsonGenerator, "inserts", inserts);
			if (!removes.isEmpty()) writeJSONStringArray(jsonGenerator, "removes", removes);
			if (!priors.isEmpty()) writeJSONStringArray(jsonGenerator, "priors", priors);
			jsonGenerator.writeEndObject();
			jsonGenerator.close();
			return outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected void writeJSONStringArray(JsonGenerator jsonGenerator, String fieldName,  PriorityQueue<String> array) throws IOException {
		jsonGenerator.writeArrayFieldStart(fieldName);
		for (String value: array) {
			jsonGenerator.writeString(value);
		}
		jsonGenerator.writeEndArray();
	}
	
	public String getCommitter() {
		return committer;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public String getComment() {
		return comment;
	}
	
	public ArrayList<String> getInserts() {
		return new ArrayList<String>(inserts);
	}
	
	public ArrayList<String> getRemoves() {
		return new ArrayList<String>(removes);
	}
	
	public ArrayList<String> getPriors() {
		return new ArrayList<String>(priors);
	}
	
	public String getURI() {
		return uri;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	// Extra accessor to avoid copying just to check size
	public boolean hasPriors() {
		return priors.size() > 0;
	}
}
