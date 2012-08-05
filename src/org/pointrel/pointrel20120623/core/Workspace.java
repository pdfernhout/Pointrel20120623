package org.pointrel.pointrel20120623.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jdesktop.swingworker.SwingWorker;

//TODO: Need to think about encoding of workspace variable names from UUIDs?

// TODO: Starting to think only one "Workspace" can have control of a variable at a time? Like locking?
// But would happen implicitely by one Community app saying it is in a workspace? Maybe should be explicit lock?

public class Workspace {
	
	// TODO: Really should remove this and maybe not have a default?
	public static String DefaultWorkspaceVariable = "default_workspace";
	
	final private Session session;
	
	private String workspaceVariable;

	final private CopyOnWriteArrayList<NewTransactionCallback> newTransactionCallbacks = new CopyOnWriteArrayList<NewTransactionCallback>();
	
	int newTransactionLoaderSleepTime = 3000;
	
	// TODO: This may take a lot of memory, maybe can remove it or reduce it to just strings?
	final ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	
	class NewTransactionChecker extends TransactionVisitor {
		String lastTransactionProcessed;
		boolean foundLastTransactionProcessed = false;
		ArrayList<Transaction> unprocessedTransactions = new ArrayList<Transaction>();
		
		NewTransactionChecker(String lastTransactionProcessed) {
			this.lastTransactionProcessed = lastTransactionProcessed;
		}
		
		@Override
		public boolean transactionEntered(Transaction transaction) {
			System.out.println("Transcation entered: " + transaction.uri);
			// Stop iterating if reaching known transaction -- assuming it is not going down two sides?
			if (transaction.getURI().equals(lastTransactionProcessed)) {
				System.out.println("Hit last transaction processed");
				foundLastTransactionProcessed = true;
				return true;
			}
			System.out.println("Adding to the list");
			unprocessedTransactions.add(transaction);
			// Handle the case where run up to the end
			if (lastTransactionProcessed == null && transaction.getPrevious() == null) {
				foundLastTransactionProcessed = true;
			}
			return false;
		}
	}
	
	SwingWorker<Void,Transaction> newTransactionLoaderWorker = new SwingWorker<Void,Transaction>() {
			private String lastTransactionProcessed = null;
	
			@Override
			protected Void doInBackground() throws Exception {
				while (!this.isCancelled()) {
					// Load new transactions, if any
					loadAnyNewTransactions();
					// TODO: Save new transactions 
					Thread.sleep(newTransactionLoaderSleepTime);
				}
				return null;
			}
			
			private void loadAnyNewTransactions() {
				System.out.println("==== Checking for any new transactions");
				
				checkForCallbacksThatNeedToCatchUp();
				
				// TODO: A big issue is if there was some kind of branching (or deletion) where some objects would change; ignoring that for now
				String currentTransaction = getLatestTransaction();
				
				// Check if there is nothing to do
				if (currentTransaction == null) {
					if (lastTransactionProcessed == null) return;
					throw new RuntimeException("Backtracking back to null not supported yet");
				} else {
					if (currentTransaction.equals(lastTransactionProcessed)) return;
				}
				
				System.out.println("==== There are new transactions; last: " + lastTransactionProcessed + " current: " + currentTransaction);
				
				// There must be some difference
				NewTransactionChecker visitor = new NewTransactionChecker(lastTransactionProcessed);
				TransactionVisitor.visitAllResourcesInATransactionTreeRecursively(Workspace.this, currentTransaction, visitor);
				
				if (currentTransaction != null && !visitor.foundLastTransactionProcessed) {
					throw new RuntimeException("Unfinished: No support for branching or backtracking yet");
				}
				
				// TODO: Maybe want to try/catch around these
				// TODO: Maybe want to do these in separate threads for each callback to give a fair time slice to each
				
				Collections.reverse(visitor.unprocessedTransactions);
				for (Transaction transaction: visitor.unprocessedTransactions) {
					transactions.add(transaction);
					for (NewTransactionCallback newTransactionCallback: newTransactionCallbacks) {
						newTransactionCallback.newTransaction(transaction);
						newTransactionCallback.lastTransactionProcessed = transaction.uri;
					}
					System.out.println("transaction.uri: " + transaction.uri);
					lastTransactionProcessed = transaction.uri;
				}
			}

			protected void checkForCallbacksThatNeedToCatchUp() {
				for (NewTransactionCallback newTransactionCallback: newTransactionCallbacks) {
					// This is to handle missed transactions as apps can connect after some transactions have already been processed
					if (newTransactionCallback.lastTransactionProcessed == null) {
						catchupMissedTransactions(newTransactionCallback);
					}
				}
			}

			private void catchupMissedTransactions(NewTransactionCallback newTransactionCallback) {
				for (Transaction transaction: transactions) {
					newTransactionCallback.newTransaction(transaction);
					newTransactionCallback.lastTransactionProcessed = transaction.uri;
				}
			}
		};

//	// TODO: Maybe handle remote things via synchronization?
//	public Workspace(String workspaceVariable, String serverURL, String user) {
//		session = new Session(serverURL, user);
//		this.workspaceVariable = workspaceVariable;
//		newTransactionLoaderWorker.execute();
//	}

	public Workspace(String workspaceVariable, File pointrelArchiveDirectory, String user) {
		session = new Session(pointrelArchiveDirectory, user);
		this.workspaceVariable = workspaceVariable;
		newTransactionLoaderWorker.execute();
	}
	
	
	public String getWorkspaceVariable() {
		return workspaceVariable;
	}

	public void setWorkspaceVariable(String workspaceVariable) {
		this.workspaceVariable = workspaceVariable;
	}

	public void addNewTransactionCallback(NewTransactionCallback newTransactionCallback) {
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

	// Transactions
	
	public String getLatestTransaction() {
		if (workspaceVariable == null) {
			throw new IllegalArgumentException("workspace variableName should not be null");
		}
		return session.basicGetVariable(workspaceVariable);
	}
	
	// TODO: Think about better synchronization and also locking the variable
	// This does not check if user might be out-of-date in multi-user system
	synchronized public String addSimpleTransaction(String uriToAdd, String comment) {
		if (uriToAdd == null) {
			throw new IllegalArgumentException("uriToAdd should not be null");
		}
		if (comment == null) {
			throw new IllegalArgumentException("comment should not be null");
		}
		if (workspaceVariable == null) {
			throw new IllegalArgumentException("workspace variableName should not be null");
		}
		String previousTransaction = this.getLatestTransaction();
		// TODO: System could fail/collide/error if another process/application/JVM adds a new transaction to the same workspace with the same previous right now -- need to lock variable/workspace briefly? Or check and retry?
		// If lock the file system, then maybe do not need to be "synchronized"? As other threads would retry or wait?
		Transaction transaction = new Transaction(workspaceVariable, Utility.currentTimestamp(), this.getUser(), uriToAdd, previousTransaction, comment);
		String newTransactionURI = addContent(transaction.toJSONBytes(), Transaction.ContentType);
		// TODO: This next line is not needed as the transaction is not kept around
		transaction.setURI(newTransactionURI);
		System.out.println("URI for new transaction: " + newTransactionURI);
		session.basicSetVariable(workspaceVariable, newTransactionURI, comment);
		return newTransactionURI;
	}

}
