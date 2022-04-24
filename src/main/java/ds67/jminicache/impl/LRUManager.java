package ds67.jminicache.impl;

public class LRUManager<Key, Value> {

	public LRUManager() {
		// TODO Auto-generated constructor stub
	}

	private LRUWrapper<Key, Value> lastEntry = null;
	
	public void setLast (LRUWrapper<Key, Value> w)
	{
		if (lastEntry!=null) {
			lastEntry.setSucc(w);
			w.setPred(lastEntry);
		}
		lastEntry = w;
	}
	
	public LRUWrapper<Key, Value> getLast()
	{
		return lastEntry;
	}
	
	public void removeLastIf (final LRUWrapper<Key, Value> element)
	{
		if (lastEntry==element) {
			lastEntry=element.getPred();
		}
	}
}
