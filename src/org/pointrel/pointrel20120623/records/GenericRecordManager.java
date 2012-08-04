package org.pointrel.pointrel20120623.records;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.pointrel.pointrel20120623.core.TransactionVisitor;
import org.pointrel.pointrel20120623.core.Utility;
import org.pointrel.pointrel20120623.core.Workspace;

public class GenericRecordManager<T extends GenericRecord> {
	public final Workspace workspace;
	public final String version;
	public final String contentType;
	public final String contentTypeEncoded;
	
	// TODO: This could be a map of names to defaults?
	public final HashSet<String> fieldNames;
	// TODO: could have flags for whether OK to read extra fields or OK to have missing fields, and whether to only write declared fields
	
	public GenericRecordManager(Workspace workspace, String contentType, String version, String[] fieldNames) {
		this.workspace = workspace;
		this.contentType = contentType;
		this.contentTypeEncoded = Utility.encodeContentType(contentType);
		this.version = version;
		this.fieldNames = new HashSet<String>(Arrays.asList(fieldNames));
	}
	
	public void saveRecord(T record) {
		String recordURI = workspace.addContent(record.toJSONBytes(), contentType);
		workspace.addSimpleTransaction(recordURI, "Adding " + this.contentTypeEncoded + " for " + record.context);
	}
	
	public class RecordCollector extends TransactionVisitor {
		ArrayList<T> records = new ArrayList<T>();
		final int maximumCount;
		final String contextID;
		
		RecordCollector(String contextID, int maximumCount) {
			this.contextID = contextID;
			this.maximumCount = maximumCount;
		}
		
		// TODO: Maybe should handle removes, too? Tricky as they come before the inserts when recursing
		
		public boolean resourceInserted(String resourceUUID) {
			if (!resourceUUID.endsWith(contentTypeEncoded)) return false;;
			byte[] recordContent = workspace.getContentForURI(resourceUUID);
			T record;
			try {
				record = newRecord(recordContent);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			if (record.context.equals(contextID)) {
				records.add(record);
				if (maximumCount > 0 && records.size() >= maximumCount) return true;
			}
			return false;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public T newRecord(byte[] recordContent) throws IOException {
		// Subclasses would need to override this if subclassing GenericRecord or may produce error
		return (T) new GenericRecord(this, recordContent);
	}
	
	// Finds most recently added version of record
	public T loadRecordForContextID(String contextID) {
		ArrayList<T> records = loadRecordsForContextID(contextID, 1);
		if (records.isEmpty()) return null;
		return records.get(0);
	}
	
	// Finds all added records for a contextID up to a maximumCount (use zero for all)
	public ArrayList<T> loadRecordsForContextID(String contextID, int maximumCount) {
		// TODO: Should create, maintain, and use an index
		String transactionURI = workspace.getLatestTransaction();
		RecordCollector visitor = new RecordCollector(contextID, maximumCount);
		TransactionVisitor.visitAllResourcesInATransactionTreeRecursively(workspace, transactionURI, visitor);
		if (visitor.records.isEmpty()) return null;
		return visitor.records;			
	}
}
