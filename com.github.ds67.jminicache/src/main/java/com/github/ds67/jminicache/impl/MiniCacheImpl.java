package com.github.ds67.jminicache.impl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import com.github.ds67.jminicache.CacheChangeEvent;
import com.github.ds67.jminicache.MiniCache;
import com.github.ds67.jminicache.MiniCacheBuilder;
import com.github.ds67.jminicache.ValueSupplier;
import com.github.ds67.jminicache.ValueWithExpiry;
import com.github.ds67.jminicache.impl.guard.GuardIF;
import com.github.ds67.jminicache.impl.guard.LocalGuard;
import com.github.ds67.jminicache.impl.storage.StorageManagerIF;
import com.github.ds67.jminicache.plugin.AsynchronousSubscriberPlugin;
import com.github.ds67.jminicache.plugin.Plugin;
import com.github.ds67.jminicache.plugin.PluginManager;
import com.github.ds67.jminicache.plugin.StatisticsPlugin;

public class MiniCacheImpl<Key, Value> implements MiniCache<Key, Value>
{			
	public MiniCacheImpl (int maxSize, 
			              MiniCacheBuilder.EvictionPolicy evictionPolicy,
			              MiniCacheBuilder.StoragePolicy storagePolicy,
			              boolean useWeakKey,
			              boolean useExpiry,
			              Comparator<Key> keyComparator)
	{
		manager = ManagerFactory.createCacheManager(evictionPolicy, storagePolicy, keyComparator, useWeakKey);
		guard = manager.getGuard();		
		setMaxSize(maxSize);
		if (useExpiry) {
			expiryManager = new ExpiryManager<Key>(this::remove, MiniCacheBuilder.getSchedulerService());
		}
	}

	private StorageManagerIF<Key, Value, ?> manager;
	private Function<Key, ValueWithExpiry<Value>> valueWithExpiryFactory = null;
	private ExpiryManager<Key> expiryManager = null;
	
	private final GuardIF guard;
	
	private final Map<Key,ReadWriteLock> creationGuards = new HashMap<>();
	
