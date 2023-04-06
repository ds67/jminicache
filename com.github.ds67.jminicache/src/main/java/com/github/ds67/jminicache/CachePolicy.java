package com.github.ds67.jminicache;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Cache policies which define the behavior of the cache.  
 * 
 * Two categories of policies a provided. First the eviction policies which defines which entries will be removed from the cache when the maximum capacity 
 * is reached. 
 * 
 * @author Jens Ketterer
 *
 */
public enum CachePolicy {
	
	/**
	 * Eviction policy: Removes the least recently used entry when necessary. Beware that this policy needs a single thread access even for read cache requests
	 * as the LRU status must be recorded. This may slow done the cache access.
	 * 
	 * Choose this eviction policy when the entries to not age and the probability of a new access to such an item does not increase over time. That you have a risk
	 * of trashing the cache where you always remove the entries with the highest probability for the next access.  
	 */
	EVICTION_LRU(Category.EVICTION_POLICY),
	
	/**
	 * Eviction policy: Removes the oldest entry by insertion time.
	 * 
	 * Choose this policy when entries get outdated and the chance increases that a new value was created for a key.  
	 * 
	 */
	EVICTION_FIFO(Category.EVICTION_POLICY),
	
	/**
	 * Eviction policy: No entries will be removed in background. Use this policy when you either have a limit set of entries which do never expire.
	 * Usage of the cache is then similar to a simple {@link Map}. However, reads to the cache are done in parallel and will only
	 * queue when write requests occur.
	 * 
	 * Or use it with
	 */
	EVICTION_NONE(Category.EVICTION_POLICY, true),
	
	/**
	 * Can be added to the existing eviction policies. When values have an expire date these are recognized and 
	 * values are removed from the cache when they expire. 
	 * A cached item need not to have an expire date. Then it remains valid forever.
	 * 
	 * Using this additional policy will add O(n) complexity to the {@link MiniCache#set(Object, Object)} 
	 * and {@link MiniCache#remove(Object)} and similar methods. Furthermore it requires a installed scheduler
	 * which will schedule the expire operations.
	 * 
	 * When you install a{@link  MiniCache#setRefreshMethod(java.util.function.Function)} method the cached items
	 * are not removed but instead refreshed.
	 * 
	 * @see MiniCache#setSchedulerService(java.util.concurrent.ScheduledExecutorService) 
	 * @see MiniCache#getSchedulerService()
	 * @see MiniCache#hasSchedulerService()
	 * 
	 * If this policy is not set upon creation the expire date is not used. Adding such a date to a set method has no effect.
	 */
	ENABLE_VALUE_EXPIRY(Category.ADDITIONAL_EVICTION_POLICY),
	
	/**
	 * 
	 */
	WEAK_KEYS(Category.KEY_HANDLING),
		
	/**
	 * Uses a hash base key value store (based on a {@link HashMap}). A hash map uses more memory than a tree map but has usually faster access.
	 * This is the default storage. 
	 */
	HASH_MAP_STORAGE(Category.MAP_STRUCTURE, true),
	
	/**
	 * Builds a key value store based on a {@link TreeMap}. A TreeMap has a guaranteed access time of O(n) time and uses less memory than the
	 * hash based tree storage. Therefore, use a tree map when memory consumption is an issue (many keys and small cached values).
	 */
	TREE_MAP_STORAGE(Category.MAP_STRUCTURE),
	
	/**
	 * When this policy is set the {@link MiniCache} constructor while throw a {@link IllegalArgumentException} when illegal policy combinations are used.
	 * 
	 * As illegal configuration is considered
	 * <ul>
	 * <li>Two different policies of the same category (only one is valid).<p> 
	 *     Without the THROW_ON_MISCONFIGURATION policy the second and all subsequent policies of the same category are ignored.
	 * </li>
	 * <li>The TREE_MAP_STORAGE without a key class which is {@link Comparable}.<p>
	 *     Without the THROW_ON_MISCONFIGURATION policy the configuration will fallback on a hash map.  
	 * </li>
	 * </ul>
	 */
	THROW_ON_MISCONFIGURATION(Category.TECHNICAL);
	
	public static enum Category
	{
		EVICTION_POLICY,
		ADDITIONAL_EVICTION_POLICY,
		KEY_HANDLING,
		MAP_STRUCTURE,
		TECHNICAL
	}
	
	private Category category;
	private boolean defaultPolicy;
	
