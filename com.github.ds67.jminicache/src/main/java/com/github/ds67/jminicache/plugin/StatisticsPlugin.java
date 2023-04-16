package com.github.ds67.jminicache.plugin;

import java.util.concurrent.atomic.LongAdder;

import com.github.ds67.jminicache.MiniCache;
import com.github.ds67.jminicache.Statistics;

public class StatisticsPlugin<Key,Value> implements Plugin<Key,Value>, Statistics {
		
	final private MiniCache<?,?> cache;
	private boolean isActive = true;
	
	public void setIsActive (boolean isActive)
	{
		this.isActive=isActive;
	}
	
	public boolean isActive ()
	{
		return isActive;
	}
	
	public StatisticsPlugin(MiniCache<?,?> cache) {
		this.cache=cache;
	}
	
	@Override
	public void reset ()
	{
		gets.reset();
		sets.reset();
		misses.reset();
		removals.reset();
		shrinks.reset();
		clears.reset();
		refreshes.reset();
		collisions.reset();
		expired.reset();
	}
	
	private LongAdder fetches = new LongAdder();
	private LongAdder gets = new LongAdder();
	private LongAdder sets = new LongAdder();
	private LongAdder misses = new LongAdder();
	private LongAdder removals = new LongAdder();
	private LongAdder shrinks = new LongAdder();
	private LongAdder clears = new LongAdder();
	private LongAdder refreshes = new LongAdder();
	private LongAdder collisions = new LongAdder();
	private LongAdder expired = new LongAdder();

	@Override
	public void onBeforeGet(Key k) {
	}

	@Override
	public void onAfterGet(Key key, Value value) {
		gets.increment();
	}

	@Override
	public void onBeforeFetch(Key k) {
	}

	@Override
	public void onAfterFetch(Key key, Value value) {
		fetches.increment();
	}

	@Override
	public void onBeforeSet(Key k, Value value) {
	}

	@Override
	public void onAfterSet(Key key, Value oldValue, Value newValue) {
		sets.increment();
	}

	@Override
	public void onBeforeRemove(Key key) {
	}

	@Override
	public void onAfterRemove(Key key, Value value) {
		removals.increment();
	}

	@Override
	public void onMiss(Key key) {
		misses.increment();
	}

	@Override
	public void onValueCreateCollision(Key k) {
		collisions.increment();
	}

	@Override
	public void onRefresh(Key key) {
		refreshes.increment();
	}

	@Override
	public void onShrink(Key key) {
		shrinks.increment();
	}

	@Override
	public void onClear() {
		clears.increment();
	}

	@Override
	public long getFetchCounter() {
		return fetches.sum();
	}
	
	@Override
	public long getGetCounter() {
		return gets.sum();
	}

	@Override
	public long getUpdateCounter() {
		return sets.sum();
	}

	@Override
	public long getMissesCounter() {
		return misses.sum();
	}

	@Override
	public long getClearCounter() {
		return clears.sum();
	}

	@Override
	public long getRefreshCounter() {
		return refreshes.sum();
	}

	@Override
	public long getRemovalCounter() {
		return removals.sum();
	}

	@Override
	public long getShrinkCounter() {
		return shrinks.sum();
	}

	@Override
	public long getCollisionCounter()
	{
		return collisions.sum();
	}
	
	@Override
	public long getExpiredCounter()
	{
		return expired.sum();
	}

	@Override
	public MiniCache<?, ?> getCache() {
		return cache;
	}
	
	@Override
	public void onExpire (Key key)
	{
		expired.increment();
	}
	
	@Override
	public String toString ()
	{
		return String.format("Gets:%d\nFetches:%d\nSets:%d\nMisses:%d\nRemovals:%d\nShrinks:%d\nCollisions:%d\nExpired:%d", 
				getGetCounter(),
				getFetchCounter(), 
				getUpdateCounter(), 
				getMissesCounter(), 
				getRemovalCounter(), 
				getShrinkCounter(),
				getCollisionCounter(),
				getExpiredCounter());
	}
}
