package com.github.ds67.jminicache.impl.payload;

public interface PayloadIF<Key, Value> {

	public void onRemove();

	public Value getPayload();

	public Key getKey();

}