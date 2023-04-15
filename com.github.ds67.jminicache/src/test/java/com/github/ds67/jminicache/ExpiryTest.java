package com.github.ds67.jminicache;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ExpiryTest {

	@Test
	public void simpleExpireTestOfOneElement () throws InterruptedException
	{
		final int maxSize = 10;
		final var cache = new MiniCacheBuilder<Integer, Integer>()
					.setEvictionPolicy(MiniCacheBuilder.EvictionPolicy.EVICTION_FIFO)
					.setUseExpiry(true)
				    .setMaxSize(maxSize)
				    .build();
		
		final var expireDate = System.currentTimeMillis()+300;
		cache.set(42,42,expireDate);
		cache.set(43,43,expireDate);
		cache.set(43,43,System.currentTimeMillis()+600);
		Thread.sleep(400);
		
		// Two elements with the same expire date should have been removed but thos the last element 
		assertEquals(1,cache.size());
		Thread.sleep(200);
		assertEquals(0,cache.size());
	}
	
	@Test
	public void mutipleExpireTestOfElements () throws InterruptedException
	{
		final int maxSize = 10;
		final var cache = new MiniCacheBuilder<Integer, Integer>()
				.setEvictionPolicy(MiniCacheBuilder.EvictionPolicy.EVICTION_FIFO)
				.setUseExpiry(true)
			    .setMaxSize(maxSize)
			    .build();
	
		final var expireDate = System.currentTimeMillis()+300;
		cache.set(42,42,expireDate);
		cache.set(43,43,expireDate);
		cache.set(43,43,System.currentTimeMillis()+600);
		Thread.sleep(400);
		
		// Two elements with the same expire date should have been removed but thos the last element 
		assertEquals(1,cache.size());
		Thread.sleep(300);
		
		assertEquals(0,cache.size());
	}
}
