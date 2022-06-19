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
	 * Can be added to the existing eviction policies. When set expiry dates of the values are recognized and a value is removed from cache wehn the
	 * expiry date is reached. However, you may also have unlimited valid entries.
	 * 
	 *  If this policy is not set upon creation the expiry is not used.
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
