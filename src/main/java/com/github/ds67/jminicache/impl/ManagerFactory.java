package com.github.ds67.jminicache.impl;

import com.github.ds67.jminicache.CachePolicy;
import com.github.ds67.jminicache.impl.eviction.FIFOManager;
import com.github.ds67.jminicache.impl.eviction.LRUManager;
import com.github.ds67.jminicache.impl.eviction.NoopManager;
import com.github.ds67.jminicache.impl.guard.ReadWriteGuard;
import com.github.ds67.jminicache.impl.guard.SimpleLockGuard;
import com.github.ds67.jminicache.impl.payload.KeySoftValuePayload;
import com.github.ds67.jminicache.impl.payload.KeyValuePayload;
import com.github.ds67.jminicache.impl.payload.ListWrapper;
import com.github.ds67.jminicache.impl.storage.SimpleCacheManager;
import com.github.ds67.jminicache.impl.storage.SoftManager;
import com.github.ds67.jminicache.impl.storage.StorageManagerIF;

public class ManagerFactory {

	public ManagerFactory() {
	}

	public static <Key, Value> StorageManagerIF<Key, Value, ?> createCacheManager (final boolean soft, final CachePolicy policy)
	{
		if (soft==false && policy==CachePolicy.EVICTION_LRU) {
			final var lruEvictionManager = new LRUManager<Key, Value, ListWrapper<Key, Value, KeyValuePayload<Key,Value>>>(
				// function to wrap	
				(k,v) -> {
				     return new ListWrapper<Key, Value, KeyValuePayload<Key,Value>>(new KeyValuePayload<Key,Value>(k,v));
			    },
				// function to unwrap
				(w) -> w.getPayload()
			);
			
			return new SimpleCacheManager<Key, Value, ListWrapper<Key, Value, KeyValuePayload<Key,Value>>>(new SimpleLockGuard(), lruEvictionManager);
		}
		else if (soft==true && policy==CachePolicy.EVICTION_LRU) {
			final var lruEvictionManager = new LRUManager<Key, Value, ListWrapper<Key, Value, KeySoftValuePayload<Key,Value>>>(null, null);
			final var cacheManager = new SimpleCacheManager<Key, Value, ListWrapper<Key, Value, KeySoftValuePayload<Key,Value>>>(new SimpleLockGuard(),  lruEvictionManager);
			
			return new SoftManager<Key, Value, ListWrapper<Key, Value, KeySoftValuePayload<Key,Value>>>(
					cacheManager, 
					// Function to wrap the payload
					(k,v,q) -> {
						return new ListWrapper<Key, Value, KeySoftValuePayload<Key,Value>>(new KeySoftValuePayload<Key,Value>(k,v,q));
					},
					// function to unwrap the payload
					(w) -> {
						return w.getPayload();
					}
			);
		}
		else if (soft==false && policy==CachePolicy.EVICTION_FIFO) {
			final var evictionManager = new FIFOManager<Key, Value, ListWrapper<Key, Value, KeyValuePayload<Key,Value>>>(
				// Function to wrap the payload	
				(k,v) -> {
				   return new ListWrapper<Key, Value, KeyValuePayload<Key,Value>>(new KeyValuePayload<Key,Value>(k,v));
			    },
				// Function to unwrap the payload
				(w) -> {
					return w.getPayload();
				}
			);
			
			return new SimpleCacheManager<Key, Value, ListWrapper<Key, Value, KeyValuePayload<Key,Value>>>(new ReadWriteGuard(), evictionManager);
		}
		else if (soft==false && policy==CachePolicy.EVICTION_NONE) {
			final var evictionManager = new NoopManager<Key, Value>();
			
			return new SimpleCacheManager<Key, Value, Value>(new ReadWriteGuard(), evictionManager);
		}
		
		return null;
	}	
}
