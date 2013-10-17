public class Roomstate
{
	boolean _seen = false;
	String _description;		// This is the long description for this roomstate
	String _shortDescription;	// This is the short description for this roomstate
	String[] _keyCombos;		// This is a list of key


	public Roomstate()
	{
		
	}
	
	public String description()
	{
		if (!this._seen)
		{
			this._seen = true;
			return this._description;
		}
		return this._shortDescription;
	}

	public String description(boolean check)
	{
		this._seen = true;
		return this._description;
	}
}