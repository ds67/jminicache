# jminicache
Small but fast memory cache of arbitrary objects


# Usage

You must create an object of `ds.67.jmininicache.MiniCache`. 

The created cache object is fully mulithreaded. Read operations are done in parallel, for writes the full cache access is shorty blocked. 

Then consider the upate and the removal policies.

## Update policies

There are different update policies. Choose the appropriate methods by your needs. Different provider methods can be combined.

Consider an object cache backed by an database. You may periodically refresh all currently known keys in a batch but retrieve data for new keys immediately from the
database backend.

### Simple updates

Use the `MiniCache.set (final Object key, final Value value)` method to add or refresh a single value to the cache.

### Batch updates

Add a large number of objects to the cache at once. 

### Fetch and store upon non existance

When its not clear if the requested key value is found in the cache you can use the method `MiniCache.get(final Object key, Supplier<Value> supplier)`.
When the requested key was not found in the cache storage the supplier method is called to create the content. It is guranteed that the supplier is only called once
even when several parallel requests for the missing key arrive before value retrieval. Furthermore the cache is not blocked when executing the supplier function,
therefore it may take time.

	final var cache = new MiniCache<Value>();
	Value v = cache.get ("test", () -> {
	   Value v = new Value();
	   // do some expensive operations to generate the value
	   return v;
	});

## Removal policies

To limit the grow of a cache values might be removed

1.  if the cache reached a maximum size, either in number of elements or size of the cache value objects. This is the only removal policy whre the cache decides
    which element to remove: Either the oldest element by creation time or the least recently used (LRU) object
    
    To do this add either `CachePolicy.EVICTION_LRU` or `CachePolicy.EVICTION_FIFO` to the constructor parameters.
    
2.  if a memory shortage occur and the values are garbage collected. Which objects are removed is decided by the garbage collector. 
3.  a value is outdated. That means that a generated value is cache for a certain timespan and than removed 
4.  When the key is not longer available. This mode is useful when the cache stored additional information for an object. The cache content is automatically
    removed when the referenced object is unused.

You may combine the different removal policies.