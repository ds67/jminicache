package com.github.ds67.jminicache.burnin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

import com.github.ds67.jminicache.CachePolicy;
import com.github.ds67.jminicache.MiniCache;

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
		final var cache = new MiniCache<Integer, byte[]>(CachePolicy.EVICTION_FIFO)
	              .setMaxSize(120);
		
		burn(cache,10000);

		assertEquals(120, cache.size());
	}
	
	@Test
	public void lruParallelTest () throws InterruptedException
	{
		final var cache = new MiniCache<Integer, byte[]>(CachePolicy.EVICTION_LRU)
	              .setMaxSize(250);
		
		burn(cache,10000);

		assertEquals(250, cache.size());
	}
}
