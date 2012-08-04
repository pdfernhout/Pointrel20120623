package org.pointrel.pointrel20120623.records;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.pointrel.pointrel20120623.core.Utility;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Support having an arbitrary list of data mapping field names to values.
 * 
 * @author Paul Fernhout
 */
public class GenericRecord {
	@SuppressWarnings("rawtypes")
	protected final GenericRecordManager recordFactory;
	protected final String context;
	protected final String document;
	protected final String timestamp;
	protected final String committer;
	protected final HashMap<String,String> values = new HashMap<String,String>();
		
	public GenericRecord(GenericRecordManager<GenericRecord> recordFactory, String context, String document, String timestamp, String committer, Map<String,String> values) {
		this.recordFactory = recordFactory;
		this.context = context;
		this.document = document;
		this.timestamp = timestamp;
		this.committer = committer;
		// TODO: Could check fields to see if match field names in factory
		values.putAll(values);
	}
	
	public GenericRecord(GenericRecordManager<GenericRecord> recordFactory, String context, String document, Map<String,String> values) {
		this(recordFactory, context, document, Utility.currentTimestamp(), recordFactory.workspace.getUser(), values);
	}
	
	@SuppressWarnings({ "rawtypes" })
	public GenericRecord(GenericRecordManager genericRecordManager, byte[] content) throws IOException {
		this.recordFactory = genericRecordManager;
		
		boolean typeChecked = false;
		boolean versionChecked = false;
		String context_Read = null;
		String document_Read = null;
		String timestamp_Read = null;
		String committer_Read = null;
		
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
				if (!genericRecordManager.contentType.equals(type)) {
					throw new RuntimeException("Expected type of " +  genericRecordManager.contentType + "  but got : " + type);
				}
				typeChecked = true;
			} else if (fieldName.equals("version")) {
				String version = jsonParser.getText();
				if (!genericRecordManager.version.equals(version)) {
					throw new RuntimeException("Expected version of " + genericRecordManager.version + "  but got : " + version);
				}
				versionChecked = true;
			} else if (fieldName.equals("context")) {
				context_Read = jsonParser.getText();
			} else if (fieldName.equals("document")) {
				document_Read = jsonParser.getText();
			} else if (fieldName.equals("timestamp")) {
				timestamp_Read = jsonParser.getText();
			} else if (fieldName.equals("committer")) {
				committer_Read = jsonParser.getText();
			} else if (fieldName.equals("values")) {
				readValuesMap(jsonParser);
			} else { 
				throw new IOException("Unrecognized field '" + fieldName + "'");
			}
		}
		jsonParser.close();
		
		if (!typeChecked) {
			throw new RuntimeException("Expected type of " + genericRecordManager.contentType + "  but no type field");
		}
		
		if (!versionChecked) {
			throw new RuntimeException("Expected version of " + genericRecordManager.version + "  but no version field");
		}

		context = context_Read;
		document = document_Read;
		committer = committer_Read;
		timestamp = timestamp_Read;
	}


	private void readValuesMap(JsonParser jsonParser) throws IOException {
		while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
			String fieldName = jsonParser.getCurrentName();
			// TODO: Could add some kind of validation here, relying on factory list of field names
			jsonParser.nextToken();
			String value = jsonParser.getText();
			this.values.put(fieldName, value);
		}
	}
	
	public byte[] toJSONBytes() {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			JsonFactory jsonFactory = new JsonFactory();
			JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(outputStream, JsonEncoding.UTF8);

			jsonGenerator.useDefaultPrettyPrinter();
			  
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringField("type", recordFactory.contentType);
			jsonGenerator.writeStringField("version", recordFactory.version);
			jsonGenerator.writeStringField("context", context);
			jsonGenerator.writeStringField("document", document);
			jsonGenerator.writeStringField("timestamp", timestamp);
			jsonGenerator.writeStringField("committer", committer);
			
			// write values map
			jsonGenerator.writeObjectFieldStart("values");
			for (Map.Entry<String,String> entry: values.entrySet()) {
				jsonGenerator.writeStringField(entry.getKey(), entry.getValue());
			}
			jsonGenerator.writeEndObject();
			
			jsonGenerator.writeEndObject();
			jsonGenerator.close();
			return outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		// TODO: Could have URI here
		return "<Record " + recordFactory.contentType + " | " + timestamp + " | " + committer + ">";
	}
	
	@SuppressWarnings("rawtypes")
	public GenericRecordManager getRecordFactory() {
		return recordFactory;
	}


	public String getContext() {
		return context;
	}
	
	public String getDocument() {
		return document;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getCommitter() {
		return committer;
	}

	public HashMap<String, String> getValues() {
		return values;
	}
	
	public String getValue(String fieldName) {
		return values.get(fieldName);
	}	
}
