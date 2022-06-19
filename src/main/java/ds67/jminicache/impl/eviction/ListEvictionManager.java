package ds67.jminicache.impl.eviction;

import java.util.function.BiFunction;

import ds67.jminicache.impl.payload.ListWrapper;
import ds67.jminicache.impl.payload.PayloadIF;

class ListEvictionManager<Key, Value, Wrapper extends ListWrapper<Key, Value, ? extends PayloadIF<Key, Value>>> {

	private Wrapper lastEntry = null;
	private Wrapper firstEntry = null;
	
	ListEvictionManager (final BiFunction<Key, Value, Wrapper> constructor)
	{
		this.constructor=constructor;
	}
	
	protected void append (final Wrapper w)
	{
		if (lastEntry!=null) {
			lastEntry.setSucc(w);
			w.setPred(lastEntry);
		}
		lastEntry = w;
		if (firstEntry==null) firstEntry=lastEntry;
	}

	@SuppressWarnings("unchecked")
	protected void delete (final Wrapper w)
	{
		w.onRemove();
		
		if (firstEntry==w) {
			firstEntry=(Wrapper)w.getSucc();
		}
		if (lastEntry==w) {
			lastEntry=(Wrapper)w.getPred();
		}
	}
	
	private final BiFunction<Key, Value, Wrapper> constructor;
	
	public Wrapper createWrapper(Key k, Value v) {
		return constructor.apply(k, v);
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
