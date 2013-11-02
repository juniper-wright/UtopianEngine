import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
		this._keyname = "^" + keyname + "$";
		this._uscript = uscript;
	}
	
	public KeyCombo(Node keycomboNode)
	{
		this(((Element)keycomboNode).getAttribute("match").trim(), ((Element)keycomboNode).getTextContent().trim());
	}
	
	public String getResults(String key)
	{
		// TODO: THIS ABSOLUTELY NEEDS TO BE FIXED TO USE REGULAR FUCKING EXPRESSIONS.
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