package ds67.jminicache.impl.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import ds67.jminicache.impl.eviction.EvictionManagerIF;
import ds67.jminicache.impl.guard.GuardIF;
import ds67.jminicache.impl.payload.PayloadIF;

public class SimpleCacheManager<Key, Value, Wrapper extends PayloadIF<Key, Value>> implements StorageManagerIF<Key, Value>{

	private final EvictionManagerIF<Key, Value, Wrapper> evictionManager;
	
	private final GuardIF guard; 
	
	public SimpleCacheManager(GuardIF guard, EvictionManagerIF<Key, Value, Wrapper> evictionManager) {
		this.guard=guard;
		this.evictionManager=evictionManager;
	}

	@Override
	public GuardIF getGuard ()
	{
		return this.guard;		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onRead(final PayloadIF<Key, Value> w) {
		evictionManager.onRead(cache, (Wrapper)w);		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onBeforeWrite(final PayloadIF<Key, Value> w) {
		evictionManager.onBeforeWrite(cache, (Wrapper)w);	
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onDeletion(final PayloadIF<Key, Value> w) {
		evictionManager.onDeletion(cache, (Wrapper)w);
	}

	@Override
	public Wrapper createWrapper(Key k, Value v) {
		return evictionManager.createWrapper(k, v);
	}

	@Override
	public PayloadIF<Key, Value> getForDeletion() {
		return evictionManager.getForDeletion();
	}

	private HashMap<Key, Wrapper> cache = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public PayloadIF<Key, Value> put (Key key, PayloadIF<Key, Value> value)
	{
		return cache.put(key,(Wrapper)value);
	}

	@Override
	public int cachesize ()
	{
		return cache.size();
	}

	@Override
	public PayloadIF<Key, Value> get (final Key key)
	{ 
		return cache.get(key); 
	}
	
	@Override
	public PayloadIF<Key, Value> remove (Key key)
	{
		return cache.remove(key);
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
	 
	 @SuppressWarnings("unchecked")
	 @Override
 	 public Collection<PayloadIF<Key, Value>> values() {
	    return (Collection<PayloadIF<Key, Value>>)cache.values();	 
	 }
}
