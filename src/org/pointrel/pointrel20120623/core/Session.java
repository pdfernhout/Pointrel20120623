package org.pointrel.pointrel20120623.core;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class Session {
	final File archiveDirectory;
	final String serverURL;
	String user;
	String workspaceVariable;
	ResourcesInterface resourceFiles;
	VariablesInterface variableLogs;
	Indexes indexes;
	
	HashMap<String, byte[]> resourceCache = new HashMap<String, byte[]>();
	
	// TODO: Really should remove this and maybe not have a default?
	public static String DefaultWorkspaceVariable = "default_workspace";
	
	public Session(File pointrelArchiveDirectory) {
		this.archiveDirectory = pointrelArchiveDirectory;
		this.serverURL = null;
		resourceFiles = new ResourceFiles(pointrelArchiveDirectory);
		variableLogs = new VariableLogs(pointrelArchiveDirectory);
		indexes = new Indexes();
		workspaceVariable = DefaultWorkspaceVariable;
	}
	
	public Session(String serverURL) {
		this.archiveDirectory = null;
		this.serverURL = serverURL;
		Server server = new Server(serverURL);
		resourceFiles = server;
		variableLogs = server;
		indexes = new Indexes();
		workspaceVariable = DefaultWorkspaceVariable;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		// TODO: Temporary for debugging?
		if (user == null) {
			throw new RuntimeException("user was not set");
		}
		return user;
	}
	
	public String getWorkspaceVariable() {
		// TODO: Temporary for debugging?
		if (workspaceVariable == null) {
			throw new RuntimeException("Workspace variable was not set");
		}
		return workspaceVariable;
	}

	public void setWorkspaceVariable(String workspaceVariable) {
		this.workspaceVariable = workspaceVariable;
	}

	public String addContent(byte[] content, String contentType, String precalculatedURI) {
		try {
			String uri = resourceFiles.addContent(content, contentType, getUser(), precalculatedURI);
			resourceCache.put(uri, content);
			return uri;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// Convenience method for above
	public String addContent(byte[] content, String contentType) {
		return addContent(content, contentType, null);
	}
	
	// Convenience method for above
	public String addContent(String content, String contentType) {
		return addContent(content.getBytes(), contentType, null);
	}

	public byte[] getContentForURI(String uri) {
		try {
			byte[] result = resourceCache.get(uri);
			if (result == null) {
				result = resourceFiles.getContentForURI(uri);
			} 
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// Convenience method for above
	public String getContentForURIAsString(String uri) {
		byte[] content = this.getContentForURI(uri);
		if (content == null) return null;
		try {
			return new String(content, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// Variables

	public boolean basicSetVariable(String variableName, String newValue, String comment) {
		// TODO: Maybe could improve where previous value comes from, like index?
		String previous = variableLogs.basicGetValue(variableName);
		return variableLogs.basicSetValue(variableName, getUser(), previous, newValue, comment);
	}

	public String basicGetVariable(String variableName) {
		return variableLogs.basicGetValue(variableName);
	}

	public String basicSetContentForVariable(String variableName, byte[] content, String contentType, String comment, String precalculatedURI) {
		String uri = this.addContent(content, contentType, precalculatedURI);
		this.basicSetVariable(variableName, uri, comment);
		System.out.println("Set variable: " + variableName + " with resource URI: " + uri);
		return uri;
	}

	public byte[] basicGetContentForVariable(String variableName) {
		String uri = this.basicGetVariable(variableName);
		if (uri == null) return null;
		byte[] content = this.getContentForURI(uri);
		return content;
	}
	
	// Supports an extra level of chained indirection, where each variable setting is stored as a resource referring to a previous resource
	public String setVariable(String variableName, String newValue, String comment) {
		String previousURI = this.basicGetVariable(variableName);
		if (previousURI == null) previousURI = "";
		
		String timestamp = Utility.currentTimestamp();
		String user = this.getUser();
		VariableValueChange change = new VariableValueChange(variableName, timestamp, user, newValue, previousURI, comment);
		
		// String variableValueChangeURI = this.addContent(change.getContent(), VariableValueChange.VariableContentType);
		
		// TODO: Change this to write out JSON for each log entry, wrapped by JSON object with reference to change uuid
		// TODO: Update PHP as well for that format
		// Define a single-line resource to add
		byte[] content = change.toJSONBytes();

		String uriWritten = this.basicSetContentForVariable(variableName, content, VariableValueChange.ContentType, comment, null);
		
		return uriWritten;
	}
	
	// Supports an extra level of chained indirection, where each variable setting is stored as a resource referring to a previous resource
	public String getVariable(String variableName) {
		// Check for a newline, which is not allowed in variable names or values
		if (variableName.indexOf('\n') != -1) {
			throw new IllegalArgumentException("variable name can not have a newline in it: " + variableName);
		}

		// Need to do seperate steps so can retain uri to pass on, instead of basicGetContentForVariable
		String uri = this.basicGetVariable(variableName);
		if (uri == null || uri.length() == 0) return null;
		byte[] content = this.getContentForURI(uri);
		// TODO: Maybe better error reporting here, as this should not happen?
		if (content == null) return null;
		
		VariableValueChange change;
		try {
			change = new VariableValueChange(content);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not read VariableValueChange: " + uri, e);
		}
		
		if (!change.getVariableName().equals(variableName)) {
			throw new RuntimeException("variable definition content name: " + change.getVariableName() + " does not match expected: " + variableName);
		}
		return change.getNewValue();
	}

	public String setContentForVariable(String variableName, String content, String contentType, String comment) {
		String uri = this.addContent(content, contentType);
		if (uri == null) {
			throw new RuntimeException("Could not store resource");
		}
		this.setVariable(variableName, uri, comment);
		System.out.println("Added resource with URI: " + uri);
		return uri;
	}

	public byte[] getContentForVariable(String variableName) {
		String uri = this.getVariable(variableName);
		if (uri == null) {
			return null;
		}
		byte[] content = this.getContentForURI(uri);
		return content;
	}
	
//	public ArrayList<String> addToListForVariable(String variableName, String listItem, String comment) {
//		// First check if item has a newline, which is not allowed in this simple list approach
//		if (listItem.indexOf('\n') != -1) {
//			throw new IllegalArgumentException("listItem can not have a newline in it: " + listItem);
//		}
//		
//		byte[] oldContent = this.getContentForVariable(variableName);
//		ArrayList<String> list = Utility.listForContent(oldContent, ListHeader);
//		list.add(listItem);
//		byte[] newContent = Utility.contentForList(list, ListHeader);
//
//		String uri = this.addContent(newContent, ListContentType);
//		this.setVariable(variableName, uri, comment);
//		return list;
//	}
//
//	@SuppressWarnings("unchecked")
//	public ArrayList<String> getListForVariable(String variableName) {
//		byte[] content = this.getContentForVariable(variableName);
//		return Utility.listForContent(content, ListHeader);
//	}

	// This does not check if user might be out-of-date in multi-user system
	public String addSimpleTransactionForVariable(String variableName, String uriToAdd, String comment) {
		String previousTransaction = this.getVariable(variableName);
		Transaction transaction = new Transaction(Utility.currentTimestamp(), this.user, uriToAdd, null, previousTransaction);
		String transactionURI = addContent(transaction.toJSONBytes(), Transaction.ContentType);
		System.out.println("URI for transaction: " + transactionURI);
		this.setVariable(variableName, transactionURI, comment);
		return transactionURI;
	}
	
	public byte[] getResourceInSimpleTransactionForVariable(String variableName) {
		String transactionURI = this.getVariable(variableName);
		if (transactionURI == null) return null;
		byte[] transactionBytes = this.getContentForURI(transactionURI);
		if (transactionBytes == null) return null;
		Transaction transaction;
		try {
			transaction = new Transaction(transactionBytes);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Problem parsing transaction for: " + transactionURI);
		}
		if (transaction.getInserts().size() != 1) {
			throw new RuntimeException("Not exactly one insert in transaction: " + transaction.getInserts().size());
		}
		String resourceURI = transaction.getInserts().get(0);
		System.out.println("resourceURI from transaction: " + resourceURI);
		byte[] resourceBytes = this.getContentForURI(resourceURI);
		return resourceBytes;
	}

	public ArrayList<String> getAllVariableNames() {
		return variableLogs.getAllVariableNames();
	}

	public File getArchiveDirectory() {
		return archiveDirectory;
	}
	
	public String getServerURL() {
		return serverURL;
	}
}
