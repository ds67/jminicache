package com.github.ds67.jminicache.burnin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAdder;

import org.junit.jupiter.api.Test;

import com.github.ds67.jminicache.MiniCache;
import com.github.ds67.jminicache.MiniCacheBuilder;
import com.github.ds67.jminicache.MiniCacheBuilder.EvictionPolicy;

public class ManyThreadsTest {
	
	private void getAndAddElements (MiniCache<Integer, byte[]> cache, int elements)
	{
		final var randomizer = new Random();
		var keys = new Random().ints(elements);
		
		keys.forEach(key -> {
			cache.get(key%256,() -> {
				int size = randomizer.nextInt(10000);
				var buffer = new byte[size];
				randomizer.nextBytes(buffer);
				return buffer;
			},0);
		});
 	}

	private void burn (MiniCache<Integer, byte[]> cache, int iterations) throws InterruptedException
	{
		var latch = new CountDownLatch(5);
		
		for (int i=0;i<5;i++) {
			new Thread(() -> { this.getAndAddElements(cache, iterations); latch.countDown(); }).start();
		}
				
		latch.await();
	}
	
	
	@Test
	public void fifoParallelTest () throws InterruptedException
	{
		final var cache =  new MiniCacheBuilder<Integer, byte[]>()
			.setEvictionPolicy(EvictionPolicy.EVICTION_FIFO)
           .setMaxSize(120)
           .build();
		
		burn(cache,10000);

		assertEquals(120, cache.size());
	}
	
	@Test
	public void lruParallelTest () throws InterruptedException
	{
		final var cache = new MiniCacheBuilder<Integer, byte[]>()
				.setEvictionPolicy(EvictionPolicy.EVICTION_LRU)
		           .setMaxSize(250)
		           .build();
		
		burn(cache,10000);
		
		assertEquals(250, cache.size());
	}

	LongAdder gets = new LongAdder();
	LongAdder sets = new LongAdder();

	private void getAndAddElementsWithStatistics (MiniCache<Integer, byte[]> cache, int elements)
	{
		final var randomizer = new Random();
		var keys = new Random().ints(elements);
		
		keys.forEach(key -> {
			cache.get(key%256,() -> {
				int size = randomizer.nextInt(10000);
				var buffer = new byte[size];
				randomizer.nextBytes(buffer);
				sets.increment();
				return buffer;
			},0);
			gets.increment();
		});
 	}
	
	private void burnWithStatistics (MiniCache<Integer, byte[]> cache, int iterations) throws InterruptedException
	{
		var latch = new CountDownLatch(5);
		
		for (int i=0;i<5;i++) {
			new Thread(() -> { this.getAndAddElementsWithStatistics(cache, iterations); latch.countDown(); }).start();
		}
				
		latch.await();
	}
	
	/*
	 * This test does also a burnin test but counts the statistics internally as well as external.
	 * 
	 */
	@Test
	public void lruParallelTestWithStatistics () throws InterruptedException
	{
		final int maxSize = 250;
		
		final var cache = new MiniCacheBuilder<Integer, byte[]>()
				.setEvictionPolicy(EvictionPolicy.EVICTION_LRU)
		           .setMaxSize(maxSize)
		           .setCalculateStatistics(true)
		           .build();
		
		burnWithStatistics(cache,40*maxSize);
		final var stats = cache.getStatistics();
		
		System.out.println (stats.toString());
		// Test statistic consistency: number of gets should equal counted internally and externally 
		assertEquals (gets.longValue(), stats.getGetCounter());
		// Test statistic consistency: number of sets should equal counted internally and externally
		assertEquals (sets.longValue(), stats.getUpdateCounter());
		// Test statistic consistency: The number of insert less than the number of removals should the cache size
		assertEquals (maxSize, stats.getUpdateCounter()-stats.getRemovalCounter());
		// Test statistic consistency: The number of all gets should be the same as all fetches minus all misses plus all inserts:
		// A get is either a immediate hit (and therefore a single fetch) or an unsuccessful fetch (which is a miss), a insert and afterwards a successful fetch
		// Two accesses are true for all parallel read configurations (as FIFO EVICTION) as a second read access must be tried after the
		// exclusive lock was acquired. Some other thread might have inserted the key in the mean time until the exclusive lock was acquired 
		assertEquals (stats.getGetCounter(), stats.getFetchCounter()-stats.getMissesCounter()+stats.getUpdateCounter());
		
		assertEquals (maxSize, cache.size());
	}
}
