package ds67.jminicache;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

import ds67.jminicache.CachePolicy.Category;
import ds67.jminicache.impl.guard.GuardIF;
import ds67.jminicache.impl.storage.ExpiryManager;
import ds67.jminicache.impl.storage.StorageManagerIF;

/**
 * This is the provided cache class. 
 * 
 * For usage simple create an instance with the desired cache policies. The cache will configure itself with these policies.
 * 
 * @see ds67.jminicache.CachePolicy
 *  
 * @author Jens Ketterer
 *
 * @param <Value>
 */
public class MiniCache<Key, Value> implements Publisher<CacheChangeEvent<Key, Value>>
{	
	/**
	 * Wrapper class to allow supply functions to return the 
	 * 
	 * @author jens
	 *
	 * @param <Value>
	 */
	static public class ValueWithExpiry<Value>
	{
		final private Value value;
		final private long expiry;
		
		ValueWithExpiry (Value value)
		{
			this.value=value;
			this.expiry=0;
		}
		
		ValueWithExpiry (Value value, long expiry)
		{
			this.value=value;
			this.expiry=expiry;
		}

		public Value getValue() {
			return value;
		}

		public long getExpiry() {
			return expiry;
		}
	}
	
	private ExpiryManager<Key> expiryManager = null;
	
	public MiniCache (final CachePolicy... policies) {
		CachePolicy evictionPolicy = CachePolicy.EVICTION_NONE;
		for (final var policy: policies) {
			if (policy.getCategory()==Category.EVICTION_POLICY) {
				evictionPolicy=policy;
			}
			else if (policy.compareTo(CachePolicy.ENABLE_VALUE_EXPIRY)==0) {
				expiryManager = new ExpiryManager<>(this::remove, getSchedulerService());
			}
		}
		
		manager = ManagerFactory.createCacheManager(false, evictionPolicy);
		guard = manager.getGuard();
	}

	private Function<Key, ValueWithExpiry<Value>> valueWithExpiryFactory = null;
	private Function<Key, Value> valueFactory = null;
	
	private final GuardIF guard;
	
	private final Map<Key,ReadWriteLock> creationGuards = new HashMap<>();
	
