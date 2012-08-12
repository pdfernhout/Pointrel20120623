/**
 * 
 */
package org.pointrel.pointrel20120623.core;

import java.io.IOException;
import java.util.ArrayList;

public class TransactionVisitor {
	
	public TransactionVisitor() {
		// Does nothing
	}

	public boolean transactionEntered(Transaction transaction) {
		return false;
	}

	public boolean resourceRemoved(String resourceUUID) {
		return false;
	}
	
	public boolean resourceInserted(String resourceUUID) {
		return false;
	}
	
	public boolean transactionExited(Transaction transaction) {
		return false;
	}
	
	public static enum StopType {StopAfterFirstMatch, StopAfterAllBranchesMatch, StopAfterAllBranchesProcessedAndAtLeastOneMatch}

	public static boolean visitAllResourcesInATransactionTreeRecursively(Workspace workspace, String transactionURI, TransactionVisitor visitor) {
		return visitAllResourcesInATransactionTreeRecursively(workspace, transactionURI, visitor, StopType.StopAfterFirstMatch);
	}

	/*
	 * Visits all resources in a list (or tree) of transactions recursively.
	 * TODO: May want to change how this works so that it always calls visitor methods 
	 * for resources and transactions in the order that they were added,
	 * perhaps noting for resources if they were later deleted, or, alternatively,
	 * might have two visitors, one that visits from the most recent and one that
	 * visits from the bottom up; note that some applications depend on the current
	 * behavior to find the latest transaction about the application.
	 * 
	 * @param session The session to get data from
	 * @param transactionURI The transcario to start from
	 * @param visitor The visitor object with callbacks
	 * @param stopType Whether to stop on the first match or whether to be sure every branch matches some stop condition in the visitor
	 * 
	 * @return true if found something of interest and finished early; false otherwise
	 */ 
	public static boolean visitAllResourcesInATransactionTreeRecursively(Workspace workspace, String transactionURI, TransactionVisitor visitor, StopType stopType) {
		if (transactionURI != null && transactionURI.length() != 0) {
			byte[] transactionContent = workspace.getContentForURI(transactionURI);
			if (transactionContent == null) {
				throw new RuntimeException("Missing transaction content for: " + transactionURI);
			}
			Transaction transaction;
			try {
				transaction = new Transaction(transactionContent, transactionURI);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Problem parsing transaction: " + transactionURI);
			}
			
			if (visitor.transactionEntered(transaction)) return true;
			
			for (String resourceURI: transaction.getRemoves()) {
				if (visitor.resourceRemoved(resourceURI)) return true;
			}
			
			for (String resourceURI: transaction.getInserts()) {
				if (visitor.resourceInserted(resourceURI)) return true;
			}
			
			ArrayList<String> priors = transaction.getPriors();

			int matchCount = 0;
			for (String transactionURIForRecursing: priors) {
				if (visitAllResourcesInATransactionTreeRecursively(workspace, transactionURIForRecursing, visitor, stopType)) {
					matchCount++;
					if (stopType == StopType.StopAfterFirstMatch) return true;
					if (stopType == StopType.StopAfterAllBranchesMatch && matchCount == priors.size()) return true;
				}
			}
			
			if (stopType == StopType.StopAfterAllBranchesProcessedAndAtLeastOneMatch && matchCount > 0) return true;

			if (visitor.transactionExited(transaction)) return true;
		}
		return false;
	}
}