package com.bagri.rest.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.bagri.core.Constants.pn_document_data_format;
import static com.bagri.core.system.DataFormat.*;
import static com.bagri.rest.RestConstants.bg_cookie;

import java.util.Properties;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.SchemaRepository;
import com.bagri.client.hazelcast.impl.DocumentAccessorImpl;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.rest.BagriRestServer;
import com.bagri.rest.RepositoryProvider;

public class DocumentServiceTest extends JerseyTest {
	
	private DocumentManagement docMgr;
	private SchemaRepository mockRepo;
	private RepositoryProvider mockPro;
	private Properties propsXml;
	private Properties propsJson;
	private DocumentAccessor responseXml;
	private DocumentAccessor responseJson;

    @Override
    protected Application configure() {
    	propsXml = new Properties();
    	propsXml.setProperty(pn_document_data_format, df_xml);
    	propsJson = new Properties();
    	propsJson.setProperty(pn_document_data_format, df_json);
        docMgr = mock(DocumentManagement.class);
        mockRepo = mock(SchemaRepository.class);
    	mockPro = mock(RepositoryProvider.class);
        when(mockPro.getRepository("client-id")).thenReturn(mockRepo);
        when(mockRepo.getDocumentManagement()).thenReturn(docMgr);
        //responseXml = new Document(1L, "a0001.xml", "/content", "owner", 1, 34, 1);
        //responseJson = new Document(2L, "a0001.xml", "/content", "owner", 1, 30, 1); // why the same uri??
        responseXml = new DocumentAccessorImpl(mockRepo, null, "<content>initial content</content>", "XML", System.currentTimeMillis(), "owner", "utf-8", 
        		0, 0, 0, 0, null, -1, -1, "a0001.xml", 0);
        responseJson = new DocumentAccessorImpl(mockRepo, null, "{\"content\": \"updated content\"}", "XML", System.currentTimeMillis(), "owner", "utf-8", 
        		0, 0, 0, 0, null, -1, -1, "a0001.xml", 0); 
        try {
			when(docMgr.storeDocument("a0001.xml", "<content>initial content</content>", propsXml)).thenReturn(responseXml);
			when(docMgr.storeDocument("a0001.xml", "{\"content\": \"updated content\"}", propsJson)).thenReturn(responseJson);
			when(docMgr.getDocument("a0001.xml", propsXml)).thenReturn(responseXml);
			when(docMgr.getDocument("a0001.xml", propsJson)).thenReturn(responseJson);
		} catch (BagriException ex) {
			ex.printStackTrace();
		}
        BagriRestServer server = new BagriRestServer(mockPro, null, new Properties());
        return server.buildConfig();
    }

    @Test
    public void testDocumentService() throws Exception {
    	
    	// create document
    	DocumentParams params = new DocumentParams("a0001.xml", "<content>initial content</content>", propsXml);
        Response resp = target("docs").request()
        		.header("Content-Type", "application/json")
        		.cookie(bg_cookie, "client-id")
        		.post(Entity.json(params));
        assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());
        assertTrue(resp.getLocation().getPath().endsWith("/a0001.xml"));
        DocumentBean doc = resp.readEntity(DocumentBean.class);
        assertEquals("a0001.xml", doc.uri);
        // get initial content
        String content = target("docs").path("a0001.xml")
        		.request(MediaType.APPLICATION_XML)
        		.cookie(bg_cookie, "client-id")
        		.get(String.class);
        assertEquals("<content>initial content</content>", content);
        
        // update document
        params = new DocumentParams("a0001.xml", "{\"content\": \"updated content\"}", propsJson);
        resp = target("docs").request()
        		.header("Content-Type", "application/json")
        		.cookie(bg_cookie, "client-id")
        		.post(Entity.json(params));
        assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());
        assertTrue(resp.getLocation().getPath().endsWith("/a0001.xml"));
        doc = resp.readEntity(DocumentBean.class);
        assertEquals("a0001.xml", doc.uri);
        // get updated content
        content = target("docs").path("a0001.xml")
        		.request(MediaType.APPLICATION_JSON)
        		.cookie(bg_cookie, "client-id")
        		.get(String.class);
        assertEquals("{\"content\": \"updated content\"}", content);
        
        // delete document
        String uri = target("docs").path("a0001.xml")
        		.request()
        		.cookie(bg_cookie, "client-id")
        		.delete(String.class);
        assertEquals("a0001.xml", uri);
    }    

    @Test
    public void testDocumentPatterns() throws Exception {
    	// implement it..
    }
    
}
