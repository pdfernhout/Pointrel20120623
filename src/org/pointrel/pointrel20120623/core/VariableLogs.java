package org.pointrel.pointrel20120623.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

/*
 * @deprecated
 */
public class VariableLogs implements VariablesInterface {
	// HashMap<String,String> variables = new HashMap<String,String>(); 
	final static String VariableLogFileHeader = "#Pointrel20120531-VariableLogFile-v0.0.1";
	
	final public File variablesBaseDirectory;
	boolean mayNeetToCreateDirectory = true;
	
	public VariableLogs(File variablesBaseDirectory) {
		if (variablesBaseDirectory == null) variablesBaseDirectory = new File("./PointrelArchive/variables");
		if (variablesBaseDirectory.getName() != "variables") {
			variablesBaseDirectory = new File(variablesBaseDirectory, "variables");
		}
		this.variablesBaseDirectory = variablesBaseDirectory;
	}
	
	/* (non-Javadoc)
	 * @see org.pointrel.pointrel20120623.core.VariablesInterface#getLastValueFromLog(java.lang.String)
	 */
	public String basicGetValue(String variableName) {
		if (variableName == null) return null;
		// if (variables.containsKey(variableName)) return variables.get(variableName);
		return this.readFromFileIfAvailable(variableName);
	}
	
	/* (non-Javadoc)
	 * @see org.pointrel.pointrel20120623.core.VariablesInterface#addToLog(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean basicSetValue(String variableName, String user, String previous, String value, String comment) {
		if (variableName == null) {
			throw new IllegalArgumentException("variableName should not be null");
		}
		// check if value is OK
		if (!Utility.isValidPointrelURIOrNull(value)) {
			throw new RuntimeException("New variable value not pointrel reference or null: " + previous);
		}
		// check if previous value is OK
		if (!Utility.isValidPointrelURIOrNull(previous)) {
			throw new RuntimeException("Previous value not pointrel reference or null: " + previous);
		}
		this.writeVariable(variableName, user, previous, value, comment);
		// variables.put(variableName, value);
		return true;
	}
	
	// Supporting methods
	
	File fileForVariableLog(String variableName) {
		String encodededVariableName = Utility.encodeContentType(variableName);
		File variablesLogFile = new File(variablesBaseDirectory, "pv_" + encodededVariableName + ".log");
		return variablesLogFile;
	}
	
	void createDirectoryIfNeeded() {
		if (this.mayNeetToCreateDirectory) {
			this.mayNeetToCreateDirectory = false;
			this.variablesBaseDirectory.mkdirs();
		}
	}

	/*
	 * Write the new variable value at the end of a similarly named log file, creating it if needed"
	 * @param variableName the variable to write
	 * @param newValue the new value to set the variable to
	 * @param user the user making the change
	 * @param previous the previous value of the variable for consistanecy
	 * @comment why the change was made
	 */
	String writeVariable(String variableName, String user, String previous, String newValue, String comment) {
		createDirectoryIfNeeded();
		if (!Utility.isValidPointrelURIOrNull(newValue)) {
			throw new IllegalArgumentException("Variable value should be a pointrel reference or null: " + newValue);
		}
		if (newValue.indexOf(" ") != -1) {
			throw new IllegalArgumentException("Variable value should not have a space in it");
		}
		if (newValue.indexOf("\n") != -1) {
			throw new IllegalArgumentException("Variable value should not have a newline in it");
		}
		if (comment.indexOf("\n") != -1) {
			throw new IllegalArgumentException("Comment should not have a newline in it");
		}
		
		File variablesLogFile = fileForVariableLog(variableName);		
		
		String timestamp = Utility.currentTimestamp();
		
		if (previous == null) previous = "";
		
		String logEntry = timestamp + " " +  user + " " + previous + " " + newValue + " " + comment + "\n";
		
		System.out.print("Writing log entry: " + logEntry);
		try {
			Utility.appendToLogFile(logEntry, variablesLogFile, VariableLogFileHeader);
		} catch (IOException e) {
			e.printStackTrace();
			throw Utility.propagate(e);
		}
		
		return logEntry;
	}
	
	/*
	 * Read the variable from a file if available
	 * 
	 * @param variableName the variable name
	 */
	String readFromFileIfAvailable(String variableName) {
		File variablesLogFile = fileForVariableLog(variableName);
		System.out.println("Trying to read variable from log: " + variablesLogFile);
		
		// Load the last line and parse it to get the uri
		String lastLine = Utility.lastLine(variablesLogFile);
		if (lastLine == null) return null;
		
		String[] sections = lastLine.split(" ", 5);
		if (sections.length != 5) {
			throw new RuntimeException("Log file line has unexpected format: " + lastLine);
		}
		// comment will have newline at the end
		
		String uri = sections[3];

		// Set directly so it does not get logged
		// this.variables.put(variableName, uri);
		
		System.out.println("Read value of: " + uri);

		return uri;
	}

	/* (non-Javadoc)
	 * @see org.pointrel.pointrel20120623.core.VariablesInterface#getAllVariableNames()
	 */
	public ArrayList<String> getAllVariableNames() {
		ArrayList<String> result = new ArrayList<String>();

		// Filter only files matchign variable name pattern
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.startsWith("pv_") && name.endsWith(".log");
		    }
		};
		
		String[] children = variablesBaseDirectory.list(filter);
		if (children != null) {
		    for (int i = 0; i < children.length; i++) {
		        String fileName = children[i];
		        fileName = fileName.substring(3, fileName.length() - 4);
		        fileName = Utility.decodeContentType(fileName);
		        result.add(fileName);
		    }
		}
		return result;
	}
}
