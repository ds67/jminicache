package ds67.jminicache.impl.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ExpiryManager<Key> {

	private TreeMap<Long, Collection<Key>> expieries = new TreeMap<>();
	private HashMap<Key, Long> expireDatesToKeys = new HashMap<>(); 
	
	final private Consumer<Key> deletionTrigger;
	
	public ExpiryManager(final Consumer<Key> deletionTrigger) 
	{
		this.deletionTrigger=deletionTrigger;
	}
	
	public synchronized void add (final Key key, long expiry)
	{
		unsynchronized_remove(key);
		expieries.compute(expiry, (k,v) -> {
			if (v==null) v=new ArrayList<Key>();
			v.add(key);
			return v;
		});
		expireDatesToKeys.put(key, expiry);
		scheduleNextItem();
	}
	
	private void unsynchronized_remove (Key key)
	{
		Long expiry = expireDatesToKeys.get(key);
		if (expiry!=null) {
			expieries.compute(expiry, (k,v) -> {
				if (v!=null) {
					v.remove(key);
					if (v.isEmpty()) return null;
				}
				return v;
			});
			expireDatesToKeys.remove(key);
		}
	}
	
	public synchronized void remove (Key key)
	{
		unsynchronized_remove(key);
		scheduleNextItem();
	}
	
	/**
	 * Create a single scheduler thread for all cache instances. By using an own thread factory it is possible so set a descriptive name
	 * and the thread as a daemon thread to allow a graceful application shutdown
	 */
	private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
		       final Thread t = Executors.defaultThreadFactory().newThread(r);
               t.setDaemon(true);
               t.setName("Minicache Expiryscheduler");
               return t;
		}
	});
	private ScheduledFuture<?> nextExecution=null;
	private long nextExpireDate = 0;
	
	private void scheduleNextItem ()
	{
		if (expieries.isEmpty()) return;
		
		final long newNextExpireDate = expieries.firstKey();

		if (nextExpireDate!=newNextExpireDate) {
			if (nextExecution!=null) nextExecution.cancel(false);
			final long delay = nextExpireDate-System.currentTimeMillis();
			nextExecution = scheduler.schedule(this::deleteOldestEntry, delay, TimeUnit.MILLISECONDS);
			nextExpireDate=newNextExpireDate;
		}
	}
	
	private synchronized void deleteOldestEntry ()
	{
		boolean done = false;
		do {
			var oldestEntry = expieries.firstEntry();
			if (oldestEntry.getKey()<=System.currentTimeMillis()) {
				oldestEntry.getValue().forEach(deletionTrigger);
				expieries.remove(oldestEntry.getKey());
			}
			else { done = true; }
		}
		while (!done);
		nextExecution=null;
		scheduleNextItem();
	}
}
