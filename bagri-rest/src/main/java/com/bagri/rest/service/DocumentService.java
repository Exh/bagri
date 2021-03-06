package com.bagri.rest.service;

import static com.bagri.core.Constants.pn_client_fetchSize;
import static com.bagri.core.Constants.pn_document_data_format;
import static com.bagri.core.Constants.pn_document_headers;
import static com.bagri.core.system.DataFormat.df_json;
import static com.bagri.core.system.DataFormat.df_xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.SchemaRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * JAX-RS documents resource, contains methods for documents management. Accessible on /docs path. 
 * 
 * @author Denis Sukhoroslov
 *
 */
@Path("/docs")
@Api(value = "documents")
public class DocumentService  extends RestService {
	
    
    private DocumentManagement getDocManager() {
    	SchemaRepository repo = getRepository();
    	if (repo != null) {
        	return repo.getDocumentManagement();
    	}
		return null;
    }
    
	@GET
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "getDocuments: return Document uris corresponding to the specified query")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "success"), 
			@ApiResponse(code = 401, message = "unauthorized"), 
			@ApiResponse(code = 503, message = "internal server error") 
	})
    public Response getDocuments(
    		@ApiParam(value = "query", example = "createdBy = admin, bytes > 3000, uri like security%, collections.contains(securities)")
    		@QueryParam("query") String query, 
    		@ApiParam(value = "page number", example = "1")
    		@DefaultValue("1") @QueryParam("page") int page, 
    		@ApiParam(value = "page size", example = "50")
    		@DefaultValue("100") @QueryParam("size") int size) {
		// add paginaton, pattern
		logger.trace("getDocuments.enter; query: {}, page: {}, size: {}", query, page, size);
		DocumentManagement docMgr = getDocManager();
		Properties props = new Properties();
		props.setProperty(pn_client_fetchSize, String.valueOf(size));
		props.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_URI));
    	try (ResultCursor<DocumentAccessor> uris = docMgr.getDocuments(query, props)) {
   			List<String> names = new ArrayList<>(); //itr.size());
   			for (DocumentAccessor uri: uris) {
   				names.add(uri.getUri());
   			}
            Collections.sort(names);
            DocumentBean[] docs = new DocumentBean[size];
            long now = new java.util.Date().getTime();
            int start = page == 0 ? 0 : (page - 1) * size;
            if (start >= names.size()) {
            	start = names.size() - size;
            } 
            for (int i = 0; i < size && start + i < names.size(); i++) {
            	String uri = names.get(i);
            	docs[i] = new DocumentBean(uri, now, "owner", "xml", "utf-8", 1000);
            }
            return Response.ok(docs).build();
    	} catch (Exception ex) {
    		logger.error("getDocuments.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }
    
    @GET
    @Path("/{uri}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON}) 
	@ApiOperation(value = "getDocumentContent: return content of the specified Document")
    public Response getDocumentContent(@PathParam("uri") String uri, @Context HttpHeaders hh) {
		DocumentManagement docMgr = getDocManager();
    	try {
    		Properties props = new Properties();
    		if (MediaType.APPLICATION_JSON_TYPE.equals(hh.getAcceptableMediaTypes().get(0))) {
    	    	props.setProperty(pn_document_data_format, df_json);
    		} else if (MediaType.APPLICATION_XML_TYPE.equals(hh.getAcceptableMediaTypes().get(0))) {
    	    	props.setProperty(pn_document_data_format, df_xml);
    		}
            DocumentAccessor doc = docMgr.getDocument(uri, props);
            String content = doc.getContent();
            return Response.ok(content).build();
    	} catch (Exception ex) {
    		logger.error("getDocumentContent.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }    

    @POST
    @Consumes(MediaType.APPLICATION_JSON) 
    @Produces(MediaType.APPLICATION_JSON) 
	@ApiOperation(value = "postDocument: creates a new Document or new version if Document with provided uri already exists")
	public Response postDocument(final DocumentParams params) {
		DocumentManagement docMgr = getDocManager();
    	try {
    		logger.trace("postDocument; got params: {}", params);
            DocumentAccessor doc = docMgr.storeDocument(params.uri, params.content, params.props);
     		logger.trace("postDocument; got document: {}", doc);
            DocumentBean dr = new DocumentBean(doc.getUri(), doc.getCreateadAt(), doc.getCreatedBy(), doc.getContentType(), 
            		doc.getEncoding(), doc.getSizeInBytes());
            return Response.created(UriBuilder.fromPath("/docs/" + dr.uri).build()).entity(dr).build();
    	} catch (Exception ex) {
    		logger.error("postDocument.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }
    
    @DELETE
    @Path("/{uri}")
    @Produces(MediaType.APPLICATION_JSON) 
	@ApiOperation(value = "deleteDocument: deletes Document by its uri")
	public Response deleteDocument(@PathParam("uri") String uri) {
		DocumentManagement docMgr = getDocManager();
    	try {
            docMgr.removeDocument(uri, null);
            return Response.ok(uri).build();
    	} catch (Exception ex) {
    		logger.error("deleteDocument.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }
    
    @PUT
    @Path("/{uri}")
    @Consumes(MediaType.APPLICATION_JSON) 
    @Produces(MediaType.APPLICATION_JSON) 
	@ApiOperation(value = "changeDocumentCollections: adds/removes Document to/from the specified Collections")
	public Response changeDocumentCollections(@PathParam("uri") String uri, CollectionParams params) {
		DocumentManagement docMgr = getDocManager();
    	try {
    		int cnt = 0;
    		if (params.add) {
    			cnt = docMgr.addDocumentToCollections(uri, params.collections);
    		} else {
    			cnt = docMgr.removeDocumentFromCollections(uri, params.collections);
    		}
            return Response.ok(cnt).build();
    	} catch (Exception ex) {
    		logger.error("changeDocumentCollections.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    		//return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
    	}
    }
}


