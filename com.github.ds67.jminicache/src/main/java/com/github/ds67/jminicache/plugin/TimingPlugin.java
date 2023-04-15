package com.github.ds67.jminicache.plugin;

import java.util.Stack;

public class TimingPlugin<Key, Value> implements Plugin<Key,Value>{

	public TimingPlugin() {
	}

	private long getDuration = 0;
	private long gets = 0;
	private long longestGetDuration = -1;
	private long setDuration = 0;
	private long sets = 0;
	private long removalDuration = 0;
	private long removals = 0;
	
	private ThreadLocal<Stack<Long>> start = new ThreadLocal<>(){
		 @Override protected Stack<Long> initialValue() {
            return new Stack<Long>();
		 }
	};

	@Override
	public void onBeforeGet(Key k) {
		start.get().push(System.currentTimeMillis());
	}
	
	@Override
	public void onAfterGet(Key key, Value value) {
		final var duration = System.currentTimeMillis()-start.get().pop();
		synchronized (this) {
			++gets;
			getDuration+=duration;
			if (longestGetDuration==-1 || longestGetDuration<duration) longestGetDuration=duration;
		}
	}

	@Override
	public void onBeforeSet(Key k, Value value) {
		start.get().push(System.currentTimeMillis());
	}

	@Override
	public void onAfterSet(Key key, Value oldValue, Value newValue) {
		final var duration = System.currentTimeMillis()-start.get().pop();
		synchronized (this) {
			++sets;
			setDuration+=duration;
		}
	}

	@Override
	public void onBeforeRemove(Key key) {
		start.get().push(System.currentTimeMillis());	
	}

	@Override
	public void onAfterRemove(Key key, Value value) {
		final var duration = System.currentTimeMillis()-start.get().pop();
		synchronized (this) {
			++removals;
			removalDuration+=duration;
		}
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
	public void onClear() {
	}

}
