package com.bagri.server.hazelcast.task.trigger;

import static com.bagri.server.hazelcast.serialize.TaskSerializationFactory.cli_factory_id;
import static com.bagri.server.hazelcast.serialize.TaskSerializationFactory.cli_RemoveTriggerTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class TriggerRemover implements Callable<Boolean>, IdentifiedDataSerializable { 
	
	private static final transient Logger logger = LoggerFactory.getLogger(TriggerRemover.class);
	
	private String trigger;
	private transient SchemaRepositoryImpl xdmRepo;
    
	public TriggerRemover() {
		//
	}
	
	public TriggerRemover(String trigger) {
		this.trigger = trigger;
	}

    @Autowired
	public void setXDMRepository(SchemaRepositoryImpl xdmRepo) {
		this.xdmRepo = xdmRepo;
	}
	
	@Override
	public Boolean call() throws Exception {
		logger.trace("call.enter");
		long stamp = System.currentTimeMillis();
		boolean result = xdmRepo.dropSchemaTrigger(trigger);
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("call.exit; returning: {}; time taken: {}", result, stamp);
		return result;
	}
    
	@Override
	public int getId() {
		return cli_RemoveTriggerTask;
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		trigger = in.readUTF();
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(trigger);
	}


}
