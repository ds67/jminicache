package com.github.ds67.jminicache.impl.eviction;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.ds67.jminicache.impl.payload.ListWrapper;
import com.github.ds67.jminicache.impl.payload.PayloadIF;

public class FIFOManager<Key, Value, Wrapper extends ListWrapper<Key,Value, ? extends PayloadIF<Key, Value>>> extends ListEvictionManager<Key, Value, Wrapper> implements EvictionManagerIF<Key, Value, Wrapper> {

	public FIFOManager(final BiFunction<Key, Value, Wrapper> constructor,
			final Function<Wrapper, Value> unWrapper) 
	{
		super(constructor, unWrapper);
	}

	@Override
	public void onRead(final Map<Key, Wrapper> cache, final Wrapper w) {
	}

	@Override
	public void onWrite(final Map<Key, Wrapper> cache, final Wrapper newWrapper, final Wrapper oldWrapper) {
		if (oldWrapper!=null) {
			oldWrapper.onRemove();
		}
		append(newWrapper);
	}

	@Override
	public void onDeletion(final Map<Key, Wrapper> cache, final Wrapper w) {
		delete (w);
	}

	@Override
	public Key getForDeletion() {
		return getFirstEntry().getKey();
	}
	
	@Override
	public void onClear ()
	{
		clear();
	}

}
