package com.github.ds67.jminicache.impl.eviction;

import java.util.Map;

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
public class NoopManager<Key, Value> implements EvictionManagerIF<Key, Value, Value> {

	public NoopManager() {
	}
	
	@Override
	public Value createWrapper(Key k, Value v) {
		return v;
	}
	
	public Value unwrap (Value v) {
		return v;
	}

	@Override
	public Key getForDeletion() {
		return null;
	}

	@Override
	public void onRead(Map<Key, Value> cache, Value w) {	
	}

	@Override
	public void onBeforeWrite(Map<Key,Value> cache, Value w) {
	}

	@Override
	public void onDeletion(Map<Key, Value> cache, Value w) {
	}

	@Override
	public void onClear ()
	{
	}
	
}