	private StorageManagerIF<Key, Value> manager;

	
	/**
	 *   Retrieves a cache value, if the key does not exists in the cache the provided supplier function is called to populate the cache
	 *   (with expiry date).
	 *   
	 *   @param key key by which the desired value can be found. The key can be any object which provides a well defined {@link #hashCode()}
	 *   and {@link #equals(Object)} method. 
	 *   
	 *   @param supplier Function which will be called when the key is not found in the cache. The implementation will guarantee that the supplier method
	 *   will be called only once for a certain key regardless how many parallel requests are made and how long the value retrieval takes. Waiting 
	 *   {@link #get(Object, Supplier)} for the same key will return the provides value upon availability.
	 *   
	 *   {@link #get(Object, Supplier)} calls with other keys will not be suspended will the value will be fetched.
	 *   
	 *   When the supplier methods throws an exception this exception will be re thrown by the method. No insertion in the cache will take place. 
	 *   
	 *   This is the preferred method for interacting with the cache when no manual controlling of the cache content is necessary. You hust need a single method
	 *   which will be used for reading cache content and fill in the cache data when necessary.
	 *   
	 *   <pre>{@code
	 *   static int fac (int n)
	 *   {
	 *   	int result = 1;
	 *      for (int i=0;i<;n;i++) result*=n;
	 *      return result;
	 *   }
	 *   
	 *   static void main (String argv[])
	 *   {
	 *       var cache = new MiniCache<Integer,Integer>();
	 *       
	 *       for (int i=0;i<;1000;i++) {
	 *          System.out.println (cache.get (i%20, () -> fac(i%20)));
	 *       };
	 *   }
	 *   }</pre>
	 */
	public Value get (final Key key, Supplier<ValueWithExpiry<Value>> supplier) throws Exception
	{
		try {
			guard.lockRead();
			final var available_value = unsynchronized_fetch(key);
			if (available_value!=null) {
				return available_value;
			}
		
			// Didn't find the value but also have no supplier
			if (available_value==null && supplier==null) {
				return null;
			}
			
			guard.promoteLock();
		
			var localGuard = creationGuards.get(key);
			if (localGuard!=null) {
				// somebody else called supplier, simple wait until finished
				try {
					localGuard.readLock().lock();
					return get(key, supplier);
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
			
			guard.unlock ();
		
			try {
				final var result = supplier.get();
				
				guard.lockWrite();
				unsynchronized_set(key,result.getValue(),result.getExpiry());
				return result.getValue();
			}
			finally {
				localGuard.writeLock().unlock();
				creationGuards.remove(key);
			}
		}
		finally 
		{
			guard.unlock();
		}
	}
	
	/**
	 * Retrieves a cache value, if the key does not exists in the cache the provided supplier function is called to populate the cache
	 * (without expiry date).
	 * 
	 * Simple version of {@link #get(Object, Supplier, boolean)}
	 * 
	 * @param key
	 * @param supplier
	 * @param expireDate timestamp when the entry will expire
     *
	 * @return retrieved value from cache
	 * @throws Exception
	 */
	public Value get (final Key key, Supplier<Value> supplier, long expireDate) throws Exception
	{
		return this.get(key, () -> {
			return new ValueWithExpiry<Value>(supplier.get(), expireDate);
		});
	}
		
	protected void unsynchronized_set (final Key key, final Value value, final long expiry)
	{
		final var w = manager.createWrapper(key, value);

		if (expiryManager!=null && expiry>0) {			
			expiryManager.add(key, expiry);
		}
		
		manager.onBeforeWrite(w);	
		final var previousValue = manager.put(key, w);
		
				
		if (publisher!=null) {
			final Value oldValue = previousValue==null?null:previousValue.getPayload();
			publisher.submit (new CacheChangeEvent<Key, Value>(key, oldValue, value));		
		}
		unsynchronized_shrink();
	}

	/**
	 * Adds a value to the cache without setting a expiry date
	 * 
	 * No expiry date will be set, the lifetime will just be limited by the configured eviction policiy
	 * 
	 * @param key lookup key of the value
	 * @param value value to add
	 */
	public void set (final Key key, final Value value)	
	{
		guard.writeLocked(() -> unsynchronized_set(key,value,0));
	}
	
	/**
	 * Adds a value to the cache with a expiry date
	 * 
	 * @param key lookup key of the value
	 * @param value value to add
	 * @param expiryDate timestamp in milliseconds when the value will expire
	 */
	public void set (final Key key, final Value value, long expiryDate)	
	{
		guard.writeLocked(() -> unsynchronized_set(key,value,expiryDate));
	}

	/**
	 * Add a list of entries to the cache at once. 
	 * 
	 * In comparison to the one element {@link #set(Object, Object)} function this function will lock the cache for writing just once to insert all elements 
	 * and is thus faster. However, when other requests start piling up the insertation is interrupted and other requests will be executed before continuing
	 * to insert. Existing keys in the cache will be overwritten. Other content in the cache will stay untouched.
	 * 
	 * @param content Map of key and value elements to be inserted in the map. The map itself must not be altered will inserting into the cache.
	 * @param expiryDate timestamp (in milliseconds like System.getCurrentMillis()) when the entries will expire and be removed from the cache 
	 * 
	 * The map elements are not deep copied into the cache
	 */
	public void set (final Map<Key, Value> content, long expiryDate)
	{
		final var entries = content.entrySet();
		
		guard.writeLocked(() -> {
			int insertedInARow = 0;
			for (final var entry: entries) {
				unsynchronized_set(entry.getKey(), entry.getValue(), expiryDate);
				if (++insertedInARow > 20) {
					insertedInARow=0;
					// yield when readers are waiting
					guard.yield();
				}
			}			
		});
	}	

	/**
	 * Retrieves an existing value from the cache. 
	 * 
	 * If the key is not in the cache the value factory will be used to generate the value.
	 * 
	 * If no value factory is set the method behaves like the {@link #fetch(Object)} method. 
	 * 
	 * @see #setValueFactory(Function)
	 * 
	 * @param key key for which the value should be retrieved
	 * @return The retrieved value or null when the key does not exists in the cache.
	 * @throws Exception
	 */
	public Value get (final Key key) throws Exception
	{	
		if (valueWithExpiryFactory!=null) {
			return get(key, () -> valueWithExpiryFactory.apply(key));
		}	
		if (valueFactory!=null) {
			return get(key, () -> new ValueWithExpiry<Value>(valueFactory.apply(key),0));
		}
		return fetch(key);
	}
	
	private Value unsynchronized_fetch (final Key key)
	{
		final var wrappedValue = manager.get(key);
		if (wrappedValue==null) return null;
		manager.onRead(wrappedValue);
		return wrappedValue.getPayload();		
	}
	
	/**
	 * Fetches a value from the cache without creating values, even when a valueFactory method is set
	 * 
	 * Use this method in favor to the get methods when the cache fill is done in batch inserts.
	 * 
	 * @param key
	 * @return
	 */
	public Value fetch (final Key key) 
	{
		return guard.readLocked(() -> this.unsynchronized_fetch(key));	
	}
	
	protected Value unsynchronized_remove (Key key)
	{
		final var element = manager.remove(key);
		manager.onDeletion(element);
		if (expiryManager!=null) expiryManager.remove(key);
		if (publisher!=null) publisher.submit (new CacheChangeEvent<Key, Value>(key, element.getPayload(), null));
		return element.getPayload();		
	}
	
	/**
	 * Removed an object from the cache
	 * 
	 * @param key which will be removed from the cache
	 */
	public void remove (Key key)
	{		
		guard.writeLocked(() -> unsynchronized_remove(key));		
	}

	/**
	 * Get the number of elements in the cache
	 * @return the number of keys in the cache 
	*/
	public int size ()
	{
		return guard.readLocked(manager::cachesize);
	}

	/**
	 * Checks if a key is contained in the cache
	 * @param key key to check for containment
	 * @return true when the key exists in the cache, false otherwise
	 */
	public boolean contains (Key key)
	{
		return guard.readLocked(() -> manager.contains (key));
	}
	
	private int maxSize = -1;
	
	/**
	 * Sets the maximum cache entry size.
	 * 
	 * When the size is less than 1 it is considered as unbounded, regardless of the eviction policy. When the eviction policy 
	 * is {@link CachePolicy#EVICTION_NONE} setting the maximum size has no effect
	 * 
	 * @param maxSize new maximum cache entry size.
	 * @return this object to provide a builder like interface
	 */
	public MiniCache<Key, Value> setMaxSize (int maxSize)
	{
		this.maxSize=maxSize;
		shrink();
		return this;
	}

	private void unsynchronized_shrink ()
	{
		if (maxSize<1) return;		
		while (manager.cachesize()>maxSize) {
			final var last = manager.getForDeletion();
			if (last!=null) unsynchronized_remove(last.getKey());
			else break;
		}	
	}
	
	/**
	 * Shrinks the cache size to the maximum allowed size. 
	 * 
	 * Used the currently installed eviction policy to decide which entries will be removed. If no eviction policy is in place 
	 * the method does nothing. 
	 * The cache is write locked within this operation
	 * 
	 * @see #setMaxSize(int)
	 */
	protected void shrink ()
	{	
		guard.writeLocked(this::unsynchronized_shrink);
	}
	
	/**
	 * Sets a value factory function for the cache.
	 * 
	 * The value factory generates from a given key the corresponding value. It is called when the requested key is not found in the cache.
	 * The generated value is then stored in the cache and returned from the function. Set teh value factory to null to delete an currently
	 * installed factory.
	 * 
	 * Be aware that the value factory function must be reentrant. That means that it might be called for different key in parallel. However
	 * it will not be called for the same key in parallel. 
	 * 
	 * @see #get(Object)
	 * @see #getValueFactory()
	 * 
	 * @param valueFactory
	 * @return this object to provide a builder like interface
	 */
	public MiniCache<Key, Value> setValueWithExpiryFactory (final Function<Key,ValueWithExpiry<Value>> valueFactory)
	{
		this.valueWithExpiryFactory=valueFactory;
		this.valueFactory=null;
		return this;
	}
	
	public MiniCache<Key, Value> setValueFactory (final Function<Key,Value> valueFactory)
	{
		this.valueFactory=valueFactory;
		valueWithExpiryFactory=null;
		return this;
	}
	
	public MiniCache<Key, Value> setRefreshMethod (final Function<Key,ValueWithExpiry<Value>> refreshMethod)
	{
		if (refreshMethod==null) expiryManager.setDeletionTrigger(this::remove);
		else expiryManager.setDeletionTrigger((key) -> {
			final var newValue = refreshMethod.apply(key);
			this.set(key, newValue.getValue(), newValue.getExpiry());
		});
		return this;
	}
	
	/**
	 * Gets the currently installed valueFactory
	 *  
	 * @return the installed value factory or null if non is installed
	 */
	public Function<Key,Value> getValueFactory ()
	{
		return this.valueFactory;
	}
	
	/**
	 * Gets the currently installed valueFactory
	 *  
	 * @return the installed value factory or null if non is installed
	 */
	public Function<Key,ValueWithExpiry<Value>> getValueWithExpiryFactory ()
	{
		return this.valueWithExpiryFactory;
	}
	
	/**
	 * Clears the content of the cache.
	 */
	public void clear ()
	{
		guard.writeLocked(() -> {
			if (publisher!=null) publisher.submit(new CacheChangeEvent<Key, Value>(null, null, null));
			manager.clear();
		});
	}
	
	/**
	 * Retrieved the current set of stored key of the cache
	 * 
	 * @return set of the currently stored keys
	 */
	public Set<Key> keySet ()
	{
		return guard.readLocked(manager::keySet);
	}
	
	/**
	 * Allows to use a write lock transaction around a function.
	 * 
	 * @param f function which is executed in exclusive access mode
	 */
	public void writeLocked (Runnable f)
	{
		guard.writeLocked(f);
	}
	
	/**
	 * Allows to use a read lock transaction around a function.
	 * 
	 * @param f function which is executed in exclusive access mode
	 */
	public void readLocked (Runnable f)
	{
		guard.readLocked(f);
	}
	
	/* **********************************************************************************************************************************
	 * 
	 * Specializations for expiry duration, so that different timespans can be given
	 * 
	 ************************************************************************************************************************************/
	
	/**
	 * Adds a value to the cache with lifetime
	 * 
	 * @param key lookup key of the value
	 * @param value value to add
	 * @param delay delay how long the value remains valid
	 * @param TimeUnit timeunit of the delay
	 */
	public void set (final Key key, final Value value, long delay, TimeUnit unit)	
	{
		this.set(key,value,System.currentTimeMillis()+unit.toMillis(delay));
	}

	/**
	 * Adds a value to the cache with a expiry date
	 * 
	 * @param key lookup key of the value
	 * @param value value to add
	 * @param expiryDate Date when the value will expire
	 */
	public void set (final Key key, final Value value, LocalDateTime expiryDate)	
	{
		this.set(key,value,expiryDate==null?0:expiryDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
	}
	
	public void set (final Map<Key, ValueWithExpiry<Value>> content)
	{
		final var entries = content.entrySet();
		
		guard.writeLocked(() -> {
			int insertedInARow = 0;
			for (final var entry: entries) {
				unsynchronized_set(entry.getKey(), entry.getValue().getValue(), entry.getValue().getExpiry());
				if (++insertedInARow > 20) {
					insertedInARow=0;
					// yield when readers are waiting
					guard.yield();
				}
			}			
		});
	}
	
	public Collection<Value> values ()
	{
		return guard.readLocked(() -> {
			final var values = new ArrayList<Value>(this.size()); 
			for (var entry: manager.values()) values.add(entry.getPayload());
			return values;
		});
	}
	
	public Collection<ValueWithExpiry<Value>> valuesWithExpiryDate ()
	{
		// Define a lambda function to retrieve the expire time. If no expire manager exists its always 0, otherwise ask the expiryManager
		final Function<Key, Long> getExpireTime = (expiryManager==null)?(key) -> 0L:(key) -> expiryManager.getExpiryTime(key);
		
		return guard.readLocked(() -> {
			final var values = new ArrayList<ValueWithExpiry<Value>>(this.size()); 
			for (var entry: manager.values()) {
				values.add(new ValueWithExpiry<Value>(entry.getPayload(), getExpireTime.apply(entry.getKey())));
			}
			return values;
		});
	}
	
	/**
	 * Create a single scheduler thread for all cache instances. By using an own thread factory it is possible so set a descriptive name
	 * and the thread as a daemon thread to allow a graceful application shutdown
	 */
	private static ScheduledExecutorService scheduler = null;
	
	public static ScheduledExecutorService getSchedulerService ()
	{
		if (scheduler==null) {
			scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
				       final Thread t = Executors.defaultThreadFactory().newThread(r);
		               t.setDaemon(true);
		               t.setName("Minicache Expiryscheduler");
		               return t;
				}
			});
		}
		return scheduler;
	}
	
	public static void setSchedulerService (final ScheduledExecutorService scheduler)
	{
		MiniCache.scheduler = scheduler;
	}

	/* **********************************************************************************************************************************
	 * 
	 * Subscription interface
	 * 
	 ************************************************************************************************************************************/
		
	private SubmissionPublisher<CacheChangeEvent<Key, Value>> publisher = null;
	
	/**
	 * Offers an subscription interface to monitor changes to the cache. New inserts, updates and removals are published asynchronously using the 
	 * #java.util.concurrent.Flow mechanisms.
	 * 
	 * You may add as many subscribers as you like. 
	 * 
	 */
	@Override
	public synchronized void subscribe(final Subscriber<? super CacheChangeEvent<Key, Value>> subscriber) {
		if (publisher==null) {
			publisher = new SubmissionPublisher<>();
		}
		
		publisher.subscribe(subscriber);
	}
}
