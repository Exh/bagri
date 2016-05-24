package com.bagri.xdm.client.hazelcast.serialize.domain;

import java.io.IOException;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMDocumentSerializer implements StreamSerializer<XDMDocument> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMDocument;
	}

	@Override
	public XDMDocument read(ObjectDataInput in) throws IOException {
		XDMDocument xDoc = new XDMDocument(
				in.readLong(),
				in.readUTF(),
				in.readInt(),
				in.readLong(),
				in.readLong(),
				new java.util.Date(in.readLong()),
				in.readUTF(),
				in.readUTF(),
				in.readInt(),
				in.readInt());
		xDoc.setCollections(in.readIntArray());
		return xDoc;
	}

	@Override
	public void write(ObjectDataOutput out, XDMDocument xDoc) throws IOException {
		out.writeLong(xDoc.getDocumentKey());
		out.writeUTF(xDoc.getUri());
		out.writeInt(xDoc.getTypeId());
		out.writeLong(xDoc.getTxStart());
		out.writeLong(xDoc.getTxFinish());
		out.writeLong(xDoc.getCreatedAt().getTime());
		out.writeUTF(xDoc.getCreatedBy());
		out.writeUTF(xDoc.getEncoding());
		out.writeInt(xDoc.getBytes());
		out.writeInt(xDoc.getElements());
		out.writeIntArray(xDoc.getCollections());
	}
	
}