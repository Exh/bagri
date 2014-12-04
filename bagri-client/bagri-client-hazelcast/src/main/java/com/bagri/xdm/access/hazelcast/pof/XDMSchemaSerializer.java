package com.bagri.xdm.access.hazelcast.pof;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMSchemaSerializer extends XDMEntitySerializer implements StreamSerializer<XDMSchema> {

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XDMSchema;
	}

	@Override
	public XDMSchema read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		XDMSchema xSchema = new XDMSchema(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readUTF(),
				in.readBoolean(),
				(Properties) in.readObject()); 
		return xSchema;
	}

	@Override
	public void write(ObjectDataOutput out, XDMSchema xSchema)	throws IOException {
		super.writeEntity(out, xSchema);
		out.writeUTF(xSchema.getName());
		out.writeUTF(xSchema.getDescription());
		out.writeBoolean(xSchema.isActive());
		out.writeObject(xSchema.getProperties());
	}

}