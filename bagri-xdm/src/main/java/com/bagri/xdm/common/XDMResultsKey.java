package com.bagri.xdm.common;

/**
 * The key is used for cached query results. Consists of query hash and parameters hash. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public class XDMResultsKey {

	protected int queryHash;
	protected long paramsHash;
	
	/**
	 * default constructor
	 */
	public XDMResultsKey() {
		//
	}
	
	/**
	 * 
	 * @param queryHash the query string hash
	 * @param paramsHash the query parameters hash
	 */
	public XDMResultsKey(int queryHash, long paramsHash) {
		this.queryHash = queryHash;
		this.paramsHash = paramsHash;
	}
	
	/**
	 * 
	 * @return teh query string hash
	 */
	public int getQueryHash() {
		return queryHash;
	}
	
	/**
	 * 
	 * @return the query parameters hash
	 */
	public long getParamsHash() {
		return paramsHash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (queryHash ^ (queryHash >>> 32));
		result = prime * result + (int) (paramsHash ^ (paramsHash >>> 32));
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		XDMResultsKey other = (XDMResultsKey) obj;
		if (queryHash != other.queryHash) {
			return false;
		}
		if (paramsHash != other.paramsHash) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [queryHash=" + queryHash + ",paramsHash=" + paramsHash + "]";
	}
	
}
