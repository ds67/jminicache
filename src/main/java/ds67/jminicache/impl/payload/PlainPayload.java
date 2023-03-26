package ds67.jminicache.impl.payload;

public class PlainPayload<Key, Payload> implements PayloadIF<Key, Payload> 
{
	private Payload payload;
	
	public PlainPayload(Payload payload) {
		this.payload=payload;
	}

	@Override
	public void onRemove() {
	}

	@Override
	public Payload getPayload() 
	{
		return this.payload;
	}

	@Override
	public Key getKey() {
		return null;
	}

}
