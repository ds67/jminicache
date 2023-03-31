package com.github.ds67.jminicache.impl.payload;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class KeySoftValuePayload<Key, Value> extends SoftReference<Value> implements PayloadIF<Key, Value> {

	private final Key key;

	public KeySoftValuePayload(final Key key, final Value payload, final ReferenceQueue<Value> queue) 
	{
		super(payload,queue);
		this.key=key;
	}

	@Override
	public void onRemove ()
	{		
	}

	@Override
	public Value getPayload ()
	{
		return super.get();
	}

	@Override
	public Key getKey() {
		return key;
	}

}
