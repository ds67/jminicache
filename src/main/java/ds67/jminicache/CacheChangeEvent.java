package ds67.jminicache;

public class CacheChangeEvent<Key, Value> {

	private final Key key;
	private final Value oldValue;
	private final Value newValue;
	
	public CacheChangeEvent(final Key key, final Value oldValue, final Value newValue) 
	{
		this.key = key;
		this.oldValue=oldValue;
		this.newValue=newValue;
	}

	public boolean isSet()
	{
		return newValue!=null;
	}
	
	public boolean isDelete ()
	{
		return oldValue!=null && newValue==null;
	}
	
	public boolean isUpdate ()
	{
		return oldValue!=null && newValue!=null;
	}
	
	public boolean isNew ()
	{
		return oldValue==null && newValue!=null;
	}

	public boolean isClear()
	{
		return key==null && oldValue==null && newValue==null;
	}
	
	public Key getKey() {
		return key;
	}

	public Value getOldValue() {
		return oldValue;
	}

	public Value getNewValue() {
		return newValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
		result = prime * result + ((oldValue == null) ? 0 : oldValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CacheChangeEvent other = (CacheChangeEvent) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (newValue == null) {
			if (other.newValue != null)
				return false;
		} else if (!newValue.equals(other.newValue))
			return false;
		if (oldValue == null) {
			if (other.oldValue != null)
				return false;
		} else if (!oldValue.equals(other.oldValue))
			return false;
		return true;
	}
}
