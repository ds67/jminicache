package com.github.ds67.jminicache;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper class to allow supply functions to return the expire date of the value
 * 
 * @author jens
 *
 * @param <Value>
 */
public class ValueWithExpiry<Value>
{
	final private Value value;
	final private long expiry;

	public static <Value> ValueWithExpiry<Value> of(Value v)
	{
		return new ValueWithExpiry<Value>(v);
	}

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
}