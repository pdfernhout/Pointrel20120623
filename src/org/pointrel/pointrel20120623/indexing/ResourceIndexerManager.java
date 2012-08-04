package org.pointrel.pointrel20120623.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.pointrel.pointrel20120623.core.Transaction;
import org.pointrel.pointrel20120623.core.TransactionVisitor;
import org.pointrel.pointrel20120623.core.Workspace;

public class ResourceIndexerManager {
	private HashMap<String,ResourceIndexer> resourceIndexers = new HashMap<String,ResourceIndexer>();
	String lastTransactionProcessed = null;
	
	public ResourceIndexerManager() {
		// Nothing needed
	}
	
	public void addResourceIndexer(String name, ResourceIndexer resourceIndexer) {
		if (resourceIndexers.containsKey(name)) {
			throw new RuntimeException("ResourceIndexer for name: \"" + name + "\" already exists");
		}
		// TODO: Should all previous resources be added to the index?
		resourceIndexers.put(name, resourceIndexer);
	}
	
	// TODO: Subtle issue with branching, where if branches at higher level, might have indexed differently if was starting over from top; leaving for later
	
	class NewTransactionChecker extends TransactionVisitor {
		String lastTransactionProcessed;
		boolean foundLastTransactionProcessed = false;
		ArrayList<Transaction> unprocessedTransactions = new ArrayList<Transaction>();
		
		NewTransactionChecker(String lastTransactionProcessed) {
			this.lastTransactionProcessed = lastTransactionProcessed;
		}
		
		@Override
		public boolean transactionEntered(Transaction transaction) {
			// Stop iterating if reaching known transaction -- assuming it is not going down two sides?
			if (transaction.getURI().equals(lastTransactionProcessed)) {
				foundLastTransactionProcessed = true;
				return true;
			}
			return false;
		}
	}
	
	public void loadNewTransactions(Workspace workspace) {
		// TODO: A big issue is if there was some kind of branching (or deletion) where some objects would change; ignoring that for now
		String currentTransaction = workspace.getLatestTransactionForWorkspace();
		
		// Check if there is nothing to do
		if (currentTransaction == null) {
			if (lastTransactionProcessed == null) return;
		} else {
			if (currentTransaction.equals(lastTransactionProcessed)) return;
		}
		
		// There must be some difference
		NewTransactionChecker visitor = new NewTransactionChecker(currentTransaction);
		TransactionVisitor.visitAllResourcesInATransactionTreeRecursively(workspace, currentTransaction, visitor);
		
		if (currentTransaction != null && !visitor.foundLastTransactionProcessed) {
			throw new RuntimeException("Unfinished: No support for branching");
		}
		
		// TODO: Order might not be correct if branching was involved
		Collections.reverse(visitor.unprocessedTransactions);
		for (Transaction transaction: visitor.unprocessedTransactions) {
			// TODO: Ignoring deletions for now
			loadNewTransaction(workspace, transaction);
		}
	}

	private void loadNewTransaction(Workspace workspace, Transaction transaction) {
		// TODO: Should these be added in reverse order?
		for (String resourceURI: transaction.getInserts() ) {
			processInsertedResource(workspace, resourceURI);
		}
	}

	private void processInsertedResource(Workspace workspace, String resourceURI) {
		// TODO: Figure out how to go from a resource URI to adding a user, membership, tag, message, association, or other object?
		// TODO: Also need to somehow let some listeners know that there is new data
		int indexingCount = 0;
		for (ResourceIndexer resourceIndexer: resourceIndexers.values()) {
			if (resourceIndexer.maybeIndexResource(workspace, resourceURI)) indexingCount++;
		}
		if (indexingCount > 0) return;
		// TODO: Nobody wanted to process the resource -- what to do? Just ignore it?
		System.out.println("Nobody indexed: " + resourceURI);
	}
}
