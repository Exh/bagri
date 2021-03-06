package com.bagri.server.hazelcast.store;

import static com.bagri.core.Constants.pn_node_instance;
import static com.bagri.core.Constants.pn_schema_name;
import static com.bagri.core.Constants.pn_schema_store_data_path;
import static com.bagri.core.Constants.pn_schema_store_tx_buffer_size;
import static com.bagri.support.util.FileUtils.buildStoreFileName;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.api.TransactionState;
import com.bagri.core.model.Transaction;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

public class TransactionCacheStore implements MapStore<Long, Transaction>, MapLoaderLifecycleSupport {

    private static final Logger logger = LoggerFactory.getLogger(TransactionCacheStore.class);
	private static final int byLen = 26;
	private static final int txLen = 64;
	private static final int szOff = 4;

    private int bSize = 2048;
	private BitSet bits;
    private FileChannel fc;
    private String schemaName;
	private RandomAccessFile raf;
    private MappedByteBuffer buff;
    private Map<Long, Long> transactions;
    
	@Override
	public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
		logger.trace("init.enter; properties: {}", properties);
		schemaName = properties.getProperty(pn_schema_name);
		String dataPath = properties.getProperty(pn_schema_store_data_path);
		String nodeNum = properties.getProperty(pn_node_instance);
		String fileName = buildStoreFileName(dataPath, nodeNum, "txlog");
		String buffSize = properties.getProperty(pn_schema_store_tx_buffer_size);
		if (buffSize != null) {
			bSize = Integer.parseInt(buffSize);
		}
		logger.info("init; opening tx log from file: {}; buffer size: {} tx", fileName, bSize);

		bits = new BitSet(bSize);
		int size = bit2pos(bSize);
		try {
			raf = new RandomAccessFile(fileName, "rw");
		} catch (FileNotFoundException ex) {
			throw new RuntimeException("Path " + fileName + " does not exists", ex);
		}
		
