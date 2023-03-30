package ds67.jminicache.impl.guard;

import java.util.function.Supplier;

/**
 * Interface to define a synchronization guard.
 * 
 * Provides functions to lock for read usage (multiple read request in parallel) or write usage ( a single write at a time). 
 * However, depending on the used eviction policy a read request to the cache will result in a write lock as the background datastructes require
 * reorganization upon every request
 * 
 * Therefore different implementations are provided.
 * 
 * @author Jens Ketterer
 *
 */
public interface GuardIF {
	
	/**
	 * Locks the guard for read purposes. Waits until a read lock can be aquired.
	 */
	public void lockRead ();
	
	/**
	 * Locks the guard for write purposes. Waits until a write lock can be aquired.
	 */
	public void lockWrite ();
	
	public void unlockRead ();
	
	public void unlockWrite ();
	
	/**
	 * Promoted an existing read lock to an write lock. Might wait until other readers unlock. 
	 */
	public void promoteLock();
	
	/**
	 * Can be called when there is a long lasting lock. When other threads wait for lock aquiring these threads will be
	 * preferred executed. After these executions the current write lock is reestablished.
	 * 
	 * Must not be implemented by every guard
	 */
	public void yield();
	
	/**
	 * Executes a supplied function in a read locked context. Ensures that the lock is released after the function is called even when
	 * exceptions occur.
	 * 
	 * @param <R> Return type of the supplied function
	 * @param f function which is called in a read lock context
	 * @return the result of the supplier function f
	 */
	default public void readLocked (Runnable f)
	{
		try {
			lockRead();
			f.run();
		}
		finally {
			unlockRead();
		}
	}
	
	/**
	 * Executes a supplied function in a read locked context. Ensures that the lock is released after the function is called even when
	 * exceptions occur.
	 * 
	 * @param <R> Return type of the supplied function
	 * @param f function which is called in a read lock context
	 * @return the result of the supplier function f
	 */
	default public <R> R readLocked (Supplier<R> f)
	{
		try {
			lockRead();
			return f.get();
		}
		finally {
			unlockRead();
		}
	}
	
	/**
	 * Executes a supplied function in a write locked context. Ensures that the lock is released after the function is called even when
	 * exceptions occur.
	 * 
	 * @param <R> Return type of the supplied function
	 * @param f function which is called in a write lock context
	 * @return the result of the supplier function f
	 */
	default public <R> R writeLocked (Supplier<R> f)
	{
		try {
			lockWrite();
			return f.get();
		}
		finally {
			unlockWrite();
		}
	}
	
	/**
	 * Executes a supplied function in a read locked context. Ensures that the lock is released after the function is called even when
	 * exceptions occur.
	 * 
	 * @param f procedure which is called in a write lock context 
	 */
	default public void writeLocked (Runnable f)
	{
		try {
			lockWrite();
			f.run();
		}
		finally {
			unlockWrite();
		}
	}
}
