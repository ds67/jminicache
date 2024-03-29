package com.github.ds67.jminicache.impl.storage;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.github.ds67.jminicache.impl.eviction.EvictionManagerIF;
import com.github.ds67.jminicache.impl.guard.GuardIF;

public class MapBasedCacheManager<Key, Value, Wrapper> implements StorageManagerIF<Key, Value, Wrapper>{

	private final EvictionManagerIF<Key, Value, Wrapper> evictionManager;
	
	private final GuardIF guard; 
	
	private Map<Key, Wrapper> cache = new HashMap<>();
	
	public MapBasedCacheManager(Supplier<Map<Key, Wrapper>> mapConstructor,
			                    GuardIF guard, 
			                    EvictionManagerIF<Key, Value, Wrapper> evictionManager) {
		this.guard=guard;
		this.evictionManager=evictionManager;
		this.cache = mapConstructor.get();
	}

	@Override
	public GuardIF getGuard ()
	{
		return this.guard;		
	}

	@Override
	public Wrapper wrap(Key k, Value v) {
		return evictionManager.createWrapper(k, v);
	}
	
	public Value unwrap (Wrapper w) {
		return evictionManager.unwrap(w);
	}

	@Override
	public Key getForDeletion() {
		return evictionManager.getForDeletion();
	}

	@Override
	public Value put (Key key, Value value, BiFunction<Key, Value, Wrapper> wrapper)
	{
		if (wrapper==null) wrapper=this::wrap;
		final var w = wrapper.apply(key, value);
		var oldWrapper = cache.put(key,w);
		evictionManager.onWrite(cache, w, oldWrapper);	
		return unwrap(oldWrapper);
	}

	@Override
	public int cachesize ()
	{
		return cache.size();
	}

	@Override
	public Value get (final Key key)
	{ 
		final var w = cache.get(key);
		evictionManager.onRead(cache, w);		
		return unwrap(w);
	}
	
	@Override
	public Value remove (Key key)
	{
		final var w = cache.remove(key);
		if (w!=null) {
			evictionManager.onDeletion(cache, w);
			return unwrap(w);
		}

		// Key was already delete (because it was never in or some other thread removed it already)
		return null;
	}
	
	@Override
	public boolean contains (Key key)
	{
		return cache.containsKey(key);
	}
	
	@Override
	public void clear ()
	{
		cache.clear();
		evictionManager.onClear();
	}
	
	 @Override
	 public Set<Key> keySet ()
	 {
		 return cache.keySet();
	 }
	 
	 @Override
 	 public Set<Map.Entry<Key, Value>> entrySet() {
		 Set<Map.Entry<Key, Value>> result = new HashSet<>();
		 for (var entry: cache.entrySet())  {
			 result.add(new AbstractMap.SimpleEntry<Key,Value>(entry.getKey(), unwrap(entry.getValue())));
		 }
		 return result;
	 }
	 
	 @Override
	 public Collection<Value> values ()
	 {
		 ArrayList<Value> result = new ArrayList<>(cache.size());
		 for (var entry: cache.values())  {
			 result.add(unwrap(entry));
		 }
		 return result;
	 }
}
