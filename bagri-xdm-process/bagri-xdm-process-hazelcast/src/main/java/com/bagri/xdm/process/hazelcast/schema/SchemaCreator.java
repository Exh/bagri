package com.bagri.xdm.process.hazelcast.schema;

import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;

import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaCreator extends SchemaProcessor implements DataSerializable {
	
	private String description;
	private Properties properties;
	
	public SchemaCreator() {
		//
	}
	
	public SchemaCreator(String admin, String description, Properties properties) {
		super(1, admin);
		this.description = description;
		this.properties = properties;
	}

	@Override
	public Object process(Entry<String, XDMSchema> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() == null) {
			String schemaName = entry.getKey();
			XDMSchema schema = new XDMSchema(getVersion(), new Date(), getAdmin(), schemaName, 
					description, true, properties);
			if (initSchemaInCluster(schema) == 0) {
				schema.setActive(false);
			}
			entry.setValue(schema);
			auditEntity(AuditType.create, schema);

			if (schema.isActive()) {
				logger.debug("process; schema activated, starting population");
				SchemaPopulator pop = new SchemaPopulator(schema.getName());
				execService.submitToAllMembers(pop);
			}
			return schema;
		} 
		return null;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		description = in.readUTF();
		properties = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(description);
		out.writeObject(properties);
	}

}
