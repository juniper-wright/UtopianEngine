import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Roomstate
{
	private boolean _seen = false;
	private String _longDescription;	// This is the long description for this roomstate
	private String _shortDescription;	// This is the short description for this roomstate
	private KeyCombo[] _keyCombos;		// This is a list of keyCombos


	// Default constructor. Never used.
	public Roomstate()
	{
		this._longDescription = "";
		
		this._shortDescription = "";
		
		this._keyCombos = new KeyCombo[1];
		this._keyCombos[0] = new KeyCombo();
	}
	
	// Main constructor.
	public Roomstate(Node roomstateNode)
	{
		this._longDescription = ((Element)roomstateNode).getElementsByTagName("longdescription").item(0).getTextContent().trim();
				
		this._shortDescription = ((Element)roomstateNode).getElementsByTagName("shortdescription").item(0).getTextContent().trim();
		
		NodeList keyCombos = ((Element)roomstateNode).getElementsByTagName("key");
		
		this._keyCombos = new KeyCombo[keyCombos.getLength()];
		
		for(int i = 0; i < keyCombos.getLength(); i++)
		{
			this._keyCombos[i] = new KeyCombo((Element)keyCombos.item(i));
		}
	}

	public String description(boolean longDesc)
	{
		// If the long description has not been seen, or it is specifically requested, show that.
		// Otherwise, just show the short description.
		if(!this._seen || longDesc)
		{
			this._seen = true;
			return this._longDescription;
		}
		return this._shortDescription;
	}
	
	public NodeList checkKeys(String key)
	{
		NodeList script = null;
		for(int i = 0;i < _keyCombos.length && script == null; i++)
		{
			script = _keyCombos[i].checkKey(key);
		}
		return script;
	}
}