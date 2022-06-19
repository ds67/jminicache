package ds67.jminicache.impl.eviction;

import java.util.Map;
import java.util.function.BiFunction;

import ds67.jminicache.impl.payload.PlainPayload;

public class NoopManager<Key, Value> implements EvictionManagerIF<Key, Value, PlainPayload<Key, Value>> {

	private final BiFunction<Key, Value, PlainPayload<Key, Value>> constructor;
	
	public NoopManager(BiFunction<Key, Value, PlainPayload<Key, Value>> constructor) {
		this.constructor=constructor;
	}
	
	@Override
	public  PlainPayload<Key, Value> createWrapper(Key k, Value v) {
		return constructor.apply(k, v);
	}

	@Override
	public PlainPayload<Key, Value> getForDeletion() {
		return null;
	}

	@Override
	public void onRead(Map<Key, PlainPayload<Key, Value>> cache, PlainPayload<Key, Value> w) {	
	}

	@Override
	public void onBeforeWrite(Map<Key, PlainPayload<Key, Value>> cache, PlainPayload<Key, Value> w) {
	}

	@Override
	public void onDeletion(Map<Key, PlainPayload<Key, Value>> cache, PlainPayload<Key, Value> w) {
	}

	@Override
	public void onClear ()
	{
	}
	
}
