package com.github.ds67.jminicache.plugin;

import java.util.ArrayList;

public class PluginManager<Key,Value> implements Plugin<Key,Value> {

	public PluginManager() {
	}

	private ArrayList<Plugin<Key,Value>> plugins = null;
	
	public void addPlugin (Plugin<Key,Value> plugin)
	{
		if (plugins==null) {
			plugins=new ArrayList<>();
		}
		plugins.add(plugin);
	}
	
	public void removePlugin (Plugin<Key,Value> plugin)
	{
		if (plugins!=null) {
			plugins.remove(plugin);
		}
	}

	@Override
	public void onBeforeFetch(Key key) {
		if (plugins!=null) plugins.forEach(p -> p.onBeforeFetch(key));		
	}

	@Override
	public void onAfterFetch(Key key, Value value) {
		if (plugins!=null) plugins.forEach(p -> p.onAfterFetch(key, value));		
	}

	@Override
	public void onBeforeSet(Key key, Value value) {
		if (plugins!=null) plugins.forEach(p -> p.onBeforeSet(key,value));		
	}

	@Override
	public void onAfterSet(Key key, Value oldValue, Value newValue) {
		if (plugins!=null) plugins.forEach(p -> p.onAfterSet(key,oldValue,newValue));		
	}

	@Override
	public void onBeforeRemove(Key key) {
		if (plugins!=null) plugins.forEach(p -> p.onBeforeRemove(key));		
	}

	@Override
	public void onAfterRemove(Key key, Value value) {
		if (plugins!=null) plugins.forEach(p -> p.onAfterRemove(key,value));		
	}

	@Override
	public void onMiss(Key key) {
		if (plugins!=null) plugins.forEach(p -> p.onMiss(key));		
	}

	@Override
	public void onValueCreateCollision(Key key) {
		if (plugins!=null) plugins.forEach(p -> p.onValueCreateCollision(key));
	}

	@Override
	public void onRefresh(Key key) {
		if (plugins!=null) plugins.forEach(p -> p.onRefresh(key));		
	}

	@Override
	public void onShrink(Key key) {
		if (plugins!=null) plugins.forEach(p -> p.onShrink(key));		
	}

	@Override
	public void onClear() {
		if (plugins!=null) plugins.forEach(p -> p.onClear());		
	}

	@Override
	public void onBeforeGet(Key key) {
		if (plugins!=null) plugins.forEach(p -> p.onBeforeGet(key));		
	}

	@Override
	public void onAfterGet(Key key, Value value) {
		if (plugins!=null) plugins.forEach(p -> p.onAfterGet(key,value));		
	}
	
	@Override
	public void onExpire(Key key) {
		if (plugins!=null) plugins.forEach(p -> p.onExpire(key));		
	}
}
