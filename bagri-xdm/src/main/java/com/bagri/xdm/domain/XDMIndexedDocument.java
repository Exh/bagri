package com.bagri.xdm.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;

/**
 * Represents indexed document value
 * 
 * @author Denis Sukhoroslov
 *
 */
public class XDMIndexedDocument extends XDMIndexedValue {
	
	private Set<Long> docKeys = new HashSet<>();

	/**
	 * {@inheritDoc}
	 */
	public XDMIndexedDocument() {
		super();
	}

	/**
	 * 
	 * @param docKey the document internal key
	 */
	public XDMIndexedDocument(long docKey) {
		super();
		addDocument(docKey, TX_NO);
	}

	/**
	 * 
	 * @param docKeys the documents
	 */
	public XDMIndexedDocument(Collection<Long> docKeys) {
		super();
		if (docKeys != null) {
			for (Long docKey: docKeys) {
				addDocument(docKey, TX_NO);
			}
		}
	}

	/**
	 * return indexed documents count
	 */
	@Override
	public int getCount() {
		return docKeys.size();
	}

	@Override
	public long getDocumentKey() {
		//if (docIds.size() > 0) {
		//	return ???
		//}
		return 0;
	}

	/**
	 * @return the document keys
	 */
	@Override
	public Set<Long> getDocumentKeys() {
		return docKeys;
	}

	/**
	 * adds document to index
	 */
	@Override
	public boolean addDocument(long docKey, long txId) {
		return docKeys.add(docKey);
	}
	
	/**
	 * removes document from index
	 */
	@Override
	public boolean removeDocument(long docKey, long txId) {
		return docKeys.remove(docKey);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XDMIndexedValue [docKeys=" + docKeys + "]";
	}

	/**
	 * return consumed size in bytes
	 */
	@Override
	public int getSize() {
		// have no idea how much memory HashSet takes!
		return Long.SIZE / Byte.SIZE // Set ref
			+ (2 * Long.SIZE / Byte.SIZE) * docKeys.size();
	}

}
