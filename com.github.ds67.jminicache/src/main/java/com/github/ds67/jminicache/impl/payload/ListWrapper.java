package com.github.ds67.jminicache.impl.payload;

public class ListWrapper<Key, Payload, Wrapper extends PayloadIF<Key, Payload>> implements PayloadIF<Key, Payload>
{
	private ListWrapper<Key, Payload, Wrapper> pred = null;
	private ListWrapper<Key, Payload, Wrapper> succ = null;
	private final Wrapper wrapper;
	
	public ListWrapper (final Wrapper wrapper)
	{
		this.wrapper=wrapper;
	}
	
	public void onRemove ()
	{
		if (this.pred!=null) this.pred.succ=this.succ;
		if (this.succ!=null) this.succ.pred=this.pred;
	}

	public ListWrapper<Key, Payload, Wrapper> getPred() {
		return pred;
	}

	@SuppressWarnings("unchecked")
	public void setPred(final ListWrapper<Key, Payload, ? extends PayloadIF<Key, Payload>> pred) {
		this.pred = (ListWrapper<Key, Payload, Wrapper>)pred;
	}

	public ListWrapper<Key, Payload, Wrapper> getSucc() {
		return succ;
	}
	
	@SuppressWarnings("unchecked")
	public void setSucc(ListWrapper<Key, Payload, ? extends PayloadIF<Key, Payload>> succ) {
		this.succ = (ListWrapper<Key, Payload, Wrapper>)succ;
	}

	@Override
	public Payload getPayload() {
		return wrapper.getPayload();
	}

	@Override
	public Key getKey() {
		return wrapper.getKey();
	}
	
}
