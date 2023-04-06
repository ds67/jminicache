/**
 * This package hosts the basic storage implementation of the cache. This means that 
 * it provides a storage structure to quickly extract a (cached) value for a key.
 * 
 * It knows nothing about eviction strategies.This is done in the 
 * {@link com.github.ds67.jminicache.impl.eviction} package.
 * 
 * All available storage managers implement the interface 
 * {@link com.github.ds67.jminicache.impl.storage.StorageManagerIF}
 * 
 * @see com.github.ds67.jminicache.impl.storage.MapBasedCacheManager
 * @see com.github.ds67.jminicache.impl.storage.SoftManager
 * 
 */
package com.github.ds67.jminicache.impl.storage;
