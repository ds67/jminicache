package com.github.ds67.jminicache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class LRUTest {

	// Just some function for cache value creation. Might be everything
	static int sqr (int n)
	{
		return n*n;
	}
	
	@Test
	public void lruNoAccessTest() throws Exception 
	{
		final int maxSize = 10;
		final var cache = new MiniCacheBuilder<Integer, Integer>()
				.setEvictionPolicy(MiniCacheBuilder.EvictionPolicy.EVICTION_LRU)
			    .setMaxSize(maxSize)
			    .setValueFactory(FifoTest::sqr)
			    .build();
		
		// Insert but never access. Then the LRU should fall back in insertion order and remove the oldest entries
		for (int i=0;i<4*maxSize;i++) {
			int v = i%(2*maxSize);
			// Check if the basic value creation works
			assertEquals(sqr(v), cache.get(v), "Unexpected cached value for the key");
			// Check if the maximum size is not exceeded 
			assertEquals(Math.min(maxSize, i+1), cache.size(), "Unexpected cache size");
			if (cache.size()==maxSize) {
				assertTrue(!cache.contains((i-maxSize)%20), "Oldest value no longer part of the cache");
			}
		}
	}
	
	@Test
	public void basicLRUTest ()
	{
		final int maxSize = 10;
		final var cache = new MiniCacheBuilder<Integer, Integer>()
				.setEvictionPolicy(MiniCacheBuilder.EvictionPolicy.EVICTION_LRU)
			    .setMaxSize(maxSize)
			    .build();
		
		for (int i=0;i<10;i++) cache.set(i,i);
		cache.fetch(0);
		cache.set(10,10);
		
		assertIterableEquals(cache.keySet(),Arrays.asList(0,2,3,4,5,6,7,8,9,10),"The 1 key should have been removed");
	}
	
}
