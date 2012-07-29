/**
 * 
 */
package org.pointrel.pointrel20120623.demos.simplecommunityapp;

import java.util.ArrayList;
import java.util.Collections;

import org.pointrel.pointrel20120623.core.Session;
import org.pointrel.pointrel20120623.core.Transaction;
import org.pointrel.pointrel20120623.core.TransactionVisitor;

public class Community {
	final String communityUUID;
	// String publicKey;
	String name = "Test Name Community (TODO)";
	String about = "This is a great place to live (TODO)";
	
	String lastTransactionProcessed = null;
	
	final ArrayList<User> users = new ArrayList<User>();
	final ArrayList<Membership> memberships = new ArrayList<Membership>();
	final ArrayList<DocumentVersion> documentVersions = new ArrayList<DocumentVersion>();
	final ArrayList<Message> messages = new ArrayList<Message>();
	final ArrayList<TagEvent> tagEvents = new ArrayList<TagEvent>();
	final ArrayList<CommunityAssociation> communityAssociations = new ArrayList<CommunityAssociation>();
	private ArrayList<ResourceProcessor> resourceProcessors;

	public Community(String communityUUID) {
		this.communityUUID = communityUUID;
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
	
	public void loadNewTransactions(Session session) {
		// TODO: A big issue is if there was some kind of branching (or deletion) where some objects would change; ignoring that for now
		String currentTransaction = session.getLatestTransactionForWorkspace();
		
		// Check if there is nothing to do
		if (currentTransaction == null) {
			if (lastTransactionProcessed == null) return;
		} else {
			if (currentTransaction.equals(lastTransactionProcessed)) return;
		}
		
		// There must be some difference
		NewTransactionChecker visitor = new NewTransactionChecker(currentTransaction);
		TransactionVisitor.visitAllResourcesInATransactionTreeRecursively(session, currentTransaction, visitor);
		
		if (currentTransaction != null && !visitor.foundLastTransactionProcessed) {
			throw new RuntimeException("Unfinished: No support for branching");
		}
		
		// TODO: Order might not be correct if branching was involved
		Collections.reverse(visitor.unprocessedTransactions);
		for (Transaction transaction: visitor.unprocessedTransactions) {
			// TODO: Ignoring deletions for now
			loadNewTransaction(session, transaction);
		}
	}

	private void loadNewTransaction(Session session, Transaction transaction) {
		// TODO: Should these be added in reverse order?
		for (String resourceURI: transaction.getInserts() ) {
			processInsertedResource(session, resourceURI);
		}
	}

	private void processInsertedResource(Session session, String resourceURI) {
		// TODO: Figure out how to go from a resource URI to adding a user, membership, tag, message, association, or other object?
		// TODO: Also need to somehow let some listeners know that there is new data
		for (ResourceProcessor resourceProcessor: resourceProcessors) {
			if (resourceProcessor.maybeProcessResource(session, resourceURI)) return;
		}
		// TODO: Nobody wanted to process the resource -- what to do?
	}
	
}