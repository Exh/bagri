package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_ProvideDocumentsTask;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.TransactionAwareTask;
import com.bagri.core.api.ResultCollection;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentsProvider<T> extends TransactionAwareTask implements Callable<ResultCollection<T>> {
	
	protected String pattern;
	protected Properties props;
	
	public DocumentsProvider() {
		super();
	}
	
	public DocumentsProvider(String clientId, long txId, String pattern, Properties props) {
		super(clientId, txId);
		this.pattern = pattern;
		this.props = props;
	}

	@Override
	public ResultCollection<T> call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentsTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		pattern = in.readUTF();
		props = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(pattern);
		out.writeObject(props);
	}

}


