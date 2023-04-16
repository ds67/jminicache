package com.github.ds67.jminicache.impl.guard;

public class LocalGuard implements GuardIF
{

	private final GuardIF guard;
	
	private boolean readLock = false;
	private boolean writeLock = false;
	
	public LocalGuard(GuardIF guard) {
		this.guard=guard;
	}

	@Override
	public void lockRead() {
		unlockWrite();
		guard.lockRead();
		readLock=true;
	}

	@Override
	public void lockWrite() {
		unlockRead();
		guard.lockWrite();
		writeLock=true;
	}

	@Override
	public void yield() {
		guard.yield();		
	}

	public void unlock ()
	{
		unlockRead();
		unlockWrite();
	}

	@Override
	public void unlockRead() {
		if (readLock) {
			guard.unlockRead ();
			readLock=false;
		}
	}

	@Override
	public void unlockWrite() {
		if (writeLock) {
			guard.unlockWrite ();
			writeLock=false;
		}
	}

	@Override
	public boolean promoteLock() {
		final boolean r = guard.promoteLock();
		readLock = false;
		writeLock= true;
		return r;
	}
}
