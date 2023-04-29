package com.github.ds67.jminicache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.function.Function;

/**
 * Provided cache interface. 
 * 
 * For usage simple create an builder and set the desired configuration.
 * 
 * Example:
 * <code>
 * {@code
 * final var cache = new MiniCacheBuilder<Integer, Integer>()
 *	 		.setEvictionPolicy(MiniCacheBuilder.EvictionPolicy.EVICTION_FIFO)
 *		    .setMaxSize(maxSize)
 *		    .build();
 * }
 * </code>
 * 
 * Item access
 * <ul>
 * <li>{@link #fetch(Object)}</li>
 * <li>{@link #get(Object, ValueSupplier)}</li>
 * <li>{@link #get(Object, ValueSupplier, long)}</li>
 * </ul>
 * 
 * item manipulation
 * <ul>
 * <li>{@link #set(Object, Object)}</li>
 * <li>{@link #set(Object, ValueWithExpiry)}</li>
 * <li>{@link #remove(Object)}</li>
 * <li>{@link #clear()}</li>
 * </ul>
 *  
 * Information:
 * <ul>
 * <li>{@link #isEmpty()}</li>
 * <li>{@link #size()}</li>
 * <li>{@link #contains(Object)}</li>
 * <li>{@link #entrySetWithExpiryDate()}</li>
 * <li>{@link #entrySet()}</li>
 * <li>{@link #values()}</li>
 * <li>{@link #keySet()}</li>
 * </ul>
 * 
 * Mass settings of cached items:
 * <ul>
 * <li>{@link #set(Set, long)}</li>
 * <li>{@link #set(Map, long)}</li>
 * <li>{@link #set(Map)}</li>
 * </ul>
 * 
 * Configuration:
 * <ul>
 * <li>{@link #setMaxSize(int)}</li>
 * <li>{@link #setRefreshMethod(Function)}</li>
 * <li>{@link #setValueFactory(Function)}</li>
 * <li>{@link #setValueWithExpiryFactory(Function)}</li>
 * </ul>
 * 
 * Configuration information:
 * <ul>
 * <li>{@link #getMaxSize()}</li>
 * <li>{@link #getValueWithExpiryFactory()}</li>
 * </ul>
 * 
 * @see com.github.ds67.jminicache.MiniCacheBuilder
 *  
 * @author Jens Ketterer
 *
 * @param <Key> Type of the access key of the cached items. A key should not be muted after usage in the cache. 
 *              It must provide a working {@link Object#equals(Object)} and {@link Object#hashCode()} implementation. 
 *             
 * @param <Value> Type of the cached item
 * 
 */
public interface MiniCache<Key, Value> extends Publisher<CacheChangeEvent<Key, Value>>{

	/**
	 *   Retrieves a cache value, if the key does not exists in the cache the provided supplier function is called to populate the cache
	 *   (with expiry date).
	 *      
	 *   {@link #get(Object, ValueSupplier)} calls with other keys will not be suspended will the value will be fetched.
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
	 *   
	 * @see #fetch(Object)
	 *   
	 * @param <E> Exception type the supplier function returns. Works also fine when the supplier does not return an exception.
	 *   
	 * @param key key by which the desired value can be found. The key can be any object which provides a well defined {@link #hashCode()}
	 *   and {@link #equals(Object)} method. 
	 *   
	 * @param supplier Function which will be called when the key is not found in the cache. The implementation will guarantee that the supplier method
	 *   will be called only once for a certain key regardless how many parallel requests are made and how long the value retrieval takes. Waiting 
	 *   {@link #get(Object, ValueSupplier)} for the same key will return the provides value upon availability.
	 *            
	 * @return Value which was retrieved from the cache. As a supplier is provided it is guranteed that a value is returned. However, when the supplier
	 *               provides a <code>null</code> this is cached and returned.
	 *                   
	 * @throws E Exception the supplier function throws. It is simply rethrown after cleanup of the get function.
	 */
	<E extends Throwable> Value get(Key key, ValueSupplier<ValueWithExpiry<Value>, E> supplier) throws E;

