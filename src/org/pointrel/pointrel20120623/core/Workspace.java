package org.pointrel.pointrel20120623.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jdesktop.swingworker.SwingWorker;

public class Workspace {
	
	// TODO: Really should remove this and maybe not have a default?
	public static String DefaultWorkspaceVariable = "default_workspace";
	
	final private Session session;
	
	private String workspaceVariable;

	final private ArrayList<NewTransactionCallback> newTransactionCallbacks = new ArrayList<NewTransactionCallback>();
	
	private String lastTransactionProcessed = null;
	int newTransactionLoadRate = 3000;
	
	SwingWorker<Void,Transaction> newTransactionLoaderWorker = new SwingWorker<Void,Transaction>() {
	
			@Override
			protected Void doInBackground() throws Exception {
				while (!this.isCancelled()) {
					Thread.sleep(newTransactionLoadRate);
					// publish(newTransaction);
				}
				return null;
			}
			
			@Override
			protected void process(List<Transaction> transactions) {
				
			}
			
		};

	public Workspace(String serverURL, String workspaceVariable, String user) {
		session = new Session(serverURL, user);
		this.workspaceVariable = workspaceVariable;
	}

	public Workspace(File pointrelArchiveDirectory, String workspaceVariable, String user) {
		session = new Session(pointrelArchiveDirectory, user);
		this.workspaceVariable = workspaceVariable;
	}
	
	
	public String getWorkspaceVariable() {
		return workspaceVariable;
	}

	public void setWorkspaceVariable(String workspaceVariable) {
		this.workspaceVariable = workspaceVariable;
	}

	void addNewTransactionCallback(NewTransactionCallback newTransactionCallback) {
		newTransactionCallbacks.add(newTransactionCallback);
	}

	public void setNewTransactionLoadRate(int newValue) {
		if (newValue < newTransactionLoadRate) {
			newTransactionLoadRate = newValue;
		}
	}
	
	// Resources
	
	public String addContent(byte[] content, String contentType, String precalculatedURI) {
		return session.addContent(content, contentType, precalculatedURI);
	}
	
	// Convenience method for above
	public String addContent(byte[] content, String contentType) {
		return session.addContent(content, contentType);
	}
	
	// Convenience method for above
	public String addContent(String content, String contentType) {
		return session.addContent(content.getBytes(), contentType);
	}

	public byte[] getContentForURI(String uri) {
		return session.getContentForURI(uri);
	}
	
	// Convenience method for above
	public String getContentForURIAsString(String uri) {
		return session.getContentForURIAsString(uri);
	}

	// User 
	public String getUser() {
		return session.getUser();
	}

	// TODO: Maybe should not have this and should treat it as final? Only used by SimpleChatApp right now.
	public void setUser(String userID) {
		session.setUser(userID);
	}

	public String getLatestTransactionForWorkspace() {
		return session.getLatestTransactionForWorkspace(this.workspaceVariable);
	}

	// TODO: Maybe should not expose this?
	public Session getSession() {
		return session;
	}

	public void addSimpleTransactionToWorkspace(String uri, String comment) {
		session.addSimpleTransactionToWorkspace(this.workspaceVariable, uri, comment);
	}

}
