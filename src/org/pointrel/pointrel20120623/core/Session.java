package org.pointrel.pointrel20120623.core;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class Session {
	final private File archiveDirectory;
	final private String serverURL;
	final private ResourcesInterface resourceFiles;
	final private VariablesInterface variableLogs;
	
	// Ideally this would be final, too; not sure if OK in practice
	private String user;
	
	HashMap<String, byte[]> resourceCache = new HashMap<String, byte[]>();
	
	// Constructors
	
	public Session(File pointrelArchiveDirectory, String user) {
		this.archiveDirectory = pointrelArchiveDirectory;
		this.serverURL = null;
		this.resourceFiles = new ResourceFiles(pointrelArchiveDirectory);
		this.variableLogs = new VariableLogs(pointrelArchiveDirectory);
		this.user = user;
	}
	
	public Session(String serverURL, String user) {
		this.archiveDirectory = null;
		this.serverURL = serverURL;
		Server server = new Server(serverURL);
		this.resourceFiles = server;
		this.variableLogs = server;
		this.user = user;
	}

	// Access

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
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

	// Resources

	public String addContent(byte[] content, String contentType, String precalculatedURI) {
		if (content == null) {
			throw new IllegalArgumentException("content should not be null");
		}
		if (contentType == null) {
			throw new IllegalArgumentException("contentType should not be null");
		}
		if (user == null) {
			throw new IllegalArgumentException("user should not be null");
		}
		try {
			String uri = resourceFiles.addContent(content, contentType, user, precalculatedURI);
			putResourceInCache(content, uri);
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
		byte[] result = getResourceFromCacheOrNull(uri);
		if (result != null) {
			return result;
		}
		try {
			result = resourceFiles.getContentForURI(uri);
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
	
	// Resource caching

	protected void putResourceInCache(byte[] content, String uri) {
		resourceCache.put(uri, content);
	}

	protected byte[] getResourceFromCacheOrNull(String uri) {
		return resourceCache.get(uri);
	}
	
	// Variables
	
	// Each variable should generally represent a workspace

	// TODO: Think about issue of previous value, including if it is needed and where it should come from
	boolean basicSetVariable(String variableName, String newValue, String comment) {
		if (variableName == null) {
			throw new IllegalArgumentException("variableName should not be null");
		}
		// TODO: Are null values for variables actually OK?
		if (newValue == null) {
			throw new IllegalArgumentException("newValue should not be null");
		}
		if (comment == null) {
			throw new IllegalArgumentException("comment should not be null");
		}
		if (user == null) {
			throw new IllegalArgumentException("user should not be null");
		}
		// TODO: Maybe could improve where previous value comes from, like index?
		String previous = variableLogs.basicGetValue(variableName);
		return variableLogs.basicSetValue(variableName, getUser(), previous, newValue, comment);
	}

	String basicGetVariable(String variableName) {
		return variableLogs.basicGetValue(variableName);
	}

	String basicSetContentForVariable(String variableName, byte[] content, String contentType, String comment, String precalculatedURI) {
		String uri = this.addContent(content, contentType, precalculatedURI);
		this.basicSetVariable(variableName, uri, comment);
		System.out.println("Set variable: " + variableName + " with resource URI: " + uri);
		return uri;
	}

	byte[] basicGetContentForVariable(String variableName) {
		String uri = this.basicGetVariable(variableName);
		if (uri == null) return null;
		byte[] content = this.getContentForURI(uri);
		return content;
	}
	

	// Public access for when poking around in other workspaces for utilities or for copying
	public String getVariable(String variableName) {
		if (variableName == null) {
			throw new IllegalArgumentException("variableName should not be null");
		}
		return basicGetVariable(variableName);
	}
}
