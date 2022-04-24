package ds67.jminicache.impl;

import java.util.Arrays;
import java.util.EnumSet;

public enum CachePolicies {
	MaxEntries(Category.SizePolicy),
	MaxSize(Category.SizePolicy),
	LeastRecentlyUsed,
	TimeToLive;
	
	private static enum Category
	{
		SizePolicy
	}
	
	private EnumSet<Category> categories = null;
	
	private CachePolicies (final Category... c)
	{
		categories = EnumSet.copyOf(Arrays.asList(c));
	}
	
}
