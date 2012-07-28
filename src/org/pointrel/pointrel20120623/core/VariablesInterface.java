package org.pointrel.pointrel20120623.core;

import java.util.ArrayList;

public interface VariablesInterface {

	public abstract String basicGetValue(String variableName);

	public abstract boolean basicSetValue(String variableName, String user, String value, String previous, String comment);

	public abstract ArrayList<String> getAllVariableNames();

}