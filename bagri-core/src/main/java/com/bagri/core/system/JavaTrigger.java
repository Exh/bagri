package com.bagri.core.system;

import java.util.Date;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents trigger implemented in Java.
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="javatrigger", namespace = "http://www.bagridb.com/schema/system", propOrder = {
		"library", 
		"className"
})
public class JavaTrigger extends TriggerDefinition {

	@XmlElement(required = false)
	private String library;
		
	@XmlElement(required = true)
	private String className;
	
	/**
	 * default constructor
	 */
	public JavaTrigger() {
		// for JAXB de-serialization
		super();
	}

	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param library the library containing trigger implementation
	 * @param className the implementing Java class name
	 * @param synchronous is trigger invoked synchronously or not
	 * @param enabled the trigger enabled flag
	 * @param collection the documents collection for which trigger is registered
	 */
	public JavaTrigger(int version, Date createdAt, String createdBy, String library, 
			String className, boolean synchronous, boolean enabled, String collection) {
		super(version, createdAt, createdBy, synchronous, enabled, collection);
		this.library = library;
		this.className = className;
	}
	
	/**
	 * 
	 * @return the trigger name
	 */
	public String getName() {
		return className;
	}

	/**
	 * 
	 * @return the library containing trigger implementation
	 */
	public String getLibrary() {
		return library;
	}

	/**
	 * 
	 * @return the implementing Java class name
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("library", library);
		result.put("className", className);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "JavaTrigger [library=" + library + ", className=" + className
				+ ", collection=" + getCollection() + ", enabled=" + isEnabled()
				+ ", actions=" + getActions().toString() + "]";
	}
	
}
