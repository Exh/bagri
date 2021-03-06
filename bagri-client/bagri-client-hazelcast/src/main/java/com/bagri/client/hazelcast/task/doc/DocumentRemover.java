package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_RemoveDocumentTask;

import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.core.api.DocumentAccessor;

public class DocumentRemover extends DocumentAwareTask implements Callable<DocumentAccessor> {

	public DocumentRemover() {
		super();
	}

	public DocumentRemover(String clientId, long txId, Properties props, String uri) {
		super(clientId, txId, props, uri);
	}

	@Override
	public int getId() {
		return cli_RemoveDocumentTask;
	}

	@Override
	public DocumentAccessor call() throws Exception {
		return null;
	}

}
