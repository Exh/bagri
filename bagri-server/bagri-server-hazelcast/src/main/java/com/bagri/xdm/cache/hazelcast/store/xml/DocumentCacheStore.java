package com.bagri.xdm.cache.hazelcast.store.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bagri.common.util.FileUtils.def_encoding;
import static com.bagri.common.config.XDMConfigConstants.xdm_schema_store_type;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.util.FileUtils;
import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.api.XDMTransactionManagement;
import com.bagri.xdm.cache.hazelcast.impl.DocumentManagementImpl;
import com.bagri.xdm.client.common.XDMCacheConstants;
import com.bagri.xdm.client.xml.XDMStaxParser;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElements;
import com.bagri.xdm.domain.XDMFragmentedDocument;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMParser;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;
import com.hazelcast.core.PartitionService;

import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;
import static com.bagri.xdm.api.XDMTransactionManagement.TX_INIT;

public class DocumentCacheStore extends XmlCacheStore implements MapStore<XDMDocumentKey, XDMDocument>, MapLoaderLifecycleSupport {

    private static final Logger logger = LoggerFactory.getLogger(DocumentCacheStore.class);
    private static final int defVersion = 1;
    
    private HazelcastInstance hzInstance;
    //private IdGenerator docGen;
    private Map<XDMDocumentKey, DocumentDataHolder> docKeys = new HashMap<XDMDocumentKey, DocumentDataHolder>();
    
	//private String dataPath;
    private String dataFormat = XDMParser.df_xml;
	//private XDMFactory keyFactory;
    private DocumentManagementImpl docMgr;
    private XDMModelManagement schemaDict;
    private IMap<XDMDocumentKey, String> xmlCache;
    //private IMap<XDMDataKey, XDMElements> xdmCache;
    
