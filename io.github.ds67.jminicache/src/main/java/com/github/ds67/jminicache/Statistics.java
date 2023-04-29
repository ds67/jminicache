package com.github.ds67.jminicache;

/**
 * Allows access to cache statistics. The returned object will be continuously updates as long as statistic collection is turned on.
 * When its turned of {@link #isActive()} will return false and the statistics are not longer updated. When calulation is turned on again a 
 * new object will be created.
 * 
 * @author Jens Ketterer
 *
 */
public interface Statistics {
	
	/**
	 * Reset all statistics.
	 * The reset of the different statistics is not atomic, that means that other threads can read the statistics in parallel and some my already be zero others not.
	 * Usually this is not an issue, but if it is you must provide you own synchronization. 
	 * 
	 */
	void reset(); 
	
	/**
	 * Check if statistics are still collected
	 * 
	 * @return <code>true</code> when statistics are still collected
	 */
	boolean isActive ();
	
	long getFetchCounter();
	
	/**
	 * Counter of how many get accesses are done
	 * @return get accesses
	 */
	long getGetCounter();
	
	/**
	 * Counter how many udpates are done. Updates include refreshes.
	 * @return
	 */
	long getUpdateCounter();
	
	/**
	 * Counter how often a cache miss occured 
	 * @return cache misses
	 */
	long getMissesCounter();
	
	/**
	 * Counter how often a cache clear was executed
	 * @return cache clears
	 */
	long getClearCounter();
	
	/**
	 * Counter how often a refresh occured (that is a value expired and was automatically reloaded)
	 * @return refresh counter
	 */
	long getRefreshCounter();
	
	/**
	 * Counter how many key whre removed from the cache. This includes all shrinks 
	 * @return removed key counter
	 */
	long getRemovalCounter();
	
	/**
	 * Counter how many keys where removed from the cache due to eviction
	 * @return shrink counter
	 */
	long getShrinkCounter();
	
	/**
	 * Returns how often parallel supplied values for the same key was detected.
	 * @return key supply collision counter
	 */
	long getCollisionCounter();
	
	/**
	 * Returns how often a value expired because it was outdated
	 * @return expired element counter
	 */
	long getExpiredCounter();
	
	/**
	 * Retrieves the cache object which statistics are collected for
	 * @return cache object
	 */
	MiniCache<?,?> getCache ();
}
