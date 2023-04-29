package com.github.ds67.jminicache;

import java.util.ArrayList;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class SoftFifoTest {
	
	private byte[] fill (int valueSize)
	{
		final var randomizer = new Random();
		var buffer = new byte[valueSize];
		randomizer.nextBytes(buffer);
		return buffer;
	}
	
	@Test
	public void softFifoTest() throws InterruptedException
	{	
		final var memoryToFill = Runtime.getRuntime().maxMemory();
		final int maxSize =100;
		final var valueSize = (int)(memoryToFill/maxSize/2);
		
		System.out.println ("Size of value: "+valueSize);
		System.out.println ("Available memory: "+Runtime.getRuntime().freeMemory()/1024+"kB");
		
		final var cache = new MiniCacheBuilder<Integer, byte[]>()
				.setEvictionPolicy(MiniCacheBuilder.EvictionPolicy.EVICTION_FIFO)
			    .setMaxSize(maxSize)
			    .setUseSoftKeys(true)
			    .build();
		
		// fill cache
		for (int i=0;i<maxSize;i++) {
			cache.set(i,fill(valueSize));
		}

		System.gc();
		
		System.out.println ("Available memory: "+Runtime.getRuntime().freeMemory()/1024+"kB");

		int firstSize = cache.size();
		
		ArrayList<byte[]> buffer = new ArrayList<>(maxSize);
		// start consume more memory
		for (int i=0;i<maxSize;i++) {
			buffer.add(fill(valueSize));
			System.out.println ("Available memory: "+Runtime.getRuntime().freeMemory()/1024+"kB");
			System.out.println("Size: "+cache.size());
		}

		System.out.println ("Available memory: "+Runtime.getRuntime().freeMemory()/1024+"kB");

		int secondSize = cache.size();
		
		Thread.sleep(100);
		
		System.out.println (String.format("First size: %d, second size: %d",firstSize, secondSize));
		
		int l = 0;
		for (int i=0;i<buffer.size();i++) {
			l+=buffer.get(i).length;
		}
		System.out.println (""+l/1024+"kb");
	}

}
