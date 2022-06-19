package ds67.jminicache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

public class FifoTest {

	// Just some function for cache value creation. Might be everything
	static int sqr (int n)
	{
		return n*n;
	}
	
	@Test
	public void fifoFunctionTest() throws Exception 
	{
		final int maxSize = 10;
		final var cache = new MiniCache<Integer, Integer>(CachePolicy.EVICTION_FIFO)
							  .setValueFactory(FifoTest::sqr)
				              .setMaxSize(maxSize);
		
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

	/*
	 * Checks if the clear operation functions as expected
	 * 
	 */
	@Test
	public void fifoClearTest() throws Exception 
	{
		final int maxSize = 10;
		final var cache = new MiniCache<Integer, Integer>(CachePolicy.EVICTION_FIFO)
							  .setValueFactory(FifoTest::sqr)
				              .setMaxSize(maxSize);
		
		// Fill cache
		for (int i=0;i<4*maxSize;i++) cache.get(i%(2*maxSize));
		
		assertEquals (maxSize, cache.size(), "Cache size after filling unexpected");
		cache.clear();
		// After clearing no elements are in the cache
		assertEquals (0, cache.size(), "Cache size after clearing unexpected");
		
		final var expectedKeySet = new HashSet<Integer>(); 
		for (int i=0;i<maxSize+maxSize/2;i++) {
			if (i>=maxSize/2) expectedKeySet.add(i);
			cache.set(i, sqr(i));
		}
		// After refilling the expected keys are found. (black box test to check if the internal structured where correctly reseted) 
		assertEquals (expectedKeySet, cache.keySet(), "Unexpected keys are found in the cache");
	}
}