	@Override
	public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
		logger.trace("init.enter; properties: {}", properties);
		hzInstance = hazelcastInstance;
		//dataPath = (String) properties.get("dataPath");
		//keyFactory = (XDMFactory) properties.get("keyFactory");
		docMgr = (DocumentManagementImpl) properties.get("xdmManager");
		schemaDict = (XDMModelManagement) properties.get("xdmModel");
		//schemaDict = docMgr.getSchemaDictionary();
		xmlCache = hzInstance.getMap(XDMCacheConstants.CN_XDM_XML);
		//xdmCache = hzInstance.getMap(XDMCacheConstants.CN_XDM_ELEMENT);
		String df = properties.getProperty(xdm_schema_store_type);
		if (df != null) {
			dataFormat = df;
		}
	}

	@Override
	public void destroy() {
		// do nothing
	}
    
	//Map<Long, DocumentDataHolder> getDocumentKeys() {
	//	return docKeys;
	//}
	
    //public void setDataPath(String dataPath) {
    //	this.dataPath = dataPath;
    //}
    
	private void processPathFiles(Path root, List<Path> files) throws IOException {
		String ext = "*." + dataFormat.toLowerCase();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, ext)) {
		    for (Path path: stream) {
		        if (Files.isDirectory(path)) {
		            processPathFiles(path, files);
		        } else {
		    		//logger.trace("processPathFiles; path: {}; uri: {}", path.toString(), path.toUri().toString());
		            files.add(path);
		        }
		    }
		}
	}

	private String normalizePath(Path path) {
		return path.toAbsolutePath().normalize().toString();
	}
	
    private XDMDocument loadDocument(XDMDocumentKey docKey) {
		DocumentDataHolder ddh = docKeys.get(docKey);
		if (ddh != null) {
			Path path = Paths.get(ddh.uri);
	    	if (Files.exists(path)) {
	    		String uri = normalizePath(path);
	    		path = Paths.get(uri);
	    		uri = path.toUri().toString();
        		try {
        			String xml = FileUtils.readTextFile(ddh.uri);
    	    		XDMParser parser = docMgr.getXdmFactory().newXDMParser(dataFormat, schemaDict);
        			//List<XDMData> data = parser.parse(new File(ddh.uri));
        			List<XDMData> data = parser.parse(xml);         		

        			List<Long> fragments = docMgr.loadElements(docKey.getKey(), data); 
        			if (fragments == null) {
        				logger.warn("loadDocument.exit; the document is not valid as it has no root element");
        				//throw new XDMException("invalid document", XDMException.ecDocument);
        			} else {
	        			int docType = fragments.get(0).intValue();
        				ddh.docType = docType;
						xmlCache.set(docKey, xml);
	        			XDMDocument doc;
						// can make a fake population TX with id = 1! 
	        			if (fragments.size() == 1) {
	        				doc = new XDMDocument(docKey.getDocumentId(), docKey.getVersion(), uri, docType, TX_INIT, TX_NO,
	        					new Date(Files.getLastModifiedTime(path).toMillis()), Files.getOwner(path).getName(), def_encoding);
	        			} else {
	        				doc = new XDMFragmentedDocument(docKey.getDocumentId(), docKey.getVersion(), uri, docType, TX_INIT, TX_NO,	
	        					new Date(Files.getLastModifiedTime(path).toMillis()), Files.getOwner(path).getName(), def_encoding);
	        				long[] fa = new long[fragments.size()];
	        				fa[0] = docKey.getKey();
	        				for (int i=1; i < fragments.size(); i++) {
	        					fa[i] = fragments.get(i);
	        				}
	        				((XDMFragmentedDocument) doc).setFragments(fa);
	        			}
	        			return doc;
        			}        			
				} catch (IOException | XDMException ex) {
					logger.error("loadDocument.error", ex);
				}
	    	}
		}
    	return null;
    }
    
	private XDMData getDataRoot(List<XDMData> elements) {

		for (XDMData data: elements) {
			if (data.getNodeKind() == XDMNodeKind.element) {
				return data;
			}
		}
		return null;
	}
    
	@Override
	public XDMDocument load(XDMDocumentKey key) {
		logger.trace("load.enter; key: {}", key);
    	XDMDocument result = loadDocument(key);
		logger.trace("load.exit; returning: {}", result);
		return result;
	}

	@Override
	public Map<XDMDocumentKey, XDMDocument> loadAll(Collection<XDMDocumentKey> keys) {
		logger.trace("loadAll.enter; keys: {}; Cluster size: {}", keys, hzInstance.getCluster().getMembers().size());
		Map<XDMDocumentKey, XDMDocument> result = new HashMap<XDMDocumentKey, XDMDocument>(keys.size());
	    for (XDMDocumentKey key: keys) {
	    	XDMDocument doc = loadDocument(key);
	    	if (doc != null) {
	    		result.put(key, doc);
	    	}
	    }
		logger.trace("loadAll.exit; returning: {}", result);
		return result;
	}

	@Override
	public Set<XDMDocumentKey> loadAllKeys() {
		if (schemaDict == null) {
			logger.trace("loadAllKeys.enter; store is not ready yet, skipping population");
			return null;
		}
		
		logger.trace("loadAllKeys.enter;");
	    Set<XDMDocumentKey> docIds = null;
		Path root = Paths.get(getDataPath());
		try {
			docKeys.clear();
			List<Path> files = new ArrayList<>();
			processPathFiles(root, files);
			Collections.sort(files);
			XDMDocumentKey docKey; 
			PartitionService ps = hzInstance.getPartitionService();
			for (Path path: files) {
				docKey = docMgr.nextDocumentKey();
				if (ps.getPartition(docKey).getOwner().localMember()) {
					docKeys.put(docKey, new DocumentDataHolder(normalizePath(path)));
				}
			}
			docIds = new HashSet<XDMDocumentKey>(docKeys.keySet());
		} catch (IOException ex) {
			logger.error("loadAllKeys.error;", ex);
		}
		logger.trace("loadAllKeys.exit; returning: {}; docKeys: {}", docIds, docKeys);
		return docIds;
	}
	
	@Override
	public void store(XDMDocumentKey key, XDMDocument value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
		if (docMgr == null) {
			logger.trace("store; not ready yet, skipping store");
			return;
		}
		
		DocumentDataHolder data = docKeys.get(key);
		if (data == null) {
			// create a new document
			Path path = Paths.get(value.getUri());
			logger.trace("store; going to create new file {} at {}", path, getDataPath());
			if (!path.isAbsolute()) {
				Path root = Paths.get(getDataPath());
				path = root.resolve(path);
			}
    		String uri = normalizePath(path);
			logger.trace("store; got path: {}; uri: {}", path, uri);
    		data = new DocumentDataHolder(uri);
			docKeys.put(key, data);
		} else {
			// update existing document - put a new version
		}
		
		try {
			String xml = docMgr.getDocumentAsString(key.getKey());
			FileUtils.writeTextFile(data.uri, xml);
			logger.trace("store.exit; stored as: {}; length: {}", data.uri, xml.length());
		} catch (IOException | XDMException ex) {
			logger.error("store.error; exception on store document: " + ex.getMessage(), ex);
		}
	}

	@Override
	public void storeAll(Map<XDMDocumentKey, XDMDocument> entries) {
		logger.trace("storeAll.enter; entries: {}", entries.size());
		for (Map.Entry<XDMDocumentKey, XDMDocument> entry: entries.entrySet()) {
			store(entry.getKey(), entry.getValue());
		}
		logger.trace("storeAll.exit; stored: {}", entries.size());
	}

	@Override
	public void delete(XDMDocumentKey key) {
		logger.trace("delete.enter; key: {}", key);
    	boolean result = false;
		DocumentDataHolder data = docKeys.get(key);
		if (data != null) {
	    	Path path = Paths.get(data.uri);
			try {
				result = Files.deleteIfExists(path);
			} catch (IOException ex) {
				logger.error("delete.error; path: " + path, ex);
			}
		}
    	logger.trace("delete.exit; deleted: {}", result);
	}

	@Override
	public void deleteAll(Collection<XDMDocumentKey> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys.size());
		int deleted = 0;
		for (XDMDocumentKey key: keys) {
			DocumentDataHolder data = docKeys.get(key);
			if (data != null) {
		    	Path path = Paths.get(data.uri);
				try {
					if (Files.deleteIfExists(path)) {
						deleted++;
					}
				} catch (IOException ex) {
					logger.error("deleteAll.error; path: " + path, ex);
				}
			}
		}
		logger.trace("deleteAll.exit; deleted: {}", deleted);
	}
	

	private class DocumentDataHolder {

		String uri;
		int docType = 0;
			
		DocumentDataHolder(String uri) {
			this.uri = uri;
		}

		@Override
		public String toString() {
			return "DocumentDataHolder [uri=" + uri + ", docType=" + docType + "]";
		}
		
	}
}