	/**
	 * Retrieves a cache value, if the key does not exists in the cache the provided supplier function is called to populate the cache
	 * (without expiry date).
	 * 
	 * Simple version of {@link #get(Object, ValueSupplier)}
	 * 
	 * @param <E> Exception which is thrown by the Suppier Method. Unfortenately only one exception type can be returned 
	 *            in a generic signature.	 
	 * 
	 * @param key key by which the desired value can be found. The key can be any object which provides a well defined {@link #hashCode()}
	 *   and {@link #equals(Object)} method. 
	 *   
	 * @param supplier Function which will be called when the key is not found in the cache. The implementation will guarantee that the supplier method
	 *   will be called only once for a certain key regardless how many parallel requests are made and how long the value retrieval takes. Waiting 
	 *   {@link #get(Object, ValueSupplier)} for the same key will return the provides value upon availability.
	
	 * @param expireDate timestamp in milliseconds when the entry will expire
	 *
	 * @return retrieved value from cache or newly generated value when not existed
	 * 
	 * @throws E Exception the supplier function throws. It is simply rethrown after cleanup of the get function.
	 * 
	 */
	<E extends Throwable> Value get(Key key, ValueSupplier<Value, E> supplier, long expireDate) throws E;

	/**
	 * Adds a value to the cache without setting a expiry date
	 * 
	 * No expiry date will be set, the lifetime will just be limited by the configured eviction policiy
	 * 
	 * @param key lookup key of the value
	 * @param value value to add
	 */
	void set(Key key, Value value);

	void set(Key key, ValueWithExpiry<Value> ve);

	/**
	 * Adds a value to the cache with a expiry date
	 * 
	 * @param key lookup key of the value
	 * @param value value to add
	 * @param expiryDate timestamp in milliseconds when the value will expire
	 */
	void set(Key key, Value value, long expiryDate);

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
	void set(Map<Key, Value> content, long expiryDate);

	void set(Set<Map.Entry<Key, Value>> entries);

	void set(Set<Map.Entry<Key, Value>> entries, long expiryDate);

	void set(Map<Key, ValueWithExpiry<Value>> content);

	/**
	 * Retrieves an existing value from the cache. 
	 * 
	 * If the key is not in the cache the value factory will be used to generate the value.
	 * 
	 * If no value factory is set the method behaves like the {@link #fetch(Object)} method. 
	 * 
	 * @see #setValueFactory(Function)
	 * @see #setValueWithExpiryFactory(Function)
	 * 
	 * @param key key for which the value should be retrieved
	 * @return The retrieved value or null when the key does not exists in the cache.
	 */
	Value get(Key key);

	/**
	 * Fetches a value from the cache without creating values, even when a valueFactory method is set
	 * Will return a <code>null</code> value when the requested key is not cached.
	 * 
	 * Use this method in favor to the get methods when the cache fill is done in batch inserts.
	 * 
	 * @see #get(Object)
	 * 
	 * @param key key for which the value should be retrieved
	 * @return Value item which is stored for the key or <code>null</code> when nothing is stored.
	 */
	Value fetch(Key key);

	/**
	 * Removed an object from the cache
	 * 
	 * @param key which will be removed from the cache
	 */
	void remove(Key key);

	/**
	 * Get the number of elements in the cache
	 * @return the number of keys in the cache 
	*/
	int size();

	/**
	 * Checks weather the cache is empty.
	 * 
	 * @return <code>true</code> when cache is empty, <code>false</code> otherwise
	 */
	boolean isEmpty();

	/**
	 * Checks if a key is contained in the cache
	 * @param key key to check for containment
	 * @return true when the key exists in the cache, false otherwise
	 */
	boolean contains(Key key);

	/**
	 * Sets the maximum cache entry size.
	 * 
	 * When the size is less than 1 it is considered as unbounded, regardless of the eviction policy. When the eviction policy 
	 * is {@link MiniCacheBuilder.EvictionPolicy#EVICTION_NONE} setting the maximum size has no effect
	 * 
	 * @param maxSize new maximum cache entry size.
	 * @return this object to provide a builder like interface
	 */
	MiniCache<Key, Value> setMaxSize(int maxSize);

	int getMaxSize();

