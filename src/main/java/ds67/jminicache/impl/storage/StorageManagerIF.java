package ds67.jminicache.impl.storage;

import java.util.Collection;
import java.util.Set;

import ds67.jminicache.impl.guard.GuardIF;
import ds67.jminicache.impl.payload.PayloadIF;

/**
 * The storage manager cares for the storage of the cached data. This is more or less just a map to access values by the key.
 * 
 * However when using a weak key storage the storage logic is slightly different therefore the storage manager is not part of the general 
 * cache logic but  
 * 
 * @author Jens Ketterer
 *
 * @param <Key>
 * @param <Value>
 */
public interface StorageManagerIF<Key, Value>
{
	public PayloadIF<Key, Value> get (final Key key);
	public PayloadIF<Key, Value> remove (Key key);
	public PayloadIF<Key, Value> put (Key key, PayloadIF<Key, Value> value);
	
	public int cachesize ();
	public boolean contains (Key key);
	
	public void onRead (final PayloadIF<Key, Value> w);
	public void onBeforeWrite (final PayloadIF<Key, Value> w);
	public void onDeletion (final PayloadIF<Key, Value> w);

	public PayloadIF<Key, Value> getForDeletion ();
	
	public PayloadIF<Key, Value> createWrapper (final Key k, final Value v);
	
	public GuardIF getGuard ();
	
	public void clear ();
	public Set<Key> keySet ();
	public Collection<PayloadIF<Key, Value>> values();
}
