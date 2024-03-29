package com.github.ds67.jminicache.impl.eviction;

import java.util.Map;

/**
 * The eviction manager controls the removal of cached items when the cache gets too large.
 * 
 * It provides event slots to be informed about every read, insert or deletion of items (or the complete clearance 
 * of the cache).
 * @see #onRead(Map, Object)
 * @see #onWrite(Map, Object, Object)
 * @see #onDeletion(Map, Object)
 * @see #onClear()
 * 
 * It organizes the elements so that it can always provide the next item for deletion (by calling @see #getForDeletion())
 * 
 * As the item organization needs additional structural information every payload item will be wrapped with a Wrapper
 * type with contains the necessary properties for item ordering. 
 * 
 * @author jens
 *
 * @param <Key> Type of the cache key 
 * @param <Value> Type of the cache values (payload)
 * @param <Wrapper> Type of the wrapper which will help organizing the items in eviction order
 */
public interface EvictionManagerIF<Key, Value, Wrapper> {
	
	public void onRead (final Map<Key, Wrapper> cache, final Wrapper w);
	public void onWrite (final Map<Key, Wrapper> cache, final Wrapper newWrapper, final Wrapper oldWrapper);
	public void onDeletion (final Map<Key, Wrapper> cache, final Wrapper w);
	
	public void onClear ();
	
	public Key getForDeletion ();
	
	public Wrapper createWrapper (final Key k, final Value v);
	public Value unwrap (Wrapper w);
}