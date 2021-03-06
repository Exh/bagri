package com.bagri.core.server.api;

import java.util.Date;
import java.util.Properties;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.model.Document;

/**
 * XDM Document Management server-side implementation. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface DocumentManagement extends com.bagri.core.api.DocumentManagement {

	/**
	 * provides XDM Document content for the internal document key
	 * 
	 * @param docKey the internal Document key represented as long 
	 * @param props result production properties
	 * @return Map&lt;String, Object&gt; representing the XDM document
	 * @throws BagriException in case of any error
	 */
	DocumentAccessor getDocument(long docKey, Properties props) throws BagriException;
	
	/**
	 * provides XDM Document content for the internal document key
	 * 
	 * @param docKey the internal Document key 
	 * @param props result production properties
	 * @return Map&lt;String, Object&gt; representing the XDM document
	 * @throws BagriException in case of any error
	 */
	DocumentAccessor getDocument(DocumentKey docKey, Properties props) throws BagriException;
	
	/**
	 * provides document's MIME type (xml/json as of now)
	 * 
	 * @param docKey the internal Document key 
	 * @return the Document's content MIME type
	 * @throws BagriException in case of any error
	 */
	String getDocumentContentType(long docKey) throws BagriException;
	
	/**
	 * Creates a new Document structure from the content provided
	 * 
	 * @param docKey the Document key
	 * @param uri the Document uri
	 * @param content the Document content
	 * @param dataFormat the Document format
	 * @param createdAt dateTiem of the Document creation
	 * @param createdBy the Document's owner
	 * @param txStart the Id of the transaction owning Document  
	 * @param collections the collection Ids to include document in 
	 * @param addContent to cache Document content or not
	 * @return the Document created
	 * @throws BagriException in case of any error happened
	 */
	//Document createDocument(DocumentKey docKey, String uri, Object content, String dataFormat, 
	//		Date createdAt, String createdBy, long txStart, int[] collections, boolean addContent) throws BagriException;
	
}
