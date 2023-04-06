package com.github.ds67.jminicache.impl.eviction;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Simplest possible eviction manager. It doesn't provide an eviction at all. All cached entries stay valid until they
 * are removed manually. 
 * 
 * As this eviction manager does nothing no additional wrapping of the payload is necessary. 
 * 
 * @author jens
 *
 * @param <Key> Type of the key to access the cached items
 * @param <Value> Type of the cached item
 */
public class NoopManager<Key, Value, Wrapper> implements EvictionManagerIF<Key, Value, Wrapper> {

	private BiFunction<Key,Value,Wrapper> wrapper;
	private Function<Wrapper,Value> unWrapper;
	
	public NoopManager(BiFunction<Key,Value,Wrapper> wrapper, Function<Wrapper,Value> unWrapper) {
		this.wrapper=wrapper;
		this.unWrapper=unWrapper;
	}

	public static <Key,Value> NoopManager<Key, Value, Value> ofIdentity()
	{
		return new NoopManager<>((k,v) -> v, v -> v);
	}
		
	@Override
	public Wrapper createWrapper(Key k, Value v) {
		return wrapper.apply(k,v);
	}
	
	public Value unwrap (Wrapper v) {
		return unWrapper.apply(v);
	}

	@Override
	public Key getForDeletion() {
		return null;
	}

	@Override
	public void onRead(Map<Key, Wrapper> cache, Wrapper w) {	
	}

	@Override
	public void onWrite(Map<Key,Wrapper> cache, final Wrapper newWrapper, final Wrapper oldWrapper) {
	}

	@Override
	public void onDeletion(Map<Key, Wrapper> cache, Wrapper w) {
	}

	@Override
	public void onClear ()
	{
	}
	
}
