package com.bagri.xdm.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Denis Sukhoroslov: dsukhoroslov@gmail.com
 * @version 0.3
 */
public class XDMIndex { //implements Serializable {

	/**
	 * 
	 */
	//private static final long serialVersionUID = 7780583844744846834L;
	
	//private int pathId;
	//private Object value;
	private Set<Long> docIds = new HashSet<Long>();

	public XDMIndex() {
	}

	public XDMIndex(long docId) {
		this();
		addIndex(docId);
	}
	
	public XDMIndex(Collection<Long> docIds) {
		this();
		//this.path = path;
		//this.value = value;
		if (docIds != null) {
			for (Long docId: docIds) {
				addIndex(docId);
			}
		}
	}

	/**
	 * @return the path
	 */
	//public int getPathId() {
	//	return path;
	//}

	/**
	 * @return the value
	 */
	//public Object getValue() {
	//	return value;
	//}

	/**
	 * @return the documentIds
	 */
	public Set<Long> getDocumentIds() {
		return docIds;
	}

	public void addIndex(long docId) {
		docIds.add(docId);
	}
	
}
