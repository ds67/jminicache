package com.github.ds67.jminicache.impl.guard;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleLockGuard implements GuardIF {

	final private Lock guard = new ReentrantLock();
	
	public SimpleLockGuard() {
	}

	@Override
	public void lockRead() {
		guard.lock();
	}

	@Override
	public void lockWrite() {
		guard.lock();
	}

	@Override
	public void promoteLock() {
	}

	@Override
	public void yield ()
	{
	}

	@Override
	public void unlockRead() {
		guard.unlock();
	}

	@Override
	public void unlockWrite() {
		guard.unlock();
	}

}
