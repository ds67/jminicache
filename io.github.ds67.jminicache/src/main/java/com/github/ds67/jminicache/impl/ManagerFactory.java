package com.github.ds67.jminicache.impl;

import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Supplier;

import com.github.ds67.jminicache.MiniCacheBuilder;
import com.github.ds67.jminicache.impl.eviction.FIFOManager;
import com.github.ds67.jminicache.impl.eviction.LRUManager;
import com.github.ds67.jminicache.impl.eviction.NoopManager;
import com.github.ds67.jminicache.impl.guard.ReadWriteGuard;
import com.github.ds67.jminicache.impl.guard.SimpleLockGuard;
import com.github.ds67.jminicache.impl.payload.KeySoftValuePayload;
import com.github.ds67.jminicache.impl.payload.KeyValuePayload;
import com.github.ds67.jminicache.impl.payload.ListWrapper;
import com.github.ds67.jminicache.impl.storage.MapBasedCacheManager;
import com.github.ds67.jminicache.impl.storage.SoftManager;
import com.github.ds67.jminicache.impl.storage.StorageManagerIF;

public class ManagerFactory {

	public ManagerFactory() {
	}

	private static <Key, Wrapper> Supplier<Map<Key,Wrapper>> mapCreatorFunction (MiniCacheBuilder.StoragePolicy storagePolicy, Comparator<Key> comparator)
	{
		if (storagePolicy==MiniCacheBuilder.StoragePolicy.TREE_MAP_STORAGE) {
			if (comparator!=null) {
				return () -> new TreeMap<Key,Wrapper>(comparator);
			}
			else {
				return () -> new TreeMap<Key,Wrapper>();
			}
		}
		else {
			return () -> new HashMap<Key,Wrapper>();
		}
	}

	public static <Key, Value> StorageManagerIF<Key, Value, ?> createCacheManager (MiniCacheBuilder.EvictionPolicy policy,
																				   MiniCacheBuilder.StoragePolicy storagePolicy,
																				   Comparator<Key> comparator,
			                                                                       boolean weakKeys)
	{
		
		
		if (!weakKeys && policy.equals(MiniCacheBuilder.EvictionPolicy.EVICTION_LRU)) {
			final var lruEvictionManager = new LRUManager<Key, Value, ListWrapper<Key, Value, KeyValuePayload<Key,Value>>>(
				// function to wrap	
				(k,v) -> {
				     return new ListWrapper<Key, Value, KeyValuePayload<Key,Value>>(new KeyValuePayload<Key,Value>(k,v));
			    },
				// function to unwrap
				(w) -> w.getPayload()
			);
			
			return new MapBasedCacheManager<Key, Value, ListWrapper<Key, Value, KeyValuePayload<Key,Value>>>(mapCreatorFunction(storagePolicy, comparator), new SimpleLockGuard(), lruEvictionManager);
		}
		else if (weakKeys && policy.equals(MiniCacheBuilder.EvictionPolicy.EVICTION_LRU)) {
			final var lruEvictionManager = new LRUManager<Key, Value, ListWrapper<Key, Value, KeySoftValuePayload<Key,Value>>>(null, null);
			final var cacheManager = new MapBasedCacheManager<Key, Value, ListWrapper<Key, Value, KeySoftValuePayload<Key,Value>>>(mapCreatorFunction(storagePolicy, comparator), new SimpleLockGuard(),  lruEvictionManager);
			
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
		else if (!weakKeys && policy.equals(MiniCacheBuilder.EvictionPolicy.EVICTION_FIFO)) {
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
			
			return new MapBasedCacheManager<Key, Value, ListWrapper<Key, Value, KeyValuePayload<Key,Value>>>(mapCreatorFunction(storagePolicy, comparator), new ReadWriteGuard(), evictionManager);
		}
		else if (weakKeys && policy.equals(MiniCacheBuilder.EvictionPolicy.EVICTION_FIFO)) {
			final var evictionManager = new FIFOManager<Key, Value, ListWrapper<Key, Value, KeySoftValuePayload<Key,Value>>>(
				// No function to wrap the payload, will be provided by the storage manager	
				null, 
				// Function to unwrap the payload
				(w) -> {
					return w.getPayload();
				}
			);
			
			final var cacheManager = new MapBasedCacheManager<Key, Value, ListWrapper<Key, Value, KeySoftValuePayload<Key,Value>>>(mapCreatorFunction(storagePolicy, comparator), new ReadWriteGuard(), evictionManager);
			
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
		else if (policy.equals(MiniCacheBuilder.EvictionPolicy.EVICTION_NONE)) {
			
			if (weakKeys) {
		    	final var evictionManager = new NoopManager<Key, Value, KeySoftValuePayload<Key,Value>>(null, null);	    	
				final var cacheManager = new MapBasedCacheManager<Key, Value, KeySoftValuePayload<Key,Value>>(mapCreatorFunction(storagePolicy, comparator), new ReadWriteGuard(),  evictionManager);
				
				return new SoftManager<Key, Value, KeySoftValuePayload<Key,Value>>(
						cacheManager, 
						// Function to wrap the payload
						(k,v,q) -> {
							return new KeySoftValuePayload<Key,Value>(k,v,q);
						},
						// function to unwrap the payload
						(w) -> {
							return w.getPayload();
						}
				);
			}
		    else {
				final var evictionManager = NoopManager.<Key, Value>ofIdentity();
				return new MapBasedCacheManager<Key, Value, Value>(mapCreatorFunction(storagePolicy, comparator), new ReadWriteGuard(), evictionManager);
		    }
		}
		
		return null;
	}	
}
