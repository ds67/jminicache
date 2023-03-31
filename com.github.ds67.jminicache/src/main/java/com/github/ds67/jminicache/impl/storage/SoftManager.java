package com.github.ds67.jminicache.impl.storage;

import java.lang.ref.ReferenceQueue;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.ds67.jminicache.impl.guard.GuardIF;
import com.github.ds67.jminicache.impl.payload.PayloadIF;

public class SoftManager<Key, Value, Wrapper extends PayloadIF<Key, Value>> implements StorageManagerIF<Key, Value, Wrapper>{

	private final StorageManagerIF<Key, Value, Wrapper> wrappedCacheManager;
	
	public SoftManager(final StorageManagerIF<Key, Value, Wrapper> wrapped, 
			           Constructor<Key, Value, Wrapper> constructor,
			           Function<Wrapper, Value> unWrapper) {
		this.wrapper=constructor;
		this.wrappedCacheManager=wrapped;
		this.unWrapper=unWrapper;
	}

	@Override
	public GuardIF getGuard ()
	{
		return wrappedCacheManager.getGuard();		
	}

	@Override
	public Key getForDeletion() {
		return wrappedCacheManager.getForDeletion();
	}

	@Override
	public int cachesize ()
	{
		return wrappedCacheManager.cachesize();
	}

	@Override
	public Value get (final Key key)
	{ 
		return wrappedCacheManager.get(key); 
	}
	
	@Override
	public Value remove (Key key)
	{
		return wrappedCacheManager.remove(key);
	}

	private final ReferenceQueue<Value> referenceQueue = new ReferenceQueue<>();
	
	public ReferenceQueue<Value> getReferenceQueue ()
	{
		return referenceQueue;
	}
	
	@SuppressWarnings("unchecked")
	protected void cleanup ()
	{
		PayloadIF<Key, Value> removed = null;
		while ((removed=(PayloadIF<Key, Value>)referenceQueue.poll())!=null) {
			wrappedCacheManager.remove(removed.getKey());
		}
	}
	
	@FunctionalInterface
	public interface Constructor<Key, Value, Wrapper>
	{
		public Wrapper apply (Key k, Value v, ReferenceQueue<Value> q);
	}
	
	private final Constructor<Key, Value, Wrapper> wrapper;
	private final Function<Wrapper, Value> unWrapper;
	
	@Override
	public Wrapper wrap(Key k, Value v) {
		return wrapper.apply(k,v,getReferenceQueue());
	}
	
	public Value unwrap (Wrapper w)
	{
		return unWrapper.apply(w);
	}

	@Override
	public Value put(Key key, Value value, BiFunction<Key, Value, Wrapper> wrapper) {
		if (wrapper==null) wrapper=this::wrap;
		return wrappedCacheManager.put(key, value, wrapper);
	}

	@Override
	public boolean contains (Key key)
	{
		return wrappedCacheManager.contains(key);
	}
	
	@Override 
	public void clear ()
	{
		wrappedCacheManager.clear();
	}
	
	 @Override
	 public Set<Key> keySet ()
	 {
		 return wrappedCacheManager.keySet();
	 }

	 @Override
 	 public Set<Map.Entry<Key,Value>> entrySet() {
		 return wrappedCacheManager.entrySet();
	}
	 
	 @Override
	 public Collection<Value> values ()
	 {
		 return wrappedCacheManager.values();
	 }
}
