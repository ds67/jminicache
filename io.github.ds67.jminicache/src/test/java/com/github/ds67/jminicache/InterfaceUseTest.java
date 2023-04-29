package com.github.ds67.jminicache;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class InterfaceUseTest {

	@Test
	public void createWithoutPolicies() 
	{
		var cache = new MiniCacheBuilder<Integer, Integer>().build();
	}

	
	
}
