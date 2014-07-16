package com.bagri.xdm.process.hazelcast.schema;

import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.cli_XDMDenitSchemaTask;
import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.factoryId;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import com.bagri.xdm.access.api.XDMSchemaManagement;
import com.bagri.xdm.access.api.XDMSchemaManagerBase;
import com.bagri.xdm.process.hazelcast.pof.XDMDataSerializationFactory;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaDenitiator implements Callable<Boolean>, IdentifiedDataSerializable { // Portable {
	
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	protected String schemaName;
	
	public SchemaDenitiator() {
		//
	}

	public SchemaDenitiator(String schemaName) {
		// this();
		this.schemaName = schemaName;
	}

	@Override
	public Boolean call() throws Exception {
		//return schemaManager.denitSchema(schemaName);
		
    	logger.trace("call.enter; schema: {}", schemaName);
    	boolean result = false;
		// get hzInstance and close it...
		HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(schemaName);
		if (hz != null) {
			ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) hz.getUserContext().get("appContext");
			ctx.close();
			result = true;
		}
    	logger.trace("call.exit; schema {} deactivated: {}", schemaName, result);
		return result;
	}

	@Override
	public int getId() {
		return cli_XDMDenitSchemaTask;
	}

	@Override
	public int getFactoryId() {
		return XDMDataSerializationFactory.factoryId;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		schemaName = in.readUTF();
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(schemaName);
	}


}
