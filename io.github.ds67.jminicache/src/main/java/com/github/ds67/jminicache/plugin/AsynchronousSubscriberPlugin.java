package com.github.ds67.jminicache.plugin;

import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.Flow.Subscriber;

import com.github.ds67.jminicache.CacheChangeEvent;
import com.github.ds67.jminicache.CacheChangeEvent.Operations;

public class AsynchronousSubscriberPlugin<Key,Value> implements Plugin<Key,Value>
{
	private SubmissionPublisher<CacheChangeEvent<Key, Value>> publisher = new SubmissionPublisher<>();
	
	public AsynchronousSubscriberPlugin() {
	}

	public void subscribe (final Subscriber<? super CacheChangeEvent<Key, Value>> subscriber)
	{
		publisher.subscribe(subscriber);
	}
	
	@Override
	public void onBeforeFetch(Key k) {
	}

	@Override
	public void onAfterFetch(Key key, Value value) {
	}

	@Override
	public void onBeforeSet(Key k, Value value) {
	}

	@Override
	public void onAfterSet(Key key, Value oldValue, Value newValue) {
		publisher.submit (new CacheChangeEvent<Key, Value>(Operations.SET_VALUE, key, oldValue, newValue));		
	}

	@Override
	public void onBeforeRemove(Key key) {
	}

	@Override
	public void onAfterRemove(Key key, Value value) {
		publisher.submit (new CacheChangeEvent<Key, Value>(Operations.REMOVE_VALUE,key, value, null));
	}

	@Override
	public void onMiss(Key key) {
		publisher.submit(new CacheChangeEvent<Key, Value>(Operations.MISSED_VALUE, key, null, null));
	}

	@Override
	public void onValueCreateCollision(Key key) {
		publisher.submit(new CacheChangeEvent<Key, Value>(Operations.KEY_FETCH_COLLISION, key, null, null));
	}

	@Override
	public void onRefresh(Key key) {
		publisher.submit(new CacheChangeEvent<Key, Value>(Operations.REFRESH_VALUE, key, null, null));
	}

	@Override
	public void onShrink(Key key) {
		publisher.submit(new CacheChangeEvent<Key, Value>(Operations.SHRINK, key, null, null));
	}

	@Override
	public void onClear ()
	{
		publisher.submit(new CacheChangeEvent<Key, Value>(Operations.CLEAR, null, null, null));
	}

	@Override
	public void onBeforeGet(Key k) 
	{
	}

	@Override
	public void onAfterGet(Key key, Value value) 
	{
		publisher.submit (new CacheChangeEvent<Key, Value>(Operations.GET_VALUE, key, null, value));				
	}

	@Override
	public void onExpire(Key key) {
		publisher.submit (new CacheChangeEvent<Key, Value>(Operations.EXPIRED_VALUE, key, null, null));				
	}
}
