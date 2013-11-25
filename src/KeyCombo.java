import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class KeyCombo
{
	private String _keyname = "a^"; // unmatchable regular expression
	private NodeList _uscript = null;
	
	// Default constructor.
	public KeyCombo()
	{
	}
	
	// 
	public KeyCombo(String keyname, NodeList uscript)
	{
		this._keyname = "^(" + keyname + ")$";
		this._uscript = uscript;
	}
	
	public KeyCombo(Element keycomboNode)
	{
//		String uscript = "";
//		NodeList nodes = keycomboNode.getChildNodes();
//		for(int i = 0; i < nodes.getLength(); i++)
//		{
//			uscript = uscript + nodeToString(nodes.item(i)).trim() + "\n";
//		}
		
		this._keyname = "^" + keycomboNode.getAttribute("match").trim() + "$";
		// TODO: Make sure that _uscript actually has a list of nodes in it, and make sure that all of them are either <javascript> or <utopiascript> nodes. Throw a GameLoadException otherwise. 
		this._uscript = keycomboNode.getChildNodes();
	}
	
	public NodeList checkKey(String key)
	{
		Pattern p = Pattern.compile(this._keyname);
		Matcher m = p.matcher(key);
		if(m.find())
		{
			try
			{
				if(!m.group(1).equals(""))
				{
					return this._uscript;//.replace("{COMMAND PARAMETER}", m.group(1));
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
			return null;
		}
	}
	
	/**
	 * This function turns a node into a string. This will be useful for the purposes of turning the game into a savable XML file.
	 * @param node
	 * @return
	 */
	@SuppressWarnings("unused")
	private String nodeToString(Node node)
	{
		StringWriter sw = new StringWriter();
		try
		{
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		}
		catch
		(TransformerException te)
		{
			System.out.println("nodeToString Transformer Exception");
		}
		return sw.toString();
	}
}