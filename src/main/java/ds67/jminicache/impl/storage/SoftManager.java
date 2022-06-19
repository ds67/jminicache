package ds67.jminicache.impl.storage;

import java.lang.ref.ReferenceQueue;
import java.util.Set;

import ds67.jminicache.impl.guard.GuardIF;
import ds67.jminicache.impl.payload.PayloadIF;

public class SoftManager<Key, Value, Wrapper extends PayloadIF<Key, Value>> implements StorageManagerIF<Key, Value>{

	private final StorageManagerIF<Key, Value> wrappedCacheManager;
	
	public SoftManager(final StorageManagerIF<Key, Value> wrapped, Constructor<Key, Value, Wrapper> constructor) {
		this.constructor=constructor;
		this.wrappedCacheManager=wrapped;
	}

	@Override
	public GuardIF getGuard ()
	{
		return wrappedCacheManager.getGuard();		
	}

	@Override
	public PayloadIF<Key, Value> getForDeletion() {
		return wrappedCacheManager.getForDeletion();
	}

	@Override
	public int cachesize ()
	{
		return wrappedCacheManager.cachesize();
	}

	@Override
	public PayloadIF<Key, Value> get (final Key key)
	{ 
		return wrappedCacheManager.get(key); 
	}
	
	@Override
	public PayloadIF<Key, Value> remove (Key key)
	{
		return wrappedCacheManager.remove(key);
	}

	@Override
	public void onRead (PayloadIF<Key, Value> w) {
		wrappedCacheManager.onRead(w);		
	}

	@Override
	public void onBeforeWrite(PayloadIF<Key, Value> w) {
		wrappedCacheManager.onBeforeWrite(w);		
	}

	@Override
	public void onDeletion(PayloadIF<Key, Value> w) {
		wrappedCacheManager.onDeletion(w);	
	}

	private final ReferenceQueue<Value> referenceQueue = new ReferenceQueue<>();
	
	public ReferenceQueue<Value> getReferenceQueue ()
	{
		return referenceQueue;
	}
	
	@SuppressWarnings("unchecked")
	protected void cleanup ()
	{
		PayloadIF<Key, Value> removed = null;
		while ((removed=(PayloadIF<Key, Value>)referenceQueue.poll())!=null) {
			wrappedCacheManager.remove(removed.getKey());
		}
	}
	
	@FunctionalInterface
	public interface Constructor<Key, Value, Wrapper>
	{
		public Wrapper apply (Key k, Value v, ReferenceQueue<Value> q);
	}
	
	private final Constructor<Key, Value, Wrapper> constructor;
	
	@Override
	public PayloadIF<Key, Value> createWrapper(Key k, Value v) {
		return constructor.apply(k,v,getReferenceQueue());
	}

	@Override
	public PayloadIF<Key, Value> put(Key key, PayloadIF<Key, Value> value) {
		return wrappedCacheManager.put(key, value);
	}

	@Override
	public boolean contains (Key key)
	{
		return wrappedCacheManager.contains(key);
	}
	
	@Override 
	public void clear ()
	{
		wrappedCacheManager.clear();
	}
	
	 @Override
	 public Set<Key> keySet ()
	 {
		 return wrappedCacheManager.keySet();
	 }

}
