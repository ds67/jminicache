package com.github.ds67.jminicache;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Wrapper class to allow supply functions to return the expire date of the value
 * 
 * @author Jens Ketterer
 *
 * @param <Value> Type of the cached items. 
 */
public class ValueWithExpiry<Value>
{
	final private Value value;
	
	// defines a point in time when the value expired
	// when the value is 0 the value will never expire
	final private long expiry;

	/**
	 * Creates a new ValueWithExpiry object which never expires
	 * 
	 * @param <Value> Value type to wrap
	 * @param v value to wrap
	 * @return newly created object
	 */
	public static <Value> ValueWithExpiry<Value> of(Value v)
	{
		return new ValueWithExpiry<Value>(v);
	}
	
	/**
	 * Creates a new ValueWithExpiry object which expired at expireDate
	 * 
	 * @see System#currentTimeMillis()
	 * 
	 * @param <Value> Value type to wrap
	 * @param v value to wrap
	 * @param expireDate point in time in epoch milliseconds when the value will expire 
	 * 
	 * @return newly created object
	 */
	public static <Value> ValueWithExpiry<Value> of(Value v, long expireDate)
	{
		return new ValueWithExpiry<Value>(v, expireDate);
	}
	
	public static <Value> ValueWithExpiry<Value> of(Value v, long delay, TimeUnit unit)
	{
		return new ValueWithExpiry<Value>(v, System.currentTimeMillis()+unit.toMillis(delay));
	}

	public static <Value> ValueWithExpiry<Value> of(Value v, LocalDateTime expiryDate)
	{
		return new ValueWithExpiry<Value>(v, expiryDate==null?0:expiryDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
	}
	
	public ValueWithExpiry (Value value)
	{
		this.value=value;
		this.expiry=0;
	}

	public ValueWithExpiry (Value value, long expiry)
	{
		this.value=value;
		this.expiry=expiry;
	}

	public Value getValue() {
		return value;
	}

	public long getExpiry() {
		return expiry;
	}
	
	private static class FunctionWrapper<Key,Value> implements Function<Key,ValueWithExpiry<Value>>
	{
		final Function<Key,Value> wrapped; 
		
		FunctionWrapper (Function<Key,Value> wrapped)
		{
			this.wrapped=wrapped;
		}
		
		@Override
		public ValueWithExpiry<Value> apply(Key t) {
			return new ValueWithExpiry<>(wrapped.apply(t));
		}
		
		public Function<Key,Value> getWrappedFunction ()
		{
			return wrapped;
		}
	};
	
	public static <Key, Value> Function<Key, ValueWithExpiry<Value>> wrap (Function<Key,Value> functor)
	{
		return new FunctionWrapper<>(functor);
	}
	
	public static <Key, Value> Function<Key,Value> unwrap (Function<Key,ValueWithExpiry<Value>> wrappedFunctor) 
	{
		if (wrappedFunctor instanceof FunctionWrapper) {
			return ((FunctionWrapper<Key,Value>)wrappedFunctor).getWrappedFunction();
		}
		return null;
	}
}