	/**
	 * Sets a value factory function for the cache.
	 * 
	 * The value factory generates from a given key the corresponding value. It is called when the requested key is not found in the cache.
	 * The generated value is then stored in the cache and returned from the function. Set the value factory to null to delete an currently
	 * installed factory.
	 * 
	 * Be aware that the value factory function must be reentrant. That means that it might be called for different key in parallel. However
	 * it will not be called for the same key in parallel. 
	 * 
	 * @see #get(Object)
	 * @see #getValueWithExpiryFactory()
	 * 
	 * @param valueFactory supplier function which takes a key object and produced the cachable item of type Value
	 * @return this object to provide a builder like interface
	 */
	MiniCache<Key, Value> setValueWithExpiryFactory(Function<Key, ValueWithExpiry<Value>> valueFactory);

	MiniCache<Key, Value> setValueFactory(Function<Key, Value> valueFactory);

	/**
	 * Installes a method which will be called when cache entries are expired.
	 * This method is called with the expired key as argument and mus return a {@link ValueWithExpiry} object.
	 * The new object might also be unlimited valid.
	 * 
	 * Please note that the refresh method is executed in the context of the scheduler threads. If you have many items 
	 * waiting for refreshment you should consider installing an own multithreaded scheduler to handle all requests
	 * near real time.
	 * The existing item is replaced when a new value is available. Therefore, when refreshing is slow, the old value will be
	 * visible further than the expire date.  
	 * 
	 * @see MiniCacheBuilder#setSchedulerService(java.util.concurrent.ScheduledExecutorService)
	 * @see MiniCacheBuilder#getSchedulerService()
	 * 
	 * @param refreshMethod method to refresh a key or <code>null</code> to stop refreshing a remove keys again.
	 * @return the current object to allow method chaining
	 */
	MiniCache<Key, Value> setRefreshMethod(Function<Key, ValueWithExpiry<Value>> refreshMethod);

	/**
	 * Gets the currently installed valueFactory. When you installed a value factory without an ValueWithExpiry return value an
	 * automatic wrapping of you function was done and you need to unwrap it:
	 * 
	 * <code>{@code
	 *    Function<Key,Value> yourFunction = ValueWithExpiry.unwrap(cache.getValueWithExpiryFactory());
	 * }
	 * </code>
	 * 
	 * @see #setValueFactory(Function)
	 * @see #setValueWithExpiryFactory(Function)
	 * @see ValueWithExpiry#unwrap(Function)
	 *  
	 * @return the installed value factory or null if non is installed
	 */
	Function<Key, ValueWithExpiry<Value>> getValueWithExpiryFactory();

	/**
	 * Clears the content of the cache.
	 */
	void clear();

	/**
	 * Retrieved the current set of stored key of the cache
	 * 
	 * @return set of the currently stored keys
	 */
	Set<Key> keySet();

	/**
	 * Allows to use a write lock transaction around a function.
	 * 
	 * @param f function which is executed in exclusive access mode
	 */
	void writeLocked(Runnable f);

	/**
	 * Allows to use a read lock transaction around a function.
	 * 
	 * @param f function which is executed in exclusive access mode
	 */
	void readLocked(Runnable f);

	Collection<Value> values();

	Collection<ValueWithExpiry<Value>> valuesWithExpiryDate();

	/**
	 * Retrieves the key/value pair of all stored entries in the cache.  
	 * 
	 * @return entrySet of the cache
	 */
	Set<Map.Entry<Key, Value>> entrySet();

	/**
	 * Retrieves the key/value pair of all stored entries in the cache. The values are extended with the current known
	 * expiry times.
	 * If no expiry manager is installed all expiry times are set to 0.
	 * 
	 * Please note that this methods needs O(n*log(n)) processing time with n cached elements when a expire manager is in place
	 * in contrast to the @see #entrySet() method.  
	 * 
	 * @see ValueWithExpiry
	 * 
	 * @return entrySet of the cache when the values are extended with the known expire dates
	 */
	Set<Map.Entry<Key, ValueWithExpiry<Value>>> entrySetWithExpiryDate();

	/**
	 * Offers an subscription interface to monitor changes to the cache. New inserts, updates and removals are published asynchronously using the 
	 * #java.util.concurrent.Flow mechanisms.
	 * 
	 * You may add as many subscribers as you like. 
	 * 
	 */
	void subscribe(Subscriber<? super CacheChangeEvent<Key, Value>> subscriber);

	void setCalculateStatistics (boolean v);
	boolean isCalcuatingStatistics ();
	Statistics getStatistics ();

}