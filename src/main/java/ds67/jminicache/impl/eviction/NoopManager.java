package ds67.jminicache.impl.eviction;

import java.util.Map;
import java.util.function.BiFunction;

import ds67.jminicache.impl.payload.PayloadIF;
import ds67.jminicache.impl.payload.PlainPayload;

/**
 * Simplest possible eviction manager. It doesn't provide an eviction at all. All cached entries stay valid until they
 * are removed manually. 
 * 
 * As this eviction manager does nothing no additional wrapping of the payload is necessary. 
 * 
 * @author jens
 *
 * @param <Key>
 * @param <Value>
 */
public class NoopManager<Key, Value> implements EvictionManagerIF<Key, Value, PayloadIF<Key, Value>> {

	private final BiFunction<Key, Value, PayloadIF<Key, Value>> constructor;
	
	public NoopManager(BiFunction<Key, Value, PayloadIF<Key, Value>> constructor) {
		this.constructor=constructor;
	}
	
	@Override
	public  PayloadIF<Key, Value> createWrapper(Key k, Value v) {
		return constructor.apply(k, v);
	}

	@Override
	public PlainPayload<Key, Value> getForDeletion() {
		return null;
	}

	@Override
	public void onRead(Map<Key, PayloadIF<Key, Value>> cache, PayloadIF<Key, Value> w) {	
	}

	@Override
	public void onBeforeWrite(Map<Key, PayloadIF<Key, Value>> cache, PayloadIF<Key, Value> w) {
	}

	@Override
	public void onDeletion(Map<Key, PayloadIF<Key, Value>> cache, PayloadIF<Key, Value> w) {
	}

	@Override
	public void onClear ()
	{
	}
	
}
