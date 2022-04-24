package ds67.jminicache.impl;

public class LRUWrapper<Key, Payload> {

	private Payload payload = null;
	
	private LRUWrapper<Key, Payload> pred = null;
	private LRUWrapper<Key, Payload> succ = null;
	
	private final Key key;
	
	public LRUWrapper (Key key, Payload payload)
	{
		this.key = key;
		this.payload=payload;
	}
	
	public void removeHint ()
	{
		if (this.pred!=null) this.pred.succ=this.succ;
		if (this.succ!=null) this.succ.pred=this.pred;
	}
	
	public Payload getPayload ()
	{
		return payload;
	}
	
	public void setPayload (Payload payload)
	{
		this.payload=payload;
	}

	public LRUWrapper<Key, Payload> getPred() {
		return pred;
	}

	public void setPred(LRUWrapper<Key, Payload> pred) {
		this.pred = pred;
	}

	public LRUWrapper<Key, Payload> getSucc() {
		return succ;
	}

	public void setSucc(LRUWrapper<Key, Payload> succ) {
		this.succ = succ;
	}

	public Key getKey() {
		return key;
	}
	
}
