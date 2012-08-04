package org.pointrel.pointrel20120623.records;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.pointrel.pointrel20120623.core.TransactionVisitor;
import org.pointrel.pointrel20120623.core.Utility;
import org.pointrel.pointrel20120623.core.Workspace;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

// Collects all the documentUUIDs for objects of a content type that are parseable as JSON
public class UUIDCollector extends TransactionVisitor {
	final Workspace workspace;
	final String encodedContentType;
	final HashSet<String> uuids = new HashSet<String>();
	final int maximumCount;
	final JsonFactory jsonFactory = new JsonFactory();
	
	private UUIDCollector(Workspace workspace, String contentType, int maximumCount) {
		this.workspace = workspace;
		this.encodedContentType = Utility.encodeContentType(contentType);
		this.maximumCount = maximumCount;
	}
	
	// TODO: Maybe should handle removes, too? Tricky as they come before the inserts when recursing
	
	public boolean resourceInserted(String resourceUUID) {
		if (!resourceUUID.endsWith(encodedContentType)) return false;
		byte[] content = workspace.getContentForURI(resourceUUID);
		String documentUUID_Read;
		try {
			documentUUID_Read = readDocumentUUID(content);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (documentUUID_Read != null) uuids.add(documentUUID_Read);
		if (maximumCount > 0 && uuids.size() >= maximumCount) return true;
		return false;
	}
	
	String readDocumentUUID(byte[] content) throws IOException {	
		ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
		JsonParser jsonParser = jsonFactory.createJsonParser(inputStream);

		if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected data to start with an Object");
		}
		
		while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
			String fieldName = jsonParser.getCurrentName();
			jsonParser.nextToken();
			if (fieldName.equals("documentUUID")) {
				return jsonParser.getText();
			} 
		}
		jsonParser.close();
		return null;
	}
	
	// Finds all uuids for items up to a maximumCount (use zero for all)
	static public Set<String> collectUUIDs(Workspace workspace, String contentType, int maximumCount) {
		// TODO: Should create, maintain, and use an index
		UUIDCollector collector = new UUIDCollector(workspace, contentType, maximumCount);
		String transactionURI = workspace.getLatestTransactionForWorkspace();
		TransactionVisitor.visitAllResourcesInATransactionTreeRecursively(workspace, transactionURI, collector);
		if (collector.uuids.isEmpty()) return new HashSet<String>();
		return new HashSet<String>(collector.uuids);			
	}
}
