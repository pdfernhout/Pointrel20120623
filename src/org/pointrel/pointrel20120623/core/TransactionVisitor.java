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

	public boolean transactionExited(Transaction transaction) {
		return false;
	}

	public boolean resourceInserted(String resourceUUID) {
		return false;
	}

	public boolean resourceRemoved(String resourceUUID) {
		return false;
	}

	/*
	 * Visits all resources in a list (or tree) of transactions recursively
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
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Problem parsing transaction: " + transactionURI);
			}
			
			if (visitor.transactionEntered(transaction)) return true;
			
			for (String resourceURI: transaction.getInserts()) {
				if (visitor.resourceInserted(resourceURI)) return true;
			}
			
			for (String resourceURI: transaction.getRemoves()) {
				if (visitor.resourceRemoved(resourceURI)) return true;
			}
			
			ArrayList<String> includes = transaction.getIncludes();
			
			for (String transactionURIForRecursing: includes) {
				if (visitAllResourcesInATransactionTreeRecursively(session, transactionURIForRecursing, visitor)) return true;
			}
			if (visitor.transactionExited(transaction)) return true;
		}
		return false;
	}
}