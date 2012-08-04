package org.pointrel.pointrel20120623.core;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jdesktop.swingworker.SwingWorker;

public class Workspace {
	
	// TODO: Really should remove this and maybe not have a default?
	public static String DefaultWorkspaceVariable = "default_workspace";
	
	final private Session session;
	
	private String workspaceVariable;

	final private CopyOnWriteArrayList<NewTransactionCallback> newTransactionCallbacks = new CopyOnWriteArrayList<NewTransactionCallback>();
	
	private String lastTransactionProcessed = null;
	int newTransactionLoaderSleepTime = 3000;
	
	SwingWorker<Void,Transaction> newTransactionLoaderWorker = new SwingWorker<Void,Transaction>() {
	
			@Override
			protected Void doInBackground() throws Exception {
				while (!this.isCancelled()) {
					Thread.sleep(newTransactionLoaderSleepTime);
					// publish(newTransaction);
				}
				return null;
			}
			
			@Override
			protected void process(List<Transaction> transactions) {
				// TODO: ensure the order of these is correct -- oldest to most recent
				for (Transaction transaction: transactions) {
					for (NewTransactionCallback newTransactionCallback: newTransactionCallbacks) {
						// TODO: Need to handle missed transactions somehow, and update the latest transaction for the call
						newTransactionCallback.newTransaction(transaction);
					}
				}
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

	// Accessing
	
	public String getUser() {
		return session.getUser();
	}

	// TODO: Maybe should not have this and should treat it as final? Only used by SimpleChatApp right now.
	public void setUser(String userID) {
		session.setUser(userID);
	}

	// TODO: Maybe should not expose this?
	public Session getSession() {
		return session;
	}
	
	public void setNewTransactionLoadRate(int newValue) {
		if (newValue < newTransactionLoaderSleepTime) {
			newTransactionLoaderSleepTime = newValue;
		}
	}
	
	// Resources and variables -- all of these can (for a server connection) take an arbitrarily long time to complete
	
	public String addContent(byte[] content, String contentType, String precalculatedURI) {
		return session.addContent(content, contentType, precalculatedURI);
	}
	
	// Convenience method for above
	public String addContent(byte[] content, String contentType) {
		return this.addContent(content, contentType, null);
	}
	
	// Convenience method for above
	public String addContent(String content, String contentType) {
		return this.addContent(content.getBytes(), contentType, null);
	}

	public byte[] getContentForURI(String uri) {
		return session.getContentForURI(uri);
	}
	
	// Convenience method for above
	public String getContentForURIAsString(String uri) {
		return session.getContentForURIAsString(uri);
	}

	public void addSimpleTransactionToWorkspace(String uri, String comment) {
		session.addSimpleTransactionToWorkspace(this.workspaceVariable, uri, comment);
	}
	
	public String getLatestTransactionForWorkspace() {
		return session.getLatestTransactionForWorkspace(this.workspaceVariable);
	}

}
