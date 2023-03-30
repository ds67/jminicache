package ds67.jminicache;

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
	 * Eviction policy: Removes the least recently used entry when necessary. Beware that this policy needs a single thread access even for read cache requests.
	 * This may slow done the cache access.
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
	 * Eviction policy: No entries will be removed in background.
	 */
	EVICTION_NONE(Category.EVICTION_POLICY),
	
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
	
	WEAK_KEYS(Category.KEY_HANDLING);
	
	public static enum Category
	{
		EVICTION_POLICY,
		ADDITIONAL_EVICTION_POLICY,
		KEY_HANDLING
	}
	
	private Category category;
	
	private CachePolicy (final Category c)
	{
		category = c;
	}
	
	public Category getCategory()
	{
		return category;
	}
	
}
