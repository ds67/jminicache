package com.github.ds67.jminicache.impl.guard;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteGuard implements GuardIF  {

	private final ReentrantReadWriteLock guard = new ReentrantReadWriteLock();
	
	public ReadWriteGuard() {
	}

	@Override
	public void lockRead() {
		guard.readLock().lock();
	}

	@Override
	public void lockWrite() {
		guard.writeLock().lock();		
	}

	@Override
	public boolean promoteLock() {
		guard.readLock().unlock();
		guard.writeLock().lock();
		return true;
	}

	@Override
	public void yield ()
	{
		if (guard.hasQueuedThreads()) {
			guard.writeLock().unlock();
			guard.writeLock().lock();
		}
	}

	@Override
	public void unlockRead() {
		guard.readLock().unlock();		
	}

	@Override
	public void unlockWrite() {
		guard.writeLock().unlock();
	}
	
}
