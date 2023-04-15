package com.github.ds67.jminicache;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

import com.github.ds67.jminicache.impl.MiniCacheImpl;

public class MiniCacheBuilder<Key, Value> {

	/**
	 * Define the eviction polciies of the cache.
	 * 
	 * @author Jens Ketterer
	 *
	 */
	public static enum EvictionPolicy
	{
		/**
		 * Eviction policy: Removes the least recently used entry when necessary. Beware that this policy needs a single thread access even for read cache requests
		 * as the LRU status must be recorded. This may slow done the cache access.
		 * 
		 * Choose this eviction policy when the entries to not age and the probability of a new access to such an item does not increase over time. That you have a risk
		 * of trashing the cache where you always remove the entries with the highest probability for the next access.  
		 */
		EVICTION_LRU,

		/**
		 * Eviction policy: Removes the oldest entry by insertion time.
		 * 
		 * Choose this policy when entries get outdated and the chance increases that a new value was created for a key.  
		 * 
		 */
		EVICTION_FIFO,
		
		/**
		 * Eviction policy: No entries will be removed in background. Use this policy when you either have a limit set of entries which do never expire.
		 * Usage of the cache is then similar to a simple {@link Map}. However, reads to the cache are done in parallel and will only
		 * queue when write requests occur.
		 * 
		 * Or use it with
		 */
		EVICTION_NONE
	}
	
	/**
	 * Define the underlying key value storage of the 
	 * 
	 * @author Jens Ketterer
	 *
	 */
	public static enum StoragePolicy
	{
		/**
		 * Uses a hash base key value store (based on a {@link HashMap}). A hash map uses more memory than a tree map but has usually faster access.
		 * This is the default storage. 
		 */
		HASH_MAP_STORAGE,
		
		/**
		 * Builds a key value store based on a {@link TreeMap}. A TreeMap has a guaranteed access time of O(n) time and uses less memory than the
		 * hash based tree storage. Therefore, use a tree map when memory consumption is an issue (many keys and small cached values).
		 */
		TREE_MAP_STORAGE
		
	}
	
	public MiniCacheBuilder() {	
	}
	
	private EvictionPolicy evictionPolicy=EvictionPolicy.EVICTION_NONE;
	private StoragePolicy storagePolicy=StoragePolicy.HASH_MAP_STORAGE;
	private boolean useExpiry = false;
	private boolean useWeakKeys = false;
	private Comparator<Key> keyComparator = null;
	private int maxSize = -1;
	private Function<Key, ValueWithExpiry<Value>> valueFactory = null;
	private boolean statistics = false;
	
	public MiniCacheBuilder<Key,Value> setEvictionPolicy (EvictionPolicy evictionPolicy)
	{
		this.evictionPolicy=evictionPolicy;
		return this;
	}
	
	public MiniCacheBuilder<Key,Value> setMaxSize (int maxSize)
	{
		if (maxSize<0) this.maxSize=-1;
		else this.maxSize=maxSize;
		return this;
	}
	
	public MiniCacheBuilder<Key,Value> setDefaultExpiryTime (long timespan)
	{
		this.useExpiry = true;
		return this;
	}
	
	/**
	 * Sets the general storage policy for the cache.
	 * 
	 * @see StoragePolicy
	 * 
	 * @param policy policy to set
     * @return MiniCacheBuilder instance to allow chaining	
     */
	public MiniCacheBuilder<Key,Value> setStoragePolicy (StoragePolicy policy)
	{
		this.storagePolicy = policy;
		return this;
	}

	/**
	 * Defines if the cache calculates usage statistics
	 * 
	 * @see Statistics
	 * @see MiniCache#setCalculateStatistics(boolean)
	 * 
	 * @param value when <code>true</code> statistics are calculated
	 * @return MiniCacheBuilder instance to allow chaining	
	 */
	public MiniCacheBuilder<Key,Value> setCalculateStatistics (boolean value)
	{
		this.statistics=value;
		return this;
	}
	
	/**
	 * Sets a key comparator to use for a tree storage.
	 * This implies setting the {@link StoragePolicy#TREE_MAP_STORAGE} policy
	 * 
	 * @see #setStoragePolicy(StoragePolicy)
	 * 
	 * @param comparator
     * @return MiniCacheBuilder instance to allow chaining	
	 */
	public MiniCacheBuilder<Key,Value> setKeyComparator (Comparator<Key> comparator)
	{
		this.keyComparator = comparator;
		setStoragePolicy(StoragePolicy.TREE_MAP_STORAGE);
		return this;
	}
	
	/**
	 * Set using {@link WeakReference} keys
	 * 
     * @return MiniCacheBuilder instance to allow chaining	
	 */
	public MiniCacheBuilder<Key,Value> setUseWeakKeys (boolean useWeakKeys)
	{
		this.useWeakKeys=useWeakKeys;
		return this;
	}
	
