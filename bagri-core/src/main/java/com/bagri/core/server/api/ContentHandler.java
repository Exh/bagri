package com.bagri.core.server.api;

import java.util.Properties;

import com.bagri.core.api.ContentSerializer;

public interface ContentHandler {
	
	/**
	 *  
	 * @return the corresponding DataFormat abbreviation
	 */
	//String getDataFormat();
	
	/**
	 * 
	 * @return true if content is a String, false otherwise
	 */
	boolean isStringFormat();
	
	/**
	 * 
	 * @return Builder which is used to build content with corresponding data format 
	 */
	ContentBuilder<?> getBuilder();
	
	/**
	 * 
	 * @param source type of the data to convert from 
	 * @return converter to convert from source class to Handler's format class
	 */
	ContentConverter<?, ?> getConverter(Class<?> source);
	
	/**
	 * 
	 * @return Merger which merge documents
	 */
	ContentMerger<?> getMerger();
	
	/**
	 * 
	 * @return Modeler which process schemas for the data format
	 */
	ContentModeler getModeler();
	
	/**
	 * 
	 * @return Parser which is used to parse original documents in corresponding data format
	 */
	ContentParser<?> getParser();
	
	/**
	 * 
	 * @return serializer to read/write document's content from/to the underlying stream 
	 */
	ContentSerializer<?> getSerializer();
	
	/**
	 * 
	 * @param props properties for all internal components
	 */
	void init(Properties props);

}
