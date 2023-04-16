package com.github.ds67.jminicache.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ExpiryManager<Key> {

	private TreeMap<Long, Collection<Key>> expieries = new TreeMap<>();
	private HashMap<Key, Long> expireDatesToKeys = new HashMap<>(); 
	
	private Consumer<Key> deletionTrigger;
	
	public ExpiryManager(final Consumer<Key> deletionTrigger, final ScheduledExecutorService scheduler) 
	{
		this.deletionTrigger=deletionTrigger;
		this.scheduler=scheduler;
	}
	
	public void setDeletionTrigger (final Consumer<Key> deletionTrigger)
	{
		this.deletionTrigger=deletionTrigger;
	}
	
	public synchronized void add (final Key key, long expiry)
	{
		unsynchronized_remove(key);
		expieries.compute(expiry, (k,v) -> {
			if (v==null) {
				v=new ArrayList<Key>();
			}
			v.add(key);
			return v;
		});
		expireDatesToKeys.put(key, expiry);
		scheduleNextItem();
	}
	
	public synchronized long getExpiryTime (final Key key)
	{
		final var e = expireDatesToKeys.get(key);
		return e==null?0:e;
	}
	
	private void unsynchronized_remove (Key key)
	{
		Long expiry = expireDatesToKeys.get(key);
		if (expiry!=null) {
			expieries.compute(expiry, (k,v) -> {
				if (v!=null) {
					v.remove(key);
					if (v.isEmpty()) return null;
				}
				return v;
			});
			expireDatesToKeys.remove(key);
		}
	}
	
	public synchronized void remove (Key key)
	{
		unsynchronized_remove(key);
		scheduleNextItem();
	}
	
	/**
	 * Create a single scheduler thread for all cache instances. By using an own thread factory it is possible so set a descriptive name
	 * and the thread as a daemon thread to allow a graceful application shutdown
	 */
	private final ScheduledExecutorService scheduler;
	
	private ScheduledFuture<?> nextExecution=null;
	private long nextExpireDate = 0;
	
	private void scheduleNextItem ()
	{
		if (expieries.isEmpty()) return;
		
		final long newNextExpireDate = expieries.firstKey();

		if (nextExpireDate!=newNextExpireDate) {
			if (nextExecution!=null) nextExecution.cancel(false);
			final long delay = newNextExpireDate-System.currentTimeMillis();
			nextExecution = scheduler.schedule(this::deleteOldestEntry, delay>0?delay:0, TimeUnit.MILLISECONDS);
			nextExpireDate=newNextExpireDate;
			System.out.println ("Next expiry scheduled with delay "+delay+" ms");
		}
	}
	
	private synchronized void deleteOldestEntry ()
	{
		boolean done = false;
		do {
			var oldestEntry = expieries.firstEntry();
			if (oldestEntry.getKey()<=System.currentTimeMillis()) {
				var keys = new ArrayList<>(oldestEntry.getValue());
				keys.forEach(deletionTrigger);
				expieries.remove(oldestEntry.getKey());
			}
			else { done = true; }
		}
		while (!done);
		nextExecution=null;
		scheduleNextItem();
	}
}
