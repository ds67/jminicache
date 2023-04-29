package com.github.ds67.jminicache.plugin;

public interface Plugin<Key, Value> {
	
	void onBeforeFetch (Key k);
	
	/**
	 *
	 * @param key
	 * @param value
	 */
	void onAfterFetch (Key key, Value value);

	void onBeforeGet (Key k);
	
	/**
	 * Called after a key / value pair was retrieved from the cache but before the value is returned to the caller
	 * The cache is still locked when calling this method to keep <code>key</code> and <code>value</code> consistent
	 * 
	 * @param key
	 * @param value
	 */
	void onAfterGet(Key key, Value value);
	
	void onBeforeSet (Key k, Value value);
	
	/**
	 * Called after a new value was inserted in the cache. 
	 * The cache is still locked when calling this method to keep <code>key</code> and <code>value</code> consistent
	 * 
	 * @param key
	 * @param oldValue previously stored value for this key or <code>null</code> when key was not contained in the cache
	 * @param newValue
	 */
	void onAfterSet (Key key, Value oldValue, Value newValue);
	
	void onBeforeRemove (Key key);
	
	/**
	 * Called after a key was removed from the cache
	 * The cache is still locked when calling this method to keep <code>key</code> and <code>value</code> consistent
	 * 
	 * @param key
	 * @param value
	 */
	void onAfterRemove (Key key, Value value);
	
	/**
	 * Called whenever a key was not found in the cache
	 * 
	 * @param key
	 */
	void onMiss (Key key);
	
	/**
	 * Called whenever the cache blocks a thread because a value fetching for the same key takes place in another thread 
	 * 
	 * @param k
	 */
	void onValueCreateCollision(Key k);
	
	void onRefresh (Key key);
	
	void onShrink (Key key);
	
	void onClear ();
	
	void onExpire (Key key);
}
