package ds67.jminicache.impl.guard;

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
	public void promoteLock() {
		guard.readLock().unlock();
		guard.writeLock().lock();
	}

	@Override
	public void unlock() {
		if (guard.isWriteLocked()) guard.writeLock().unlock();
		else guard.readLock().unlock();
	}

	@Override
	public void yield ()
	{
		if (guard.hasQueuedThreads()) {
			guard.writeLock().unlock();
			guard.writeLock().lock();
		}
	}
	
}
