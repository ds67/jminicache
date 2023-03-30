package com.github.ds67.jminicache.impl.payload;

public class KeyValuePayload<Key, Value> implements PayloadIF<Key, Value> {

	private Value payload = null;
	private final Key key;
	
	public KeyValuePayload(final Key key, final Value payload) 
	{
		this.payload=payload;
		this.key=key;
	}
	
	@Override
	public void onRemove ()
	{		
	}

	@Override
	public Value getPayload ()
	{
		return payload;
	}
	
	@Override
	public Key getKey() {
		return key;
	}
}
