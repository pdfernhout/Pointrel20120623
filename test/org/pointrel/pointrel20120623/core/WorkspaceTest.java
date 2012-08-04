package org.pointrel.pointrel20120623.core;

import java.util.ArrayList;

import junit.framework.TestCase;

public class WorkspaceTest extends TestCase {
	public void testAddingTransaction() {
		// Keeps adding the same file, so not something that would happen in practice
		String variableName = "test005";
		String user = "unknown_user@example.com";
		String uriToAdd = SessionTest.URI_For_This_is_a_test;
		String comment ="unit testing";
		Workspace instance = new Workspace(variableName, SessionTest.UnitTestArchive, user);
		String uri = instance.addSimpleTransaction(uriToAdd, comment);
		assertNotNull(uri);
		
		String value = instance.getLatestTransaction();
		assertEquals(uri, value);
	}
	
	public void testVisitingTransactions() {
		String variableName = "test005";
		Workspace instance = new Workspace(variableName, SessionTest.UnitTestArchive, null);
		String uri = instance.getLatestTransaction();
		final ArrayList<Transaction> transactionsEntered = new ArrayList<Transaction>();
		final ArrayList<Transaction> transactionsExited = new ArrayList<Transaction>();
		TransactionVisitor visitor = new TransactionVisitor() {
			public boolean transactionEntered(Transaction transaction) {
				// System.out.println("Entering: " + transaction);
				transactionsEntered.add(transaction);
				return false;
			}
			public boolean transactionExited(Transaction transaction) {
				// System.out.println("Exiting: " + transaction);
				transactionsExited.add(transaction);
				return false;
			}
		};
		TransactionVisitor.visitAllResourcesInATransactionTreeRecursively(instance, uri, visitor);
		assertFalse(transactionsEntered.isEmpty());
		assertEquals(transactionsEntered.size(), transactionsExited.size());
		for (int i = 0; i < transactionsEntered.size(); i++) {
			// System.out.println("Comparing :" + i);
			assertEquals(transactionsEntered.get(i), transactionsExited.get(transactionsEntered.size() - 1 - i));
		}
	}
}
