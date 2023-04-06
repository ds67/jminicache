package com.github.ds67.jminicache;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class InterfaceUseTest {

	@Test
	public void createWithoutPolicies() 
	{
		var cache = new MiniCache<>();
	}

	@Test
	public void throwOnDoublePolicy () 
	{
		assertThrows(IllegalArgumentException.class, () -> {
			new MiniCache<Integer,Integer>(CachePolicy.EVICTION_FIFO, CachePolicy.EVICTION_LRU, CachePolicy.THROW_ON_MISCONFIGURATION);
		});
		
		// no throw without the THROW_ON_MISCONFIGURATION flag
		new MiniCache<Integer,Integer>(CachePolicy.EVICTION_FIFO, CachePolicy.EVICTION_LRU);
	}
	
	
}
