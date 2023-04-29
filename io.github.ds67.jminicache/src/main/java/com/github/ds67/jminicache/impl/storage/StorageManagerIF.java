package com.github.ds67.jminicache.impl.storage;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import com.github.ds67.jminicache.impl.guard.GuardIF;

/**
 * The storage manager cares for the storage of the cached data. This is more or less just a map to access values by the key.
 * 
 * However when using a weak key storage the storage logic is slightly different therefore the storage manager is not part of the general 
 * cache logic but  
 * 
 * @author Jens Ketterer
 *
 * @param <Key> Type of the key to access the cached items
 * @param <Value> Type of the cached item
 */
public interface StorageManagerIF<Key, Value, Wrapper>
{
	public Value get (final Key key);
	public Value remove (Key key);
	public Value put (Key key, Value value, BiFunction<Key, Value, Wrapper> wrapper);
	
	public int cachesize ();
	
	/**
	 * Checks if a key is contained in the storage structure
	 * 
	 * @param key Key which should be checked.
	 * @return <code>true</code> when key exists in cache, <code>false</code> otherwise
	 */
	public boolean contains (Key key);
	
	/**
	 * This method provides the key which should be removed as next from the cache. 
	 * The decision which item should be removed is provided by the 
	 * {@link com.github.ds67.jminicache.impl.eviction} manager.
	 *
	 * @return next key which should be deleted or <code>null</code> when cache is empty
	 */
	public Key getForDeletion ();
	
	public Wrapper wrap (final Key k, final Value v);
	public Value unwrap (final Wrapper w);
	
	public GuardIF getGuard ();
	
	public void clear ();
	public Set<Key> keySet ();
	public Set<Map.Entry<Key, Value>> entrySet();
	public Collection<Value> values();
}
