package ds67.jminicache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import ds67.jminicache.impl.LRUManager;
import ds67.jminicache.impl.LRUWrapper;

public class MiniCache<Value>
{

	public MiniCache() {
		
	}

	private HashMap<Object, LRUWrapper<Object, Value>> cache;
	
	private final ReadWriteLock guard = new ReentrantReadWriteLock();
	
	private final Map<Object,ReadWriteLock> creationGuards = new HashMap<>();
	
	private LRUManager<Object, Value> manager = new LRUManager<>();
	
	public Value get (final Object key, Supplier<Value> supplier) throws Exception
	{
		Lock locked = null;
		
		try {
			guard.readLock().lock();
			locked=guard.readLock();
			LRUWrapper<Object, Value> wrappedValue = cache.get(key);
			if (wrappedValue!=null) {		
				wrappedValue.removeHint();
				manager.setLast(wrappedValue);
				return wrappedValue.getPayload();
			}
			
			if (wrappedValue==null && supplier==null) {
				return null;
			}
			
			guard.writeLock().lock();
			locked = guard.writeLock();
		
			var localGuard = creationGuards.get(key);
			if (localGuard!=null) {
				// somebody else called supplier, simple wait until finished
				try {
					localGuard.readLock().lock();
					return get(key);
				}
				finally {
					localGuard.readLock().unlock();
				}
			}
			else {
				localGuard = new ReentrantReadWriteLock();
				localGuard.writeLock().lock();
				creationGuards.put(key, localGuard);
			}
			
			guard.writeLock().unlock ();
			locked=null;
		
			try {
				final var value = supplier.get();
				
				guard.writeLock().lock();
				locked=guard.writeLock();
				set(key,value);
				return value;
			}
			finally {
				localGuard.writeLock().unlock();
			}
		}
		finally 
		{
			if (locked!=null) locked.unlock();
		}
	}
			
	public void set (final Object key, final Value value)
	{
		try {
			guard.writeLock().lock();
			final LRUWrapper<Object, Value> w = cache.merge (key, new LRUWrapper<Object,Value>(key, value), (oldPayload, newPayload) -> {
				if (oldPayload!=null) {
					oldPayload.removeHint();
				}
				return newPayload;
			});
	
			manager.setLast (w);
		}
		finally {
			guard.writeLock().unlock();
		}
	}

	public Value get (final Object key) 
	{
		try {
			guard.readLock().lock();
			LRUWrapper<Object, Value> wrappedValue = cache.get(key);
			if (wrappedValue==null) return null;
			
			wrappedValue.removeHint();
			manager.setLast(wrappedValue);

			return wrappedValue.getPayload();
		}
		finally 
		{
			guard.readLock().unlock();
		}
	}
	
	public void remove (Object key)
	{
		try {
			guard.writeLock().lock();
			final var element = cache.remove(key);
			manager.removeLastIf(element);
			element.removeHint();
		}
		finally 
		{
			guard.readLock().unlock();
		}	
	}

	public int size ()
	{
		return cache.size();
	}
	
	public void cleanupLRU ()
	{	
		try {
			guard.writeLock().lock();
			final var last = manager.getLast();
			if (last!=null) remove(last.getKey());
		}
		finally 
		{
			guard.readLock().unlock();
		}
	}
}
