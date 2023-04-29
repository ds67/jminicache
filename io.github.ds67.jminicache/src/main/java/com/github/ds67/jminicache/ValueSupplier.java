package com.github.ds67.jminicache;

import java.util.function.Supplier;

/**
 * Interface similar to the {@link Supplier} interface of the java language with the extension to throw a exception
 * If no exceptions is needed its silently skipped. Unfortunately just one exception type can be returned.
 * 
 * However this enables your code to provide a supplier to the {@link MiniCache#get(Object, ValueSupplier)} method
 * with may throw an exception. The exception is than rethrown by the get method to allow used defined exception 
 * handling outside of the lambda context. 
 *  
 * @author jens
 *
 * @param <Value> Value type which is returned by the supplier 
 * @param <E> Exception to throw
 */
public interface ValueSupplier<Value, E extends Throwable>
{
	Value get() throws E;
}