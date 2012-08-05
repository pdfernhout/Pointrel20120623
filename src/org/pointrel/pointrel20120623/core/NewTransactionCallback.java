package org.pointrel.pointrel20120623.core;

public abstract class NewTransactionCallback {
	protected String lastTransactionProcessed;
	protected String encodedContentType;
	
	public void newTransaction(Transaction transaction) {
		for (String uriToRemove: transaction.getRemoves()) {
			if (encodedContentType != null) {
				if (!uriToRemove.endsWith(encodedContentType)) return;
			}
			remove(uriToRemove);
		}
		for (String uriToInsert: transaction.getInserts()) {
			if (encodedContentType != null) {
				if (!uriToInsert.endsWith(encodedContentType)) return;
			}
			insert(uriToInsert);
		}
	}

	protected void remove(String resourceUUID) {
		// Override as needed
	}
	
	abstract protected void insert(String resourceUUID);

	// TODO: Could add support for saying up to what transaction has already been processed by this app
	public NewTransactionCallback(String contentType) {
		encodedContentType = Utility.encodeContentType(contentType);
		this.lastTransactionProcessed = null;
	}
	
//	public NewTransactionCallback filterForContentType(String contentType) {
//		 encodedContentType = Utility.encodeContentType(contentType);
//		 return this;
//	}

}