	/**
	 * 
	 * When values have an expire date these are recognized and values are removed from the cache when they expire. 
	 * A cached item need not to have an expire date. Then it remains valid forever.
	 * 
	 * Using this additional policy will add O(n) complexity to the {@link MiniCache#set(Object, Object)} 
	 * and {@link MiniCache#remove(Object)} and similar methods. Furthermore it requires a installed scheduler
	 * which will schedule the expire operations.
	 * 
	 * When you install a{@link  MiniCache#setRefreshMethod(java.util.function.Function)} method the cached items
	 * are not removed but instead refreshed.
	 * 
	 * @see #setSchedulerService(java.util.concurrent.ScheduledExecutorService) 
	 * @see #getSchedulerService()
	 * @see #hasSchedulerService()
	 * 
	 * If this policy is not set upon creation the expire date is not used. Adding such a date to a set method has no effect.
	 * @param useExpiry
	 * @return
	 */
	public MiniCacheBuilder<Key,Value> setUseExpiry (boolean useExpiry)
	{
		this.useExpiry = useExpiry;
		return this;
	}
	
	/**
	 * Creates a new mini cache instance based on the parameters set in the builder. You may call {@link #build()} several time and it will always return a
	 * new cache instance.
	 * 
	 * @return {@link MiniCache} instance
	 */
	public MiniCache<Key, Value> build ()
	{
		var cache = new MiniCacheImpl<Key, Value>(maxSize, evictionPolicy, storagePolicy, useWeakKeys, useExpiry, keyComparator);
		if (valueFactory!=null) {
			cache.setValueWithExpiryFactory(valueFactory);
		}
		cache.setCalculateStatistics(statistics);
		
		return cache;
	}
	
	/**
	 * Uses a value factory when a value must be newly inserted to the cache.
	 * 
	 * @see MiniCache#setValueFactory(Function)
	 * 
	 * @param valueFactory Value factory to install
	 * @return MiniCacheBuilder instance to allow chaining
	 */
	public MiniCacheBuilder<Key,Value> setValueFactory(Function<Key, Value> valueFactory)
	{
		this.valueFactory=ValueWithExpiry.wrap(valueFactory);
		return this;
	}
	
	/**
	 * 
	 * @see MiniCache#setValueWithExpiryFactory(Function)
	 * 
	 * @param valueFactory Value factory to install
	 * @return MiniCacheBuilder instance to allow chaining
	 */
	public MiniCacheBuilder<Key,Value> setValueWithExpiryFactory(Function<Key, ValueWithExpiry<Value>> valueFactory)
	{
		this.valueFactory=valueFactory;
		return this;
	}

	/*
	 * 
	 * Static scheduler service
	 * 
	 */
	
	/**
	 * Create a single scheduler thread for all cache instances. By using an own thread factory it is possible so set a descriptive name
	 * and the thread as a daemon thread to allow a graceful application shutdown
	 */
	private static ScheduledExecutorService scheduler = null;
	
	/**
	 * Returns true when a scheduler instance was already created.
	 * If not, a default scheduled service is installed by calling {@link #getSchedulerService()}. 
	 * This is usually done internally. Thus, when you'd like to know if the default or yours customer
	 * scheduler is used, have a logic like
	 * 
	 * <pre>{@code
	 *    MiniCache <?,?> cache;
	 *    if (cache.hasSchedulerService && cache.getSchedulerService()==myScheduler) {
	 *       ...
	 *    }
	 * }</pre>
	 * 
	 * @see #getSchedulerService()
	 * @see #setSchedulerService(ScheduledExecutorService)
	 * 
	 * @return <code>true</code> when a scheduler is already installed, <code>false</code> otherwise
	 */
	public static boolean hasSchedulerService ()
	{
		return scheduler!=null;
	}
	
	/**
	 * Gets the used scheduler service. If none is is set yet a new default scheduler is created.
	 * The default scheduler uses a single thread named "Minicache expiry scheduler".
	 * 
	 * @see #setSchedulerService(ScheduledExecutorService)
	 * @see #hasSchedulerService()
	 * 
	 * @return the used scheduler service. If none was there a new thread is created
	 */
	public static ScheduledExecutorService getSchedulerService ()
	{
		if (scheduler==null) {
			scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
				       final Thread t = Executors.defaultThreadFactory().newThread(r);
		               t.setDaemon(true);
		               t.setName("Minicache expiry scheduler");
		               return t;
				}
			});
		}
		return scheduler;
	}
	
	/**
	 * Sets a new global scheduler service for the caches. This scheduler is used to schedule expire dates and remove 
	 * cached entries in time. If no customer scheduler is the cache instances will use an own scheduler powered by a 
	 * single thread. 
	 * 
	 * If there was already a scheduler service installed already created {@link MiniCache} instances keep using 
	 * their assigned scheduler. It will not be exchanged with the new global scheduler.
	 * 
	 * @see #getSchedulerService()
	 * @see #hasSchedulerService()
	 * 
	 * @param scheduler scheduler which served the expire actions
	 */
	public static void setSchedulerService (final ScheduledExecutorService scheduler)
	{
		MiniCacheImpl.scheduler = scheduler;
	}
}

