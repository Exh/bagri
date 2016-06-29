package com.bagri.xdm.client.hazelcast.serialize.domain;

import java.io.IOException;

import com.bagri.xdm.api.TransactionIsolation;
import com.bagri.xdm.api.TransactionState;
import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.domain.Transaction;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class TransactionSerializer implements StreamSerializer<Transaction> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMTransaction;
	}

	@Override
	public Transaction read(ObjectDataInput in) throws IOException {
		
		Transaction xTrans = new Transaction(in.readLong(),
				in.readLong(),
				in.readLong(),
				in.readUTF(),
				TransactionIsolation.values()[in.readInt()],
				TransactionState.values()[in.readInt()]);
		xTrans.updateCounters(in.readInt(), in.readInt(), in.readInt());
		return xTrans;
	}

	@Override
	public void write(ObjectDataOutput out, Transaction xTrans) throws IOException {
		
		out.writeLong(xTrans.getTxId());
		out.writeLong(xTrans.getStartedAt());
		out.writeLong(xTrans.getFinishedAt());
		out.writeUTF(xTrans.getStartedBy());
		out.writeInt(xTrans.getTxIsolation().ordinal());
		out.writeInt(xTrans.getTxState().ordinal());
		out.writeInt(xTrans.getDocsCreated());
		out.writeInt(xTrans.getDocsUpdated());
		out.writeInt(xTrans.getDocsDeleted());
	}

}
