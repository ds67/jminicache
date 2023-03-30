package com.github.ds67.jminicache.usecases;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.ds67.jminicache.CachePolicy;
import com.github.ds67.jminicache.MiniCache;

/*
 * Shows how to persist the cache and restore it afterwards.
 * 
 * To different storage methods are used but this is more for testing the serialization and not the cache itself.
 * However, it shows clearly that the "persistAndLoad" method which knows about the structure of the data creates files
 * half as big as the method "persistAndLoadAsEntrySet" and loads the data structure 20x faster than the more generic approach
 * in "persistAndLoadAsEntrySet"
 * 
 */
public class PersistCacheOnShutdown {

	static final int maxSize = 100000;
	
	@Test
	public void persistAndLoad () throws IOException
	{
		System.out.println ("Store entryset as a list of key / values pairs");

		final var cache = new MiniCache<Long, Long>(CachePolicy.EVICTION_FIFO)
	              .setMaxSize(maxSize);
		
		// Fill cache with random values
		final var randoms = new Random().longs().iterator();
		for (int i=0;i<maxSize;i++) {
			cache.set(randoms.nextLong(),randoms.nextLong());
		}

		// Store cache in a temporary file on disc
		var storage = Files.createTempFile("jminicache", ".bin");
		try (var objectOutputStream = new ObjectOutputStream(new FileOutputStream(storage.toFile()))) {
			objectOutputStream.writeInt(cache.size());
			for (var entry: cache.entrySet()) {
				objectOutputStream.writeLong(entry.getKey());
				objectOutputStream.writeLong(entry.getValue());
			}
		}

		System.out.println ("Storage file size: "+Files.size(storage)+ " bytes");

		// Recreate the cache by loading the file entries one by one and putting them in the cache.
		// This doesn't need any big temporary data structures but  add the entries immediately after reading to the cache
		// If the storage is very slow this allows to access the first entries much faster in camparison to load the whole
		// data into a local data structure with is than inserted at once in the cache.
		{
			System.out.println ("Recreate with single sets");
			final var loadedCache = new MiniCache<Long, Long>(CachePolicy.EVICTION_FIFO)
		              .setMaxSize(maxSize);
			
			final long start = System.currentTimeMillis();
			try (var input = new ObjectInputStream(new FileInputStream(storage.toFile()))) {
				int size = input.readInt();
				for (int i=0;i<size;i++) {
					long key = input.readLong();
					long value = input.readLong();
					loadedCache.set(key,value);
				}
			}
			final long end = System.currentTimeMillis();
			System.out.println ("Recreation of "+maxSize+" elements took "+(end-start)+" ms");
			assertIterableEquals(cache.entrySet(),loadedCache.entrySet());
		}
		
		// Restore the cache data by loading the data first in a Map like structure and in a second step
		// all at once into the cache.
		// This has the advantage that not for every insert a complete cache look must be done. This seems about 
		// a little bit faster than the first approach (depends also on file caching, be careful)
		{
			System.out.println ("Recreate by setting a map at once");
			final var loadedCache = new MiniCache<Long, Long>(CachePolicy.EVICTION_FIFO)
		              .setMaxSize(maxSize);
			
			final long start = System.currentTimeMillis();
			Set<Map.Entry<Long, Long>> entries = new HashSet<>();
			try (var input = new ObjectInputStream(new FileInputStream(storage.toFile()))) {
				int size = input.readInt();
				for (int i=0;i<size;i++) {
					long key = input.readLong();
					long value = input.readLong();
					entries.add(new AbstractMap.SimpleEntry<>(key,value));
				}
				loadedCache.set(entries);
			}
			final long end = System.currentTimeMillis();
			System.out.println ("Recreation of "+maxSize+" elements took "+(end-start)+" ms");
			assertIterableEquals(cache.entrySet(),loadedCache.entrySet());
		}
				
		storage.toFile().delete();
	}
	
	@Test
	public void persistAndLoadAsEntrySet () throws IOException, ClassNotFoundException
	{
		System.out.println ("Store entryset as an object and recreate it as a single object");
		
		final var cache = new MiniCache<Long, Long>(CachePolicy.EVICTION_FIFO)
	              .setMaxSize(maxSize);
		
		// Fill cache with random values
		final var randoms = new Random().longs().iterator();
		for (int i=0;i<maxSize;i++) {
			cache.set(randoms.nextLong(),randoms.nextLong());
		}

		// Store cache in a temporary file on disc
		var storage = Files.createTempFile("jminicache", ".bin");
		try (var objectOutputStream = new ObjectOutputStream(new FileOutputStream(storage.toFile()))) {
			objectOutputStream.writeObject(cache.entrySet());
		}

		System.out.println ("Storage file size: "+Files.size(storage)+ " bytes");
		
		// Create new empty cache  
		final var loadedCache = new MiniCache<Long, Long>(CachePolicy.EVICTION_FIFO)
	              .setMaxSize(maxSize);
		
		final long start = System.currentTimeMillis();
		try (var input = new ObjectInputStream(new FileInputStream(storage.toFile()))) {
			@SuppressWarnings("unchecked")
			var data = (Set<Map.Entry<Long,Long>>)input.readObject();
			loadedCache.set(data);
		}
		final long end = System.currentTimeMillis();
		System.out.println ("Recreation of "+maxSize+" elements took "+(end-start)+" ms");
		
		assertIterableEquals(cache.entrySet(),loadedCache.entrySet());
		
		storage.toFile().delete();
	}
}
