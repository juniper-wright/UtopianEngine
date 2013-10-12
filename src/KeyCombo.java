class KeyCombo
{
	private String _keyname;
	private String _uscript;
	
	// Default constructor. Never used.
	public KeyCombo()
	{
		this._keyname = "DUMMY_KEY";
		this._uscript = "";
	}
	
	// 
	public KeyCombo(String keyname, String uscript)
	{
		this._keyname = keyname;
		this._uscript = uscript;
	}
	
	public String getResults(String key)
	{
		if(this._keyname.equalsIgnoreCase(key))
		{
			return this._uscript;
		}
		else
		{
			return "";
		}
	}
}