		int txCount = 0;
		try {
			if (raf.length() > 0) {
				logger.info("init; opened tx log with length: {}", raf.length());
				// not sure we have to do this..
				if (raf.length() > size) {
					size = (int) raf.length();
				}
			}
			fc = raf.getChannel();
			buff = fc.map(MapMode.READ_WRITE, 0, size);
			if (raf.length() > 0) {
				txCount = buff.getInt();
				transactions = new HashMap<>(txCount);
			} else {
				transactions = new HashMap<>();
			}
		} catch (IOException ex) {
			throw new RuntimeException("Cannot initialize Transaction Store", ex);
		}
		logger.info("init; tx buffer initialized; tx count: {}", txCount);
		loadTransactions(txCount);
	}

	@Override
	public void destroy() {
		logger.trace("destroy.enter;");
		try {
			//buff.compact();
			fc.close();
			raf.close();
		} catch (IOException ex) {
			logger.error("destroy.error", ex);
		}
	}
	
	private void loadTransactions(int txCount) {
		logger.trace("loadTransactions.enter; txCount: {}", txCount);
		int idx = 0;
		int tidx = 0;
		bits.clear();
		transactions.clear();
		buff.position(szOff);
		while (tidx < txCount) {
			int pos = buff.position();
			// read just state here!
			Transaction xtx = readTx();
			if (TransactionState.commited != xtx.getTxState()) {
				transactions.put(xtx.getTxId(), (long) pos);
				bits.set(idx);
				tidx++;
			}
			idx++;
		}
		buff.position(nextBit()); 
		logger.info("loadTransactions.exit; transactions: {}; bits: {}", transactions, bits);
	}
	
	public int getStoredCount() {
		int count = bits.cardinality();
		logger.trace("getStoredCount; returning: {}", count);
		return count;
	}
	
	@Override
	public Transaction load(Long key) {
		logger.trace("load.enter; key: {}", key);
		Transaction result = null;
		Long position = transactions.get(key);
		if (position != null) {
			synchronized (buff) {
				buff.position(position.intValue());
				result = readTx();
			}
		}
		logger.trace("load.exit; returning: {}", result);
		return result;
	}

	@Override
	public Map<Long, Transaction> loadAll(Collection<Long> keys) {
		logger.trace("loadAll.enter; keys: {}", keys);
		Map<Long, Transaction> result = new HashMap<>(keys.size());
		synchronized (buff) {
			for (Long key: keys) {
				Long position = transactions.get(key);
				if (position != null) {
					buff.position(position.intValue());
					Transaction xtx = readTx();
					result.put(key, xtx);
				}
			}
		}
		logger.trace("loadAll.exit; returning: {}", result);
		return result;
	}

	@Override
	public Set<Long> loadAllKeys() {
		logger.trace("loadAllKeys.enter;");
		Set<Long> result = transactions.keySet(); 
		logger.trace("loadAllKeys.exit; returning: {} for tx count: {}", result, bits.cardinality());
		return result;
	}
	
	@Override
	public void delete(Long key) {
		logger.trace("delete.enter; key: {}", key);
		Long position = transactions.remove(key);
		if (position != null) {
			int pos = position.intValue();
			bits.clear(pos2bit(pos));
			buff.put(pos, (byte) TransactionState.commited.ordinal());
			buff.putInt(0, bits.cardinality());
		}
		logger.trace("delete.exit; deleted: {}; tx count: {}", position != null, bits.cardinality());
	}

	@Override
	public void deleteAll(Collection<Long> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys);
		int cnt = 0;
		for (Long key: keys) {
			Long position = transactions.remove(key);
			if (position != null) {
				int pos = position.intValue();
				bits.clear(pos2bit(pos));
				buff.put(pos, (byte) TransactionState.commited.ordinal());
				buff.putInt(0, bits.cardinality());
				cnt++;
			}
		}
		logger.trace("deleteAll.exit; deleted: {}; tx count: {}", cnt, bits.cardinality());
	}
	
	@Override
	public void store(Long key, Transaction value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
		int pos;
		Long position = transactions.get(key);
		if (position == null) {
			int bit = nextBit();
			pos = bit2pos(bit);
			//logger.trace("store; got pos: {}; bits: {}", pos, bits);
			position = new Long(pos);
			bits.set(bit);
			transactions.put(key, position);
			buff.putInt(0, bits.cardinality());
		} else {
			pos = position.intValue();
		}
		buff.position(pos);
		writeTx(value);
		logger.trace("store.exit; stored tx {} at position: {}; tx count: {}", key, position, bits.cardinality());
	}

	@Override
	public void storeAll(Map<Long, Transaction> entries) {
		logger.trace("storeAll.enter; entries: {}", entries);
		int cnt = 0;
		for (Map.Entry<Long, Transaction> xtx: entries.entrySet()) {
			int pos;
			Long position = transactions.get(xtx.getKey());
			if (position == null) {
				int bit = nextBit();
				pos = bit2pos(bit);
				position = new Long(pos);
				bits.set(bit);
				transactions.put(xtx.getKey(), position);
				buff.putInt(0, bits.cardinality());
			} else {
				pos = position.intValue();
			}
			buff.position(pos);
			writeTx(xtx.getValue());
			cnt++;
		}
		logger.trace("store.exit; stored {} transactions for {} entries; tx count: {}", cnt, entries.size(), bits.cardinality());
	}

	private int nextBit() {
		int bit = bits.nextClearBit(0);
		if (bit > bSize) {
			logger.warn("nextBit; the buffer exceeds configured size: {}; next is: {}", bSize, bit);
		}
		return bit;
	}
	
	private int bit2pos(int bit) {
		return bit*txLen + szOff;
	}
	
	private int pos2bit(int pos) {
		return (pos - szOff)/txLen;
	}
	
	private Transaction readTx() {
		TransactionState state = TransactionState.values()[buff.get()];
		long id = buff.getLong();
		long start = buff.getLong();
		long finish = buff.getLong();
		TransactionIsolation isol = TransactionIsolation.values()[buff.get()];
		byte[] by26 = new byte[byLen];
		buff.get(by26);
		int sz = 0;
		while (sz < byLen && by26[sz] > 0) {
			sz++;
		}
		String owner = new String(by26, 0, sz);
		Transaction xtx = new Transaction(id, start, finish, owner, isol, state);
		xtx.updateCounters(buff.getInt(), buff.getInt(), buff.getInt());
		return xtx;
	}

	private void writeTx(Transaction xtx) {
		buff.put((byte) xtx.getTxState().ordinal());
		buff.putLong(xtx.getTxId());
		buff.putLong(xtx.getStartedAt());
		buff.putLong(xtx.getFinishedAt());
		buff.put((byte) xtx.getTxIsolation().ordinal());
		buff.putInt(xtx.getDocsCreated());
		buff.putInt(xtx.getDocsUpdated());
		buff.putInt(xtx.getDocsDeleted());
		byte[] by = xtx.getStartedBy().getBytes();
		byte[] by26 = new byte[byLen];
		for (int i=0; i < by.length; i++) {
			by26[i] = by[i];
		}
		buff.put(by26);
		//buff.force();
	}

}



