package com.bagri.server.hazelcast.task.tx;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.TransactionManagement;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class TransactionAborter extends com.bagri.client.hazelcast.task.tx.TransactionAborter {

	private transient TransactionManagement txMgr;
    
	@Autowired
	@Override
	public void setRepository(SchemaRepository repo) {
		super.setRepository(repo);
		this.txMgr = repo.getTxManagement();
	}

    @Override
	public Boolean call() throws Exception {
		txMgr.rollbackTransaction(txId);
		return true;
	}
	
}
