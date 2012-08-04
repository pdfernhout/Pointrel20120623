package org.pointrel.pointrel20120623.core;

public abstract class NewTransactionCallback {
	protected String lastTransactionProcessed = null;
	
	abstract public void newTransaction(Transaction transaction);
	
	// Tell it the last transaction that was processed, or null to receive all transactions
	protected NewTransactionCallback(String lastTransactionProcessed) {
		this.lastTransactionProcessed = lastTransactionProcessed;
	}

}