	@Override
	public <E extends Throwable> Value get (final Key key, ValueSupplier<ValueWithExpiry<Value>, E> supplier) throws E
	{
		LocalGuard lGuard = new LocalGuard(guard);
		
		try {
			lGuard.lockRead();
			var available_value = unsynchronized_fetch(key);
			if (available_value!=null) {
				return available_value;
			}
		
			// Didn't find the value but also have no supplier
			if (available_value==null && supplier==null) {
				return null;
			}
			
			lGuard.promoteLock();
			// Recheck if value is already there. Between last check and lock promotion somebody else might
			// have inserted the key.
			available_value = unsynchronized_fetch(key);
			if (available_value!=null) {
				return available_value;
			}
			
			var localGuard = creationGuards.get(key);
			if (localGuard!=null) {
				// somebody else called supplier, simple wait until finished
				lGuard.unlock();
				plugins.onValueCreateCollision(key);
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
			
			lGuard.unlock ();
		
			try {
				final var result = supplier.get();
				
				lGuard.lockWrite();
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
			lGuard.unlock();
		}
	}
	
	@Override
	public <E extends Throwable> Value get (final Key key, ValueSupplier<Value,E> supplier, long expireDate) throws E 
	{
		return this.get(key, () -> {
			return ValueWithExpiry.of(supplier.get(), expireDate);
		});
	}
    
	protected void unsynchronized_set (final Key key, final Value value, final long expiry)
	{
		if (expiryManager!=null && expiry>0) {			
			expiryManager.add(key, expiry);
		}
		
		plugins.onBeforeSet(key, value);
		final var previousValue = manager.put(key, value, null);	
		plugins.onAfterSet(key, previousValue, value);
		
		unsynchronized_shrink();
	}

	@Override
	public void set (final Key key, final Value value)	
	{
		guard.writeLocked(() -> unsynchronized_set(key,value,0));
	}
	
	@Override
	public void set (final Key key, final ValueWithExpiry<Value> ve)	
	{
		guard.writeLocked(() -> unsynchronized_set(key,ve.getValue(),ve.getExpiry()));
	}
	
	@Override
	public void set (final Key key, final Value value, long expiryDate)	
	{
		guard.writeLocked(() -> unsynchronized_set(key,value,expiryDate));
	}

	@Override
	public void set (final Map<Key, Value> content, long expiryDate)
	{
		set(content.entrySet(), expiryDate);
	}
	
	@Override
	public void set (final Set<Map.Entry<Key, Value>> entries)
	{
		set(entries, 0l);
	}
	
	@Override
	public void set (final Set<Map.Entry<Key, Value>> entries, long expiryDate)
	{	
		guard.writeLocked(() -> {
			int insertedInARow = 0;
			for (final var entry: entries) {
				unsynchronized_set(entry.getKey(), entry.getValue(), expiryDate);
				if (++insertedInARow > 100) {
					insertedInARow=0;
					// yield when readers are waiting
					guard.yield();
				}
			}			
		});
	}	
	
	@Override
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

	@Override
	public Value get (final Key key) 
	{	
		if (valueWithExpiryFactory!=null) {
			return get(key, () -> valueWithExpiryFactory.apply(key));
		}	

		return fetch(key);
	}
	
	private Value unsynchronized_fetch (final Key key)
	{
		plugins.onBeforeGet(key);
		final var value = manager.get(key);
		if (value==null && !manager.contains(key)) {
			plugins.onMiss(key);
		}
		plugins.onAfterGet(key, value);
		return value;		
	}
	
	@Override
	public Value fetch (final Key key) 
	{
		return guard.readLocked(() -> this.unsynchronized_fetch(key));	
	}
	
	protected Value unsynchronized_remove (Key key)
	{
		plugins.onBeforeRemove(key);
		final var removedElement = manager.remove(key);
		if (expiryManager!=null) expiryManager.remove(key);
		plugins.onAfterRemove(key, removedElement);
		return removedElement;		
	}
	
	@Override
	public void remove (Key key)
	{		
		guard.writeLocked(() -> unsynchronized_remove(key));		
	}

	@Override
	public int size ()
	{
		return guard.readLocked(manager::cachesize);
	}
	
	@Override
	public boolean isEmpty ()
	{
		return guard.readLocked(() -> manager.cachesize()==0);
	}

	@Override
	public boolean contains (Key key)
	{
		return guard.readLocked(() -> manager.contains (key));
	}
	
	private int maxSize = -1;
	
	@Override
	public MiniCache<Key, Value> setMaxSize (int maxSize)
	{
		synchronized (this) {
			this.maxSize=maxSize;
		}
		shrink();
		return this;
	}
	
	@Override
	public synchronized int getMaxSize ()
	{
		return maxSize;
	}

	private void unsynchronized_shrink ()
	{
		if (maxSize<1) return;		
		while (manager.cachesize()>maxSize) {
			final var last = manager.getForDeletion();
			if (last!=null) {
				unsynchronized_remove(last);
				plugins.onShrink(last);
			}
			else break;
		}	
	}
	
	protected void shrink ()
	{	
		guard.writeLocked(this::unsynchronized_shrink);
	}
	
	@Override
	public synchronized MiniCache<Key, Value> setValueWithExpiryFactory (final Function<Key,ValueWithExpiry<Value>> valueFactory)
	{
		guard.writeLocked(() -> {
			this.valueWithExpiryFactory=valueFactory;
		});
		return this;
	}
	
	@Override
	public synchronized MiniCache<Key, Value> setValueFactory (final Function<Key,Value> valueFactory)
	{
		guard.writeLocked(() -> {
			this.valueWithExpiryFactory= ValueWithExpiry.wrap(valueFactory);
		});
		return this;
	}
	
	@Override
	public synchronized MiniCache<Key, Value> setRefreshMethod (final Function<Key,ValueWithExpiry<Value>> refreshMethod)
	{
		if (refreshMethod==null) expiryManager.setDeletionTrigger(this::remove);
		else expiryManager.setDeletionTrigger((key) -> {
			final var newValue = refreshMethod.apply(key);
			plugins.onRefresh(key);
			this.set(key, newValue.getValue(), newValue.getExpiry());
		});
		return this;
	}
	
	@Override
	public synchronized Function<Key,ValueWithExpiry<Value>> getValueWithExpiryFactory ()
	{
		return this.valueWithExpiryFactory;
	}
	
	@Override
	public void clear ()
	{
		guard.writeLocked(() -> {
			manager.clear();
			plugins.onClear();
		});
	}
	
	@Override
	public Set<Key> keySet ()
	{
		return guard.readLocked(manager::keySet);
	}
	
	@Override
	public void writeLocked (Runnable f)
	{
		guard.writeLocked(f);
	}
	
	@Override
	public void readLocked (Runnable f)
	{
		guard.readLocked(f);
	}
	
	protected <Wrapper> Collection<Wrapper> values (Function<Map.Entry<Key, Value>, Wrapper> wrapper)
	{
		return guard.readLocked(() -> {
			final var values = new ArrayList<Wrapper>(this.size()); 
			for (var entry: manager.entrySet()) {
				values.add(wrapper.apply(entry));
			}
			return values;
		});
	}
	
	@Override
	public Collection<Value> values ()
	{
		return values((entry) -> entry.getValue());
	}
	
	@Override
	public Collection<ValueWithExpiry<Value>> valuesWithExpiryDate ()
	{
		// Define a lambda function to retrieve the expire time. If no expire manager exists its always 0, otherwise ask the expiryManager
		final Function<Key, Long> getExpireTime = (expiryManager==null)?(key) -> 0L:(key) -> expiryManager.getExpiryTime(key);

		return values((entry) -> new ValueWithExpiry<Value>(entry.getValue(), getExpireTime.apply(entry.getKey())));
	}
	
	protected <Wrapper> Set<Map.Entry<Key, Wrapper>> entrySet (Function<Map.Entry<Key, Value>, Wrapper> wrapper)
	{
		return guard.readLocked(() -> {
			final var values = new HashSet<Map.Entry<Key, Wrapper>>(this.size()); 
			for (var entry: manager.entrySet()) {
				values.add(new AbstractMap.SimpleEntry<>(entry.getKey(),wrapper.apply(entry)));
			}
			return values;
		});
	}
	
	@Override
	public Set<Map.Entry<Key, Value>> entrySet ()
	{
		return entrySet((entry) -> entry.getValue());
	}
	
	@Override
	public Set<Map.Entry<Key, ValueWithExpiry<Value>>> entrySetWithExpiryDate ()
	{
		// Define a lambda function to retrieve the expire time. If no expire manager exists its always 0, otherwise ask the expiryManager
		final Function<Key, Long> getExpireTime = (expiryManager==null)?(key) -> 0L:(key) -> expiryManager.getExpiryTime(key);

		return entrySet((entry) -> { 
			return new ValueWithExpiry<Value>(entry.getValue(), getExpireTime.apply(entry.getKey())); 
		});
	}

	/* **********************************************************************************************************************************
	 * 
	 * Subscription interface
	 * 
	 ************************************************************************************************************************************/

	private AsynchronousSubscriberPlugin<Key, Value> subscriptionPlugin = null;
	
	@Override
	public void subscribe(Subscriber<? super CacheChangeEvent<Key, Value>> subscriber) {
		if (subscriptionPlugin==null) {
			subscriptionPlugin = new AsynchronousSubscriberPlugin<>();
			plugins.addPlugin(subscriptionPlugin);
		}
		subscriptionPlugin.subscribe(subscriber);
	}
	
	/* **********************************************************************************************************************************
	 * 
	 * plugin implementation 
	 * 
	 ************************************************************************************************************************************/
	
	private PluginManager<Key, Value> plugins = new PluginManager<>();

	protected void addPlugin(Plugin<Key,Value> plugin)
	{
		plugins.addPlugin(plugin);
	}
	
	protected void removePlugin(Plugin<Key,Value> plugin)
	{
		plugins.removePlugin(plugin);
	}
	
	/* **********************************************************************************************************************************
	 * 
	 * Statistics implementation 
	 * 
	 ************************************************************************************************************************************/
	
	private StatisticsPlugin<Key, Value> statisticsPlugin = null;
	
	@Override
	public void setCalculateStatistics (boolean v)
	{
		if (v && statisticsPlugin==null) {
			statisticsPlugin = new StatisticsPlugin<>(this);
			plugins.addPlugin(statisticsPlugin);
		}
		if (!v && statisticsPlugin!=null) {
			plugins.removePlugin(statisticsPlugin);
			statisticsPlugin.setIsActive(false);
			statisticsPlugin=null;
		}
	}
	
	@Override
	public boolean isCalcuatingStatistics()
	{
		return statisticsPlugin!=null;
	}
	
	@Override
	public StatisticsPlugin<Key, Value> getStatistics ()
	{
		return statisticsPlugin;
	}

}