	private boolean isDefaultPolicy()
	{
		return defaultPolicy;
	}
	
	private CachePolicy (final Category c)
	{
		category = c;
		defaultPolicy = false;
	}

	private CachePolicy (final Category c, boolean isDefault)
	{
		category = c;
		defaultPolicy = isDefault;
	}

	public Category getCategory()
	{
		return category;
	}
	
	public static class ParsedPolicies
	{
		private CachePolicy evictionPolicy;
		private CachePolicy treeStoragePolicy;
		private boolean useWeakKeys = false;
		private boolean useExpire = false;
		private boolean throwOnMisconfiguration = false;
		
		public boolean isThrowOnMisconfiguration() {
			return throwOnMisconfiguration;
		}

		public void setThrowOnMisconfiguration(boolean throwOnMisconfiguration) {
			this.throwOnMisconfiguration = throwOnMisconfiguration;
		}

		void setEvictionPolicy(CachePolicy evictionPolicy) {
			this.evictionPolicy = evictionPolicy;
		}

		void setTreeStoragePolicy(CachePolicy treeStoragePolicy) {
			this.treeStoragePolicy = treeStoragePolicy;
		}

		void setUseWeakKeys(boolean useWeakKeys) {
			this.useWeakKeys = useWeakKeys;
		}

		void setUseExpire(boolean useExpire) {
			this.useExpire = useExpire;
		}

		public CachePolicy getEvictionPolicy() {
			return evictionPolicy;
		}

		public CachePolicy getTreeStoragePolicy() {
			return treeStoragePolicy;
		}

		public boolean isUseWeakKeys() {
			return useWeakKeys;
		}

		public boolean isUseExpire() {
			return useExpire;
		}
	}

	private static class ParseContext
	{
		EnumSet<Category> parsedCategories = EnumSet.noneOf(Category.class);
	}
	
	static private void parsePolicy (ParseContext context, ParsedPolicies p, CachePolicy policy)
	{
		 switch (policy.category) {
		 case EVICTION_POLICY: 
			 if (context!=null && context.parsedCategories.contains(Category.EVICTION_POLICY)) {
				 if (!p.getEvictionPolicy().equals(policy) && p.isThrowOnMisconfiguration()) {
					 throw new IllegalArgumentException("Found at least two conflicting policies: "
				                 +p.getEvictionPolicy().toString()+" and "+policy.toString()+" in argument list");
				 }
			 }
			 else { 
				 p.setEvictionPolicy(policy);
				 if (context!=null) context.parsedCategories.add(Category.EVICTION_POLICY);
			 }
			 break;
		 case MAP_STRUCTURE:
			 if (context!=null && context.parsedCategories.contains(Category.MAP_STRUCTURE)) {
				 if (!p.getEvictionPolicy().equals(policy) && p.isThrowOnMisconfiguration()) {
					 throw new IllegalArgumentException("Found at least two conflicting policies: "
				                 +p.getEvictionPolicy().toString()+" and "+policy.toString()+" in argument list");
				 }
			 }
			 else { 
				 p.setTreeStoragePolicy(policy);
				 if (context!=null) context.parsedCategories.add(Category.MAP_STRUCTURE);
			 }
			 break;
		 case ADDITIONAL_EVICTION_POLICY:
			 if (policy.equals(CachePolicy.ENABLE_VALUE_EXPIRY)) {
				 p.setUseExpire(true);
			 }
			 break;
		 case KEY_HANDLING:
			 if (policy.equals(CachePolicy.WEAK_KEYS)) {
				 p.setUseWeakKeys(true);
			 }
			 break;
		 case TECHNICAL:
			 ; // nothing to do here
		 }					
	}
	
	public static ParsedPolicies parsePolicies (CachePolicy...cachePolicies)
	{
		ParsedPolicies p = new ParsedPolicies();
		
		// preparse technical policies
		for (var policy: cachePolicies) {
			switch (policy) {
				case THROW_ON_MISCONFIGURATION:
					p.setThrowOnMisconfiguration(true);
					break;
				default:
			}
		}
		
		// Prefill with defaults
		EnumSet.allOf(CachePolicy.class)
		  .forEach(policy -> {
			 if (policy.isDefaultPolicy()) {
				 parsePolicy(null, p, policy);
			 }
		});	
		
		// parse polices
		var context = new ParseContext();
		for (var policy: cachePolicies) {
			parsePolicy(context, p, policy);
		}
		
		return p;
	}
	
}
