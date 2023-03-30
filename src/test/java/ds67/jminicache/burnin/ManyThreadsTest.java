package ds67.jminicache.burnin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

import ds67.jminicache.CachePolicy;
import ds67.jminicache.MiniCache;

public class ManyThreadsTest {

	private void getAndAddElements (MiniCache<Integer, byte[]> cache, int elements)
	{
		final var randomizer = new Random();
		var keys = new Random().ints(elements);
		
		System.out.println ("Start thread");
		
		keys.forEach(key -> {
			cache.get(key%256,() -> {
				int size = randomizer.nextInt(10000);
				var buffer = new byte[size];
				randomizer.nextBytes(buffer);
				return buffer;
			},0);
		});
		
		System.out.println ("Finished thread");
 	}
	
	@Test
	public void fifoParallelTest () throws InterruptedException
	{
		final int maxSize = 10000;
		
		final var cache = new MiniCache<Integer, byte[]>(CachePolicy.EVICTION_FIFO)
	              .setMaxSize(120);
		
		var latch = new CountDownLatch(5);
		
		for (int i=0;i<5;i++) {
			new Thread(() -> { this.getAndAddElements(cache, maxSize*5); latch.countDown(); }).start();
		}
				
		latch.await();

		assertEquals(120, cache.size());
	}
}
