/**
 * 
 */
package org.pointrel.pointrel20120623.core;

import java.io.IOException;

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

	/*
	 * Visits all resources in a list (or tree) of transactions recursively.
	 * TODO: Could be made into a loop now that only one previous is allowed per Transaction.
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
	 * 
	 * @return true if found something of interest and finished early; false otherwise
	 */ 
	public static boolean visitAllResourcesInATransactionTreeRecursively(Session session, String transactionURI, TransactionVisitor visitor) {
		if (transactionURI != null && transactionURI.length() != 0) {
			byte[] transactionContent = session.getContentForURI(transactionURI);
			if (transactionContent == null) {
				throw new RuntimeException("Missing transaction content for: " + transactionURI);
			}
			Transaction transaction;
			try {
				transaction = new Transaction(transactionContent);
				transaction.setURI(transactionURI);
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
			
			String previousTransactionURI = transaction.getPrevious();
			
			if (previousTransactionURI != null) {
				if (visitAllResourcesInATransactionTreeRecursively(session, previousTransactionURI, visitor)) return true;
			}

			if (visitor.transactionExited(transaction)) return true;
		}
		return false;
	}
}