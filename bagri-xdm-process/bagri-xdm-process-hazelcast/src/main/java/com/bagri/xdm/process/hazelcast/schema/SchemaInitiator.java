package com.bagri.xdm.process.hazelcast.schema;

import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.cli_XDMInitSchemaTask;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.access.api.XDMSchemaDictionaryBase;
import com.bagri.xdm.access.hazelcast.impl.HazelcastSchemaDictionary;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStoreFactory;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaInitiator extends SchemaDenitiator {

	private Properties properties;
	
	public SchemaInitiator() {
		super();
	}

	public SchemaInitiator(String schemaName, Properties properties) {
		super(schemaName);
		this.properties = properties;
	}

	@Override
	public Boolean call() throws Exception {
		//return schemaManager.initSchema(schemaName, properties);
		
		HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(schemaName);
		if (hz != null) {
    		logger.debug("initSchema.exit; schema {} already started on instance: {}, returning", schemaName, hz);
    		return false;
		}
		
    	properties.setProperty("xdm.schema.name", schemaName);
    	PropertiesPropertySource pps = new PropertiesPropertySource(schemaName, properties);
    	
    	try {
    		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext();
    		ctx.getEnvironment().getPropertySources().addFirst(pps);
    		ctx.setConfigLocation("spring/schema-server-context.xml");
    		ctx.refresh();

    		hz = ctx.getBean("hzInstance", HazelcastInstance.class);
    		hz.getUserContext().put("appContext", ctx);
    		//hz.getConfig().getSecurityConfig().setEnabled(true);
    		logger.debug("initSchema.exit; schema {} started on instance: {}", schemaName, hz);
    		return true;
    	} catch (Exception ex) {
    		logger.error("initSchema.error; " + ex.getMessage(), ex);
    		return false;
    	}
	}
	
	@Override
	public int getId() {
		return cli_XDMInitSchemaTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		properties = in.readObject();
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(schemaName);
		out.writeObject(properties);
	}
	
}

