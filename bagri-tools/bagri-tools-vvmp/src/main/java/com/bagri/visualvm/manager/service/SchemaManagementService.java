package com.bagri.visualvm.manager.service;

import com.bagri.visualvm.manager.model.Property;
import com.bagri.visualvm.manager.model.Schema;

import javax.management.ObjectName;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface SchemaManagementService {

    List<Schema> getSchemas() throws ServiceException;
    Properties getDefaultProperties() throws ServiceException;
    void setDefaultProperty(Property property) throws ServiceException;
    void addSchema(Schema schema) throws ServiceException;
    Schema getSchema(String schemaName) throws ServiceException;
    Schema getSchema(ObjectName objectName) throws ServiceException;
    void saveSchema(Schema schema) throws ServiceException;
    void deleteSchema(Schema schema) throws ServiceException;
    List<String> parseQuery(Schema schema, String query) throws ServiceException;
    Object runQuery(Schema schema, boolean direct, String query, Properties props) throws ServiceException;
    Object runQueryWithParams(Schema schema, boolean direct, String query, Map<String, Object> params, Properties props) throws ServiceException;
    Properties getQueryProperties(String schemaName) throws ServiceException;
    long[] getSchemaVolumeStatistics(String schemaName) throws ServiceException;
    long[] getSchemaTransactionStatistics(String schemaName) throws ServiceException;

}
