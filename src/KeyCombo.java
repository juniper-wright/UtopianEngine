import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class KeyCombo
{
	public String _keyname = "a^"; // unmatchable regular expression
	private String _uscript = "";
	
	// Default constructor.
	public KeyCombo()
	{
	}
	
	// 
	public KeyCombo(String keyname, String uscript)
	{
		this._keyname = "^" + keyname + "$";
		this._uscript = uscript;
	}
	
	public KeyCombo(Node keycomboNode)
	{
		// Call the KeyCombo(String, String) constructor with the "match" attribute as keyname and contents of the node as uscript
		System.out.println(((Element)keycomboNode).getAttribute("match").trim() + ",==");
		System.out.println(((Element)keycomboNode).getTextContent().trim() + "<==");
//		this ( ((Element)keycomboNode).getAttribute("match").trim(),
//				((Element)keycomboNode).getTextContent().trim() );
	}
	
	public String checkKey(String key)
	{
		System.out.println("Comparing `" + key + "` to `" + this._keyname + "`");
		Pattern p = Pattern.compile(this._keyname);
		Matcher m = p.matcher(key);
		if(m.find())
		{
			try
			{
				if(!m.group(1).equals(""))
				{
					return this._uscript.replace("{COMMAND PARAMETER}", m.group(1));
				}
			}
			catch(Exception e)
			{
				// Deliberately empty.
			}
			return this._uscript;
		}
		else
		{
			return "";
		}
	}
}