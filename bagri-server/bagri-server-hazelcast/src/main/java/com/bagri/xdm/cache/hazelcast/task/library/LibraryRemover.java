package com.bagri.xdm.cache.hazelcast.task.library;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_DeleteLibraryTask;

import java.util.Map.Entry;

import com.bagri.xdm.system.XDMLibrary;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class LibraryRemover extends LibraryProcessor implements IdentifiedDataSerializable {

	public LibraryRemover() {
		//
	}
	
	public LibraryRemover(int version, String admin) {
		super(version, admin);
	}

	@Override
	public Object process(Entry<String, XDMLibrary> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMLibrary library = entry.getValue();
			if (library.getVersion() == getVersion()) {
				entry.setValue(null);
				auditEntity(AuditType.delete, library);
				return library;
			} else {
				// throw ex ?
				logger.warn("process; outdated library version: {}; entry version: {}; process terminated", 
						getVersion(), entry.getValue().getVersion()); 
			}
		} 
		return null;
	}	

	@Override
	public int getId() {
		return cli_DeleteLibraryTask;
	}
	

}
