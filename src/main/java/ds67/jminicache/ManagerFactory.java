package ds67.jminicache;

import ds67.jminicache.impl.eviction.FIFOManager;
import ds67.jminicache.impl.eviction.LRUManager;
import ds67.jminicache.impl.eviction.NoopManager;
import ds67.jminicache.impl.guard.ReadWriteGuard;
import ds67.jminicache.impl.guard.SimpleLockGuard;
import ds67.jminicache.impl.payload.KeySoftValuePayload;
import ds67.jminicache.impl.payload.KeyValuePayload;
import ds67.jminicache.impl.payload.ListWrapper;
import ds67.jminicache.impl.payload.PayloadIF;
import ds67.jminicache.impl.payload.PlainPayload;
import ds67.jminicache.impl.storage.StorageManagerIF;
import ds67.jminicache.impl.storage.SimpleCacheManager;
import ds67.jminicache.impl.storage.SoftManager;

public class ManagerFactory {

	public ManagerFactory() {
		// TODO Auto-generated constructor stub
	}

	public static <Key, Value> StorageManagerIF<Key, Value> createCacheManager (final boolean soft, final CachePolicy policy)
	{
		if (soft==false && policy==CachePolicy.EVICTION_LRU) {
			final var lruEvictionManager = new LRUManager<Key, Value, ListWrapper<Key, Value, KeyValuePayload<Key,Value>>>((k,v) -> {
				return new ListWrapper<Key, Value, KeyValuePayload<Key,Value>>(new KeyValuePayload<Key,Value>(k,v));
			});
			
			return new SimpleCacheManager<Key, Value, ListWrapper<Key, Value, KeyValuePayload<Key,Value>>>(new SimpleLockGuard(), lruEvictionManager);
		}
		else if (soft==true && policy==CachePolicy.EVICTION_LRU) {
			final var lruEvictionManager = new LRUManager<Key, Value, ListWrapper<Key, Value, KeySoftValuePayload<Key,Value>>>(null);
			final var cacheManager = new SimpleCacheManager<Key, Value, ListWrapper<Key, Value, KeySoftValuePayload<Key,Value>>>(new SimpleLockGuard(),  lruEvictionManager);
			
			return new SoftManager<Key, Value, ListWrapper<Key, Value, KeySoftValuePayload<Key,Value>>>(
					cacheManager, (k,v,q) -> {
						return new ListWrapper<Key, Value, KeySoftValuePayload<Key,Value>>(new KeySoftValuePayload<Key,Value>(k,v,q));
					});
		}
		else if (soft==false && policy==CachePolicy.EVICTION_FIFO) {
			final var evictionManager = new FIFOManager<Key, Value, ListWrapper<Key, Value, KeyValuePayload<Key,Value>>>((k,v) -> {
				return new ListWrapper<Key, Value, KeyValuePayload<Key,Value>>(new KeyValuePayload<Key,Value>(k,v));
			});
			
			return new SimpleCacheManager<Key, Value, ListWrapper<Key, Value, KeyValuePayload<Key,Value>>>(new ReadWriteGuard(), evictionManager);
		}
		else if (soft==false && policy==CachePolicy.EVICTION_NONE) {
			final var evictionManager = new NoopManager<Key, Value>((k,v) -> {
				// return new PlainPayload<Key, Value>(v);
				return new PayloadIF<Key,Value>() {

					@Override
					public void onRemove() {
					}

					@Override
					public Value getPayload() {
						return v;
					}

					@Override
					public Key getKey() {
						return null;
					}
					
				};
			});
			
			return new SimpleCacheManager<Key, Value, PayloadIF<Key, Value>>(new ReadWriteGuard(), evictionManager);
		}
		
		return null;
	}	
}
