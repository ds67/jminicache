package com.github.ds67.jminicache.impl.eviction;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.ds67.jminicache.impl.payload.ListWrapper;
import com.github.ds67.jminicache.impl.payload.PayloadIF;
import com.github.ds67.jminicache.impl.storage.StorageManagerIF;

/**
 * Base class for list based eviction strategies. 
 * 
 * All caches values for the {@link StorageManagerIF} are sored in {@link ListWrapper}
 * item which realize a double linked list for each entry in the storage access structure.
 * 
 * The manipulation of the doubly linked list takes O(1) time
 * 
 * The Manager gets two function which wrap and unwrap the <code>Value</code> object 
 * from the <code>Wrapper</code> object 
 * 
 * @author Jens Ketterer
 *
 * @param <Key> Key of the cache
 * @param <Value> Value of the cache
 * @param <Wrapper> Real object which ist stored as value by the storage manager.
 */
class ListEvictionManager<Key, Value, Wrapper extends ListWrapper<Key, Value, ? extends PayloadIF<Key, Value>>> {

	private Wrapper lastEntry = null;
	private Wrapper firstEntry = null;
	
	ListEvictionManager (final BiFunction<Key, Value, Wrapper> constructor,
			             final Function<Wrapper, Value> unWrapper)
	{
		this.wrapper=constructor;
		this.unWrapper=unWrapper;
	}
	
	protected void append (final Wrapper w)
	{
		if (lastEntry!=null) {
			lastEntry.setSucc(w);
		}
		w.setPred(lastEntry);
		w.setSucc(null);
		lastEntry = w;
		if (firstEntry==null) firstEntry=lastEntry;
	}

	@SuppressWarnings("unchecked")
	protected void delete (final Wrapper w)
	{
		// Remove element from queue by linking predecessor and successor
		w.onRemove();
		
		// If element was first or last of the queue adjust start or end pointer 
		if (firstEntry==w) {
			firstEntry=(Wrapper)w.getSucc();
		}

		if (lastEntry==w) {
			lastEntry=(Wrapper)w.getPred();
		}
	}
	
	private final BiFunction<Key, Value, Wrapper> wrapper;
	private final Function<Wrapper, Value> unWrapper;
	
	public Wrapper createWrapper(Key k, Value v) {
		return wrapper.apply(k, v);
	}
	
	public Value unwrap (Wrapper w)
	{ 
		if (w==null) return null;
		return unWrapper.apply(w);
	}
	
	protected Wrapper getFirstEntry ()
	{
		return firstEntry;
	}
	
	protected Wrapper getLastEntry ()
	{
		return lastEntry;
	}
	
	protected void clear ()
	{
		firstEntry=null;
		lastEntry=null;
	}
}
