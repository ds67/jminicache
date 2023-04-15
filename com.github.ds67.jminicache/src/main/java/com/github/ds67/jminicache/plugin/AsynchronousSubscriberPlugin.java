package com.github.ds67.jminicache.plugin;

import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.Flow.Subscriber;

import com.github.ds67.jminicache.CacheChangeEvent;

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
	public void onBeforeGet(Key k) {
	}

	@Override
	public void onAfterGet(Key key, Value value) {
		
	}

	@Override
	public void onBeforeSet(Key k, Value value) {
	}

	@Override
	public void onAfterSet(Key key, Value oldValue, Value newValue) {
		publisher.submit (new CacheChangeEvent<Key, Value>(key, oldValue, newValue));		
	}

	@Override
	public void onBeforeRemove(Key key) {
	}

	@Override
	public void onAfterRemove(Key key, Value value) {
		publisher.submit (new CacheChangeEvent<Key, Value>(key, value, null));
	}

	@Override
	public void onMiss(Key key) {
	}

	@Override
	public void onValueCreateCollision(Key k) {
	}

	@Override
	public void onRefresh(Key key) {
	}

	@Override
	public void onShrink(Key key) {
	}

	@Override
	public void onClear ()
	{
		publisher.submit(new CacheChangeEvent<Key, Value>(null, null, null));
	}
}
