package com.github.ds67.jminicache;

public class CacheChangeEvent<Key, Value> {

	/** Possible operations which can occur in the cache
	 * 
	 * @author Jens Ketterer
	 *
	 */
	public static enum Operations {
		/** A fetch is a access to a key of the cache. The number of fetches is typically higher than the number of gets
		 * 
		 */
		FETCH_VALUE,
		/**
		 * Cache access from outside. Will result in on or more fetches.
		 * More than one fetch occur when the value is not in the cache and there is shared read lock (multiple thread read in parallel). Then an exclusive lock is
		 * acquired a the fetch is retried as some other thread could habe inserted the necessary information
		 */
		
		GET_VALUE,
		/**
		 * Number of writes to the cache. This includes inserts, updates and refreshes (which is only an internal update).
		 */
		
		SET_VALUE,
		/**
		 * Number of values which are removed from the cache. A removal might be triggered by cache usage or by shrinks due to eviction.
	     * 
		 */
		
		REMOVE_VALUE,
		/**
		 * Called whenever the cache grows to large a a key must be removed based on the eviction strategy. Every shrink operation
		 * includes a REMOVE_VALUE operation
		 */
		
		SHRINK,
		/**
		 * Called whenever an expired value is refreshed. Every refresh includes a UPDATE_VALUE operation
		 */
		
		REFRESH_VALUE,
		/**
		 * Called then the cache is cleared
		 */
		CLEAR,
		
		/** Called whenever a key was not found in the cache. Every MISSED_VALUE operation was preceded by an FETCH_VALUE  
		 * A miss might result in a second FETCH_VALUE or n an UPDATE_VALUE
		 */
		MISSED_VALUE,
		
		/** Called whenever another thread create the VALUE for the current key and the thread has to wait until its fetched.
		 * 
		 */
		KEY_FETCH_COLLISION,
		
		/** Called whenever a value is expired. 
		 * May result in a REFRESH_VALUE and UPDATE_VALUE operation 
		 */
		EXPIRED_VALUE
	}
	
	private final Key key;
	private final Value oldValue;
	private final Value newValue;
	private final Operations operation;
	
	public CacheChangeEvent(final Operations operation, final Key key, final Value oldValue, final Value newValue) 
	{
		this.key = key;
		this.oldValue=oldValue;
		this.newValue=newValue;
		this.operation=operation;
	}

	public boolean isSet()
	{
		return newValue!=null;
	}
	
	public boolean isDelete ()
	{
		return oldValue!=null && newValue==null;
	}
	
	public boolean isUpdate ()
	{
		return oldValue!=null && newValue!=null;
	}
	
	public boolean isNew ()
	{
		return oldValue==null && newValue!=null;
	}

	public boolean isClear()
	{
		return key==null && oldValue==null && newValue==null;
	}
	
	public Key getKey() {
		return key;
	}

	public Value getOldValue() {
		return oldValue;
	}

	public Value getNewValue() {
		return newValue;
	}
	
	Operations getOperation()
	{
		return operation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
		result = prime * result + ((oldValue == null) ? 0 : oldValue.hashCode());
		result = prime * result + ((operation == null) ? 0 : operation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CacheChangeEvent other = (CacheChangeEvent) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (newValue == null) {
			if (other.newValue != null)
				return false;
		} else if (!newValue.equals(other.newValue))
			return false;
		if (oldValue == null) {
			if (other.oldValue != null)
				return false;
		} else if (!oldValue.equals(other.oldValue))
			return false;
		if (operation != other.operation)
			return false;
		return true;
	}
}
