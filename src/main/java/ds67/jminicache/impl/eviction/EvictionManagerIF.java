package ds67.jminicache.impl.eviction;

import java.util.Map;

import ds67.jminicache.impl.payload.PayloadIF;

public interface EvictionManagerIF<Key, Value, Wrapper extends PayloadIF<Key, ?>> {
	
	public void onRead (final Map<Key, Wrapper> cache, final Wrapper w);
	public void onBeforeWrite (final Map<Key, Wrapper> cache, final Wrapper w);
	public void onDeletion (final Map<Key, Wrapper> cache, final Wrapper w);
	
	public void onClear ();
	
	public Wrapper getForDeletion ();
	
	public Wrapper createWrapper (final Key k, final Value v);
}