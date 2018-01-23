package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_FixedCollection;
import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.ResultCollection;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class FixedCollectionImpl implements ResultCollection, IdentifiedDataSerializable {
	
	protected ArrayList<DocumentAccessor> results;
	
	public FixedCollectionImpl() {
		//
	}

	public FixedCollectionImpl(int size) {
		results = new ArrayList<>(size);
	}
	
	public FixedCollectionImpl(Collection<DocumentAccessor> results) {
		this(results.size());
		this.results.addAll(results);
	}
	
	@Override
	public void close() throws Exception {
		results.clear();		
	}

	@Override
	public boolean add(DocumentAccessor result) {
		return results.add(result);
	}
	
	@Override
	public void finish() {
		// no-op
	}
	
	@Override
	public boolean isAsynch() {
		return false;
	}
	
	@Override
	public Iterator<DocumentAccessor> iterator() {
		return results.iterator();
	}
	
	@Override
	public int size() {
		return results.size();
	}
	
	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_FixedCollection;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		int size = in.readInt();
		results = new ArrayList<>(size);
		for (int i=0; i < size; i++) {
			results.add((DocumentAccessor) in.readObject());
		}
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(results.size());
		for (Object result: results) {
			out.writeObject(result);
		}
	}
	
}
