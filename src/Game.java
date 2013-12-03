import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/*
 * The global Game-class contains all information related to the current 
 */

public class Game {

	public static boolean displayScore = false;	// Controls whether or not the player's score is displayed
	public static int maxScore = 0;				// If set to anything other than 0, will not show " of <MAX>" when displaying the score.
	public static String gameName;
	public static Room[][] rooms;				// HUGE variable; contains all of the rooms
	public static String[] items;
	public static KeyCombo[] keys;
	
	public static int playerX;
	public static int playerY;
	public static double score;
	public static int[] inventory;
	
	private static boolean isValid = false;
	
	public static void resetState()
	{
		score = 0;
		isValid = true;
	}
	
	public static boolean saveState(String filename)
	{
		UtopiaScript.Print("Saving...");
		if (!isValid) return false;
		
		try
		{
			FileWriter out = new FileWriter(filename);
			BufferedWriter bw = new BufferedWriter(out);
			
			bw.write(gameName);
			bw.newLine();
			
			bw.write(Integer.toString(playerX));
			bw.newLine();
			
			bw.write(Integer.toString(playerY));
			bw.newLine();
			
			bw.write(Integer.toString((int)score));
			bw.newLine();
			
			bw.write(Integer.toString(inventory.length));
			bw.newLine();
			
			for (int i=0; i<inventory.length; i++)
			{
				bw.write(Integer.toString(inventory[i]));
				bw.newLine();	
			}
			
			for (Room[] row : rooms)
			{
				for(Room cell : row)
				{
					bw.write(Integer.toString(cell.getRoomstate()));
					bw.newLine();	
				}
			}
			
			bw.close();
			out.close();
			UtopiaScript.Print("Done.");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	public static boolean loadState(String filename)
	{
		return loadState(filename, false);
	}
	
	public static boolean loadState(String filename, boolean isSequel)
	{
		String fn = filename;
		if (!fileExists(fn))
		{
			fn = filename + ".ues";
			if (!fileExists(fn))
			{
				if (!isSequel)
				{
					UtopiaScript.Print("Save does not exist.");
				}
				return false;
			}
		}
	
		try
		{
			FileReader in = new FileReader(fn);
			BufferedReader br = new BufferedReader(in);
			
			String saveGameName = br.readLine();
			
			if (!isSequel && !saveGameName.equals(gameName))
			{
				//error Games not match!
				UtopiaScript.Print("Save is for a different ");
				br.close();
				in.close();
				return false;
			}
			
			playerX = Integer.parseInt(br.readLine());
			playerY = Integer.parseInt(br.readLine());
			score = Integer.parseInt(br.readLine());
			int itemcount = Integer.parseInt(br.readLine());
			
			for (int i=0; i<itemcount; i++)
			{
				inventory[i] = Integer.parseInt(br.readLine());
			}
			
			if (!isSequel)
			{
				for (Room[] row : rooms)
				{
					for(Room cell : row)
					{
						cell.setRoomstate(Integer.parseInt(br.readLine()));
					}
				}
			}
			
			br.close();
			in.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		//load values from file
		isValid = true;
		
		return true;
	}
	
	private static boolean fileExists(String filename)
	{
		File f = new File(filename);
		return f.exists();
	}
	
	/*
	 * Determines whether or not the room located at (x,y) is travelable.
	 * Lots of necessary conditions for this to succeed:
	 		The new x coordinate must be >= 0
	 		The new y coordinate must be >= 0
	 		The new x coordinate must be < width of the _rooms array (_rooms.length)
	 		The new y coordinate must be < height of the _rooms array _rooms[x].length)
	 		The new room (_rooms[x][y]) must be travelable -- Room::canTravel()
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the overall success of the above conditions
	 */
	public static boolean canTravel(int x, int y)
	{
		return (x >= 0 && y >= 0 && x < rooms.length && y < rooms[x].length && rooms[x][y].canTravel());
	}
	
	public static void buildFromFile(String filename)
	{
		resetState();
		
		/**		BEGIN TEMPORARY VARIABLES	**/
		String name;
		String progress = "";
		String s_x;
		String s_y;
		String s_maxscore;
		String score_display;
		/**		END TEMPORARY VARIABLES		**/
		
		try
		{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(filename));
			
			doc.getDocumentElement().normalize();

			Element gameNode = (Element)doc.getDocumentElement();
			
			if(!gameNode.getNodeName().equalsIgnoreCase("game"))
			{
				throw new LoadGameException("Game node is not first node in file.");
			}
			
			gameNode = (Element)cleanNode((Node)gameNode);
			
			name = gameNode.getAttribute("name");
			s_x = gameNode.getAttribute("x");
			s_y = gameNode.getAttribute("y");
			s_maxscore = gameNode.getAttribute("maxscore");
			
			score_display = gameNode.getAttribute("score");
			if(score_display.equalsIgnoreCase("on") || score_display.equalsIgnoreCase("true"))
			{
				displayScore = true;
			}
			else
			{
				displayScore = false;
			}
			
			if(name.equals(""))
			{
				throw new LoadGameException("Name of game is not specified, or empty string.");
			}
			else
			{
				gameName = name;
			}
			
			try
			{
				progress = "x";
				if(s_x.equals(""))
				{
					playerX = 0;
				}
				else
				{
					playerX = Integer.parseInt(s_x);
				}
				
				progress = "y";
				if(s_y.equals(""))
				{
					playerY = 0;
				}
				else
				{
					playerY = Integer.parseInt(s_y);
				}
				
				progress = "maxscore";
				if(s_maxscore.equals(""))
				{
					maxScore = 0;
				}
				else
				{
					maxScore = Integer.parseInt(s_maxscore);
				}
			}
			catch(NumberFormatException e)
			{			
				throw new LoadGameException("Parameter \"" + progress + "\" on Game node is unparsable as an Integer.");
			}
			
			
			/**		<COMMANDS> PARSING		**/
			buildGameCommands(gameNode.getElementsByTagName("commands").item(0));
			
			
			/**		<ITEMS> PARSING			**/
			buildGameItems(gameNode.getElementsByTagName("items").item(0));
			
			
			/**		<ROOMS> PARSING			**/
			buildGameRooms(gameNode.getElementsByTagName("rooms").item(0));
			
			
		}
		catch (ParserConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Builds the game's _rooms array from the <rooms> node
	 * @param roomsNode the <rooms> node parsed from the XML
	 * @param width the width attribute from the <game> node
	 * @param height the height attribute from the <game> node
	 * @return Void; directly modifies the _rooms member
	 */
	private static void buildGameRooms(Node roomsNode)
	{
		int x;		// temporary variable
		int y;		// temporary variable
		int width;	// temporary variable
		int height;	// temporary variable
		String s_width;
		String s_height;
		String progress = "";

		s_width = ((Element)roomsNode).getAttribute("width");
		s_height = ((Element)roomsNode).getAttribute("height");
		try
		{
			progress = "width";
			if(s_width.equals(""))
			{
				throw new LoadGameException("Parameter width on Rooms node is not specified.");
			}
			width = Integer.parseInt(s_width);
			
			progress = "height";
			if(s_height.equals(""))
			{
				throw new LoadGameException("Parameter height on Rooms node is not specified.");
			}
			height = Integer.parseInt(s_height);
			
			if(playerX > width || playerY > height)
			{
				throw new LoadGameException("Starting coordinates are outside game boundaries.");
			}
		}
		catch(NumberFormatException e)
		{			
			throw new LoadGameException("Parameter `" + progress + "` on Rooms node is unparsable as Integer.");
		}
		
		rooms = new Room[width][height];
		// Instantiate each room with default constructor, to ensure that they are initialized
		for(int i = 0;i < width;i++)
		{
			for(int j = 0;j < height;j++)
			{
				rooms[i][j] = new Room();
			}
		}
		
		NodeList roomNodes = roomsNode.getChildNodes();
		
		int i = 0;
		int j = 0;
		for(int index = 0; index < roomNodes.getLength(); index++)
		{
			Node nRoom = roomNodes.item(index);
			if(!nRoom.getNodeName().equalsIgnoreCase("room"))
			{
				throw new LoadGameException("Non-room node found in the rooms node");
			}
			
			Element eRoom = (Element)nRoom;
			
			// x and y are unspecified on the room node; place it in the first available space.
			if(eRoom.getAttribute("x").equals("") && eRoom.getAttribute("y").equals(""))
			{
				// Find the next open slot in the rooms array.
				while(rooms[i][j].canTravel())
				{
					i++;
					if(i > width)
					{
						i = 0;
						j++;
					}
				}
				
				rooms[i][j] = new Room(nRoom);
				
				// Continue through the _rooms array
				i++;
				if(i > width)
				{
					i = 0;
					j++;
				}
			}
			// x and y are BOTH specified on the room node.
			else if(!eRoom.getAttribute("x").equals("") && !eRoom.getAttribute("y").equals(""))
			{
				try
				{
					x = Integer.parseInt(eRoom.getAttribute("x"));
					y = Integer.parseInt(eRoom.getAttribute("y"));
				}
				catch(NumberFormatException e)
				{
					throw new LoadGameException("Coordinates on room node " + index + " are unparsable as Integer.");
				}

				// Check to see if the coordinate specified already has a room in it.
				if(rooms[x][y].canTravel())
				{
					throw new LoadGameException("Two rooms specified for the same position: (" + x + "," + y + ")");
				}
				else
				{
					rooms[x][y] = new Room(nRoom);
				}
			}
			else
			{
				throw new LoadGameException("Either x or y is unspecified on room node " + index + ". Specify either both or neither.");
			}
		}
	}
	
	/**
	 * Builds the game's _globalkeys array from the <commands> node parsed from the XML
	 * @param commandsNode the <commands> node from the game XML
	 * @return Void; directly modifies the object's _globalkeys member
	 */
	private static void buildGameCommands(Node commandsNode)
	{
		int numkeys = 7;
		
		Element commands = (Element)commandsNode;
		
		Node helpNode = commands.getElementsByTagName("help").item(0);
		NodeList directionNodes = commands.getElementsByTagName("direction");
		NodeList globalKeys = ((Element)commands.getElementsByTagName("globalkeys").item(0)).getChildNodes();
		
		// N, E, S, and W.
		
		for(int i = 0; i < directionNodes.getLength(); i++)
		{
			if(!stringIn(((Element)directionNodes.item(i)).getAttribute("direction"), new String[]{"n", "e", "s", "w"}, false))
			{
				numkeys++;
			}
		}
		
		for(int i = 0;i < globalKeys.getLength(); i++)
		{
			if(globalKeys.item(i) != null)
			{
				numkeys++;
			}
		}
		
		keys = new KeyCombo[numkeys];
		
		for(int i = 0; i < numkeys; i++)
		{
			keys[i] = new KeyCombo();
		}
		
		keys[0] = new KeyCombo("(desc(ribe)?)|(look)|(see)", stringToNodeList("<utopiascript>description</utopiascript>"));
		
		keys[1] = new KeyCombo("inv(entory)?", stringToNodeList("<utopiascript>inventory</utopiascript>"));

		if(helpNode == null || helpNode.getNamespaceURI() == null)
		{
			keys[2] = new KeyCombo("help", stringToNodeList("<utopiascript>print To move between rooms, type MOVE or GO and a cardinal direction. To look at your inventory, type INV or INVENTORY. To get a description of the room you're in, type DESC or DESCRIPTION. To quit, type EXIT or QUIT. To save or load, type SAVE or LOAD.</utopiascript>"));
		}
		else
		{
			keys[2] = new KeyCombo((Element)helpNode);
		}

		keys[3] = new KeyCombo("((move )|(go ))?n(orth)?", stringToNodeList("<utopiascript>go +0/+1</utopiascript>"));
		keys[4] = new KeyCombo("((move )|(go ))?e(ast)?", stringToNodeList("<utopiascript>go +1/+0</utopiascript>"));
		keys[5] = new KeyCombo("((move )|(go ))?s(outh)?", stringToNodeList("<utopiascript>go -0/-1</utopiascript>"));
		keys[6] = new KeyCombo("((move )|(go ))?w(est)?", stringToNodeList("<utopiascript>go -1/-0</utopiascript>"));
		
		int global_key_index = 7;
		for(int i = 0; i < directionNodes.getLength(); i++)
		{
			Element directionCommand = (Element)directionNodes.item(i);
			
			String direction = directionCommand.getAttribute("direction");
			if(direction.equals("n"))
			{
				keys[3] = new KeyCombo("((move )|(go ))?n(orth)?", directionCommand.getChildNodes());
			}
			else if(direction.equals("e"))
			{
				keys[4] = new KeyCombo("((move )|(go ))?e(ast)?", directionCommand.getChildNodes());
			}
			else if(direction.equals("s"))
			{
				keys[5] = new KeyCombo("((move )|(go ))?s(outh)?", directionCommand.getChildNodes());
			}
			else if(direction.equals("w"))
			{
				keys[6] = new KeyCombo("((move )|(go ))?w(est)?", directionCommand.getChildNodes());
			}
			else if(!direction.equals(""))
			{
				keys[global_key_index] = new KeyCombo(direction, directionCommand.getChildNodes());
				global_key_index++;
			}
			else
			{
				throw new LoadGameException("Direction found without a direction name");
			}
		}
		
		for(int i = 0; i < globalKeys.getLength(); i++)
		{
			keys[global_key_index] = new KeyCombo((Element)globalKeys.item(i));
			global_key_index++;
		}
	}
	
	/**
	 * Builds the game's _itemnames and _itemquantities arrays from the <items> node
	 * @param itemsNode the <items> node in the game XML
	 * @return Void; directly modifies the _itemnames and _itemquantities members
	 */
	private static void buildGameItems(Node itemsNode)
	{
		String item_quantity;	// temporary variable
		
		NodeList itemNodes = itemsNode.getChildNodes();
		
		items = new String[itemNodes.getLength()];
		inventory = new int[itemNodes.getLength()];
		
		for(int i = 0;i < itemNodes.getLength(); i++)
		{
			Node itemNode = itemNodes.item(i);
			
			item_quantity = ((Element)itemNode).getAttribute("quantity");
			
			items[i] = itemNode.getTextContent().trim();
			try
			{
				if(item_quantity.equals(""))
				{
					inventory[i] = 0;
				}
				else
				{
					inventory[i] = Integer.parseInt(item_quantity);
				}
			}
			catch(NumberFormatException e)
			{			
				throw new LoadGameException("Parameter \"quantity\" on item node " + i + " is unparsable as Integer.");
			}
		}
	}
	
	private static NodeList stringToNodeList(String string)
	{
		if(string.length() > 0)
		{
			try
			{
				return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(string.getBytes())).getDocumentElement().getChildNodes();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private static Node cleanNode(Node node)
	{
		NodeList childNodes = node.getChildNodes();

		for(int n = childNodes.getLength() - 1; n >= 0; n--)
		{
			Node child = childNodes.item(n);
			short nodeType = child.getNodeType();

			if(nodeType == Node.ELEMENT_NODE)
			{
				cleanNode(child);
			}
			else if(nodeType == Node.TEXT_NODE)
			{
				String trimmedNodeVal = child.getNodeValue().trim();
				if(trimmedNodeVal.length() == 0)
				{
					node.removeChild(child);
				}
				else
				{
					child.setNodeValue(trimmedNodeVal);
				}
			}
			else if (nodeType == Node.COMMENT_NODE)
			{
				node.removeChild(child);
			}
		}
		return node;
	}
	
	private static boolean stringIn(String needle, String haystack[], boolean caseSensitive)
	{
	    for(int i = 0;i < haystack.length;i++)
	    {
	        if(caseSensitive)
	        {
	            if(needle.equals(haystack[i])) return true;
	        }
	        else
	        {
	            if(needle.equalsIgnoreCase(haystack[i])) return true;
	        }
	    }
	    return false;
	}
}