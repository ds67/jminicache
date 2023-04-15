package com.github.ds67.jminicache.plugin;

public interface Plugin<Key, Value> {
	
	public void onBeforeGet (Key k);
	
	/**
	 * Called after a key / value pair was retrieved from the cache but before the value is returned to the caller
	 * The cache is still locked when calling this method to keep <code>key</code> and <code>value</code> consistent
	 * 
	 * @param key
	 * @param value
	 */
	public void onAfterGet (Key key, Value value);

	public void onBeforeSet (Key k, Value value);
	
	/**
	 * Called after a new value was inserted in the cache. 
	 * The cache is still locked when calling this method to keep <code>key</code> and <code>value</code> consistent
	 * 
	 * @param key
	 * @param oldValue previously stored value for this key or <code>null</code> when key was not contained in the cache
	 * @param newValue
	 */
	public void onAfterSet (Key key, Value oldValue, Value newValue);
	
	public void onBeforeRemove (Key key);
	
	/**
	 * Called after a key was removed from the cache
	 * The cache is still locked when calling this method to keep <code>key</code> and <code>value</code> consistent
	 * 
	 * @param key
	 * @param value
	 */
	public void onAfterRemove (Key key, Value value);
	
	/**
	 * Called whenever a key was not found in the cache
	 * 
	 * @param key
	 */
	public void onMiss (Key key);
	
	/**
	 * Called whenever the cache blocks a thread because a value fetching for the same key takes place in another thread 
	 * 
	 * @param k
	 */
	public void onValueCreateCollision(Key k);
	
	public void onRefresh (Key key);
	
	public void onShrink (Key key);
	
	public void onClear ();
}
