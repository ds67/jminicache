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
	public boolean contains (Key key);
	
	public Key getForDeletion ();
	
	public Wrapper wrap (final Key k, final Value v);
	public Value unwrap (final Wrapper w);
	
	public GuardIF getGuard ();
	
	public void clear ();
	public Set<Key> keySet ();
	public Set<Map.Entry<Key, Value>> entrySet();
	public Collection<Value> values();
}
