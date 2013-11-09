// Main function in this file

// Author: Ian McDevitt
// Title: The Utopian Engine
// Purpose: Text Adventure Game Engine
// Summary: This engine uses an object-oriented approach to text adventures, and uses a two-dimensional array of Rooms to represent discrete puzzles.
// When loading the game, the Engine will take in all text inside a .ueg file and parse it into a Game object.
// Within the game object are several variables. For their descriptions, see Game.java.
// In essence, the game is made of Rooms, arranged in a two-dimensional grid. The player is given a description of the room they are in,
// and then prompted for input. Once inputted, the text is sent to the appropriate room, and if that text matches a key in that room,
// then the appropriate event is output, and various things may happen. They are, in no particular order:
// * the player could expend an item
// * the player could gain an item
// * the player could win or lose (the Engine does not discriminate -- an endgame is an endgame, and in either event, the game quits. It is up
//		to the developer and the player to decide what is a win and what is a loss)
// * the player could advance or regress the state of the room - thereby changing the room

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UtopianEngine
{
	static Scanner scanner = new Scanner(System.in);

	static String _name;				// The name of the game.
	static int _x;						// The current x coordinate of the player
	static int _y;						// The current y coordinate of the player
	static String _key;					// Used to hold user's input and modify it (everything is toLower()'d)
	static String _out;					// Used to create and edit the output
	static Room[][] _rooms;				// HUGE variable; contains all of the rooms
	static KeyCombo[] _globalkeys;		// 
	static String[] _itemnames;			//
	static int[] _itemquantities;		//
	static ScriptEngineManager mgr = new ScriptEngineManager();
	static ScriptEngine js_engine = mgr.getEngineByName("js");
    static Bindings js_binding = js_engine.getBindings(ScriptContext.ENGINE_SCOPE);
	static Object score;
	static double _score;
	
	public static void main(String[] args)
	{
		if(args.length > 0)
		{
			buildGameFromFile(args[0]);
			if(args.length > 1)
			{
				usLoadState(args[1]);
			}
			run();
			return;
		}
		String instring = "";
		
		usPrint("Welcome to the Utopian Engine. Below you will find a list of games you have\nplaced in the appropriate folder. In order to play a game, simply type the name\nof the file.\n\n");
		
		printGameList();				// Outputs a list of available games.

		File f;
		do
		{
			instring = getKey();	// Gets input.
			if (instring.indexOf(".ueg") == -1)
			{
				instring = instring + ".ueg";
			}
			f = new File(instring);
			if(!f.exists())		// Makes sure the game exists
			{
				usPrintln("Sorry, game not found. Please try again.");
				if (instring.indexOf(".ueg") == -1)
				{
					instring = instring + ".ueg";
				}
			}
		}
		while(!f.exists());

		buildGameFromFile(instring);
		run();		// Runs game
	}

	/**
	 * Main function of the UtopianEngine class. Runs in a loop until the Game ends.
	 */
	private static void run()
	{
		try
		{
			while(true)
			{
				String event = "";
				_key = getKey();

				for(int i = 0; i < _globalkeys.length && "".equals(event); i++)
				{
					event = _globalkeys[i].checkKey(_key);
				}
				if("".equals(event))
				{
					event = _rooms[_x][_y].checkKeys(_key);
				}

				runEvent(_key, event);
			}
		}
		catch(GameEndException e)
		{
			if(_score != 0)
			{
				usPrint(String.format("%80s", "Score: " + _score));
			}
			usPrintln(e.getMessage());
		}
		return;
	}
	
	private static String getKey()
	{
		usPrint("\n> ");
		String key = scanner.nextLine().toLowerCase();
		usPrintln();
		return key;
	}
	
	private static void buildGameFromFile(String filename)
	{
		/**		BEGIN TEMPORARY VARIABLES	**/
		String name;
		String progress = "";
		String s_x;
		String s_y;
		/**		END TEMPORARY VARIABLES		**/
		
		try
		{
			File fXmlFile = new File(filename);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			
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
			
			if(name.equals(""))
			{
				throw new LoadGameException("Name of game is not specified, or empty string.");
			}
			
			try
			{
				progress = "x";
				if(s_x.equals(""))
				{
					_x = 0;
				}
				else
				{
					_x = Integer.parseInt(s_x);
				}
				
				progress = "y";
				if(s_y.equals(""))
				{
					_y = 0;
				}
				else
				{
					_y = Integer.parseInt(s_y);
				}
			}
			catch(NumberFormatException e)
			{			
				throw new LoadGameException("Parameter \"" + progress + "\" on Game node is unparsable as Integer.");
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
		
		_globalkeys = new KeyCombo[numkeys];
		
		for(int i = 0; i < numkeys; i++)
		{
			_globalkeys[i] = new KeyCombo();
		}
		
		_globalkeys[0] = new KeyCombo("THIS SHOULD BE DESCRIBE/LOOK/SEE/ETC; JIMINY CHRISTMAS", "");
		
		_globalkeys[1] = new KeyCombo("inv(entory)?", "<utopiascript>inventory;</utopiascript>");
		
		System.out.println(helpNode.getNamespaceURI());

		if(helpNode.getNamespaceURI() == null)
		{
			_globalkeys[2] = new KeyCombo("help", "<utopiascript>print To move between rooms, type MOVE or GO and a cardinal direction. To look at your inventory, type INV or INVENTORY. To get a description of the room you're in, type DESC or DESCRIPTION. To quit, type EXIT or QUIT. To save or load, type SAVE or LOAD.;</utopiascript>");
		}
		else
		{
			_globalkeys[2] = new KeyCombo(helpNode);
		}

		_globalkeys[3] = new KeyCombo("((move )|(go ))?n(orth)?", "<utopiascript>go +0/+1;</utopiascript>");
		_globalkeys[4] = new KeyCombo("((move )|(go ))?a(ast)?", "<utopiascript>go +1/+0;</utopiascript>");
		_globalkeys[5] = new KeyCombo("((move )|(go ))?s(outh)?", "<utopiascript>go -0/-1;</utopiascript>");
		_globalkeys[6] = new KeyCombo("((move )|(go ))?w(est)?", "<utopiascript>go -1/-0;</utopiascript>");
		
		int global_key_index = 7;
		for(int i = 0; i < directionNodes.getLength(); i++)
		{
			Element directionCommand = (Element)directionNodes.item(i);
			
			String direction = directionCommand.getAttribute("direction");
			if(direction.equals("n"))
			{
				_globalkeys[3] = new KeyCombo("((move )|(go ))?n(orth)?", directionCommand.getTextContent().trim());
			}
			else if(direction.equals("e"))
			{
				_globalkeys[4] = new KeyCombo("((move )|(go ))?a(ast)?", directionCommand.getTextContent().trim());
			}
			else if(direction.equals("s"))
			{
				_globalkeys[5] = new KeyCombo("((move )|(go ))?s(outh)?", directionCommand.getTextContent().trim());
			}
			else if(direction.equals("w"))
			{
				_globalkeys[6] = new KeyCombo("((move )|(go ))?w(est)?", directionCommand.getTextContent().trim());
			}
			else if(!direction.equals(""))
			{
				_globalkeys[global_key_index] = new KeyCombo(direction, directionCommand.getTextContent().trim());
				global_key_index++;
			}
			else
			{
				throw new LoadGameException("Direction found without a direction name");
			}
		}
		
		for(int i = 0; i < globalKeys.getLength(); i++)
		{
			_globalkeys[global_key_index] = new KeyCombo(globalKeys.item(i));
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
		
		_itemnames = new String[itemNodes.getLength()];
		_itemquantities = new int[itemNodes.getLength()];
		
		for(int i = 0;i < itemNodes.getLength(); i++)
		{
			Node itemNode = itemNodes.item(i);
			
			item_quantity = ((Element)itemNode).getAttribute("quantity");
			
			_itemnames[i] = itemNode.getTextContent().trim();
			try
			{
				if(item_quantity.equals(""))
				{
					_itemquantities[i] = 0;
				}
				else
				{
					_itemquantities[i] = Integer.parseInt(item_quantity);
				}
			}
			catch(NumberFormatException e)
			{			
				throw new LoadGameException("Parameter \"quantity\" on item node " + i + " is unparsable as Integer.");
			}
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
			
			if(_x > width || _y > height)
			{
				throw new LoadGameException("Starting coordinates are outside game boundaries.");
			}
		}
		catch(NumberFormatException e)
		{			
			throw new LoadGameException("Parameter `" + progress + "` on Rooms node is unparsable as Integer.");
		}
		
		_rooms = new Room[width][height];
		// Instantiate each room with default constructor, to ensure that they are initialized
		for(int i = 0;i < width;i++)
		{
			for(int j = 0;j < height;j++)
			{
				_rooms[i][j] = new Room();
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
				while(_rooms[i][j].canTravel())
				{
					i++;
					if(i > width)
					{
						i = 0;
						j++;
					}
				}
				
				_rooms[i][j] = new Room(nRoom);
				
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
				if(_rooms[x][y].canTravel())
				{
					throw new LoadGameException("Two rooms specified for the same position: (" + x + "," + y + ")");
				}
			}
			else
			{
				throw new LoadGameException("Either x or y is unspecified on room node " + index + ". Specify either both or neither.");
			}
		}

	}
	
	/**
	 * Pauses, waiting for the player to press enter
	 */
	private static void pause()
	{
		pause("Press enter...");
	}
	
	/**
	 * Pauses, waiting for the player to press enter
	 * @param prompt The prompt that 
	 */
	private static void pause(String prompt)
	{
		if("".equals(prompt))
		{
			pause();
		}
		else
		{
			usPrint("\n\n" + prompt);
			scanner.nextLine();
			usPrintln();
		}
	}
	
	/**
	 * Runs the event gotten from a key.
	 * @String event, the combination JavaScript and UtopiaScript corresponding to the matched event.
	 */
	private static void runEvent(String key, String event)
	{
		String[] events = event.split("((?<=<utopiaScript>)|(?=<utopiaScript>)|(?<=</utopiaScript>)|(?=</utopiaScript>))");
		int command_count = 0;
	
		for(int i = 0;i < events.length;i++)
		{
			if(!stringIn(events[i], new String[]{"<utopiascript>", "</utopiascript>"}, false))
			{
				command_count++;
			}
		}
		String[] commands = new String[command_count];
		boolean[] uscript = new boolean[command_count];
		int x = 0;
		boolean uscript_flag = false;
		for(int i = 0;i < events.length;i++)
		{
			if(!stringIn(events[i], new String[]{"<utopiascript>", "</utopiascript>"}, false))
			{
				commands[x] = events[i];
				uscript[x] = uscript_flag;
				x++;
			}
			else if(events[i].equalsIgnoreCase("<utopiascript>"))
			{
				uscript_flag = true;
			}
			else
			{
				uscript_flag = false;
			}
		}
		
		// Runs all of the commands in a loop. Placed in a function to allow premature ending if one of the commands fails.
		try
		{
			System.out.println(commands[0]);
			pushScore();
	    	js_engine.eval("key = '" + key + "';");
			runCommands(commands, uscript);
			pullScore();
		}
		catch(ScriptException e)
		{
			// TODO: Real exception-handling
	    	System.out.println(e.getMessage());
	    	System.out.println(e.getStackTrace());
		}
		catch(UtopiaException e)
		{
			// TODO: Real exception-handling
	    	System.out.println(e.getMessage());
	    	System.out.println(e.getStackTrace());
		}
		
	}
	
	private static void pushScore() throws ScriptException
	{
		js_engine.eval("var UtopiaScore = " + _score + ";");
	}
	
	private static void runCommands(String[] commands, boolean[] uscript) throws ScriptException
	{
		for(int i = 0;i < commands.length;i++)
		{
			if(uscript[i])
			{
				List<String> uscript_array = new ArrayList<String>();
				try
				{
					Pattern regex = Pattern.compile("(?:\\\\.|[^;\\\\]++)*");
					Matcher regexMatcher = regex.matcher(commands[i]);
					while (regexMatcher.find())
					{
						uscript_array.add(regexMatcher.group());
					}
				}
				catch(Exception e)
				{
					// TODO: Add exception handling. No clue what kind of exceptions are even thrown.
				}
				for(int j = 0;j < uscript_array.size();j++)
				{
					if(!utopiaCommand(uscript_array.get(j).trim()))
					{
						return;
					}
				}
			}
			else
			{
		    	js_engine.eval(commands[i]);
		    	pullScore();
			}
		}
	}
	
	private static void pullScore() throws ScriptException
	{
		Double score = (Double) js_engine.eval("UtopiaScore;");//js_binding.get("UtopiaScore");
		try
		{
			_score = Double.parseDouble(score.toString());
			//System.out.printf("%.0f\n", _score);
		}
		catch(Exception e)
		{
			throw new UtopiaException("FATAL ERROR: Unable to parse the UtopiaScore variable from JavaScript as a number. Value: `" + score + "`");
		}
	}
	
	private static boolean utopiaCommand(String command) throws UtopiaException
	{
		String arr[] = command.trim().split("[ ]+", 2);
		String function = arr[0].toLowerCase().trim();
		String args = (arr.length > 1 ? arr[1] : "");
		switch(function)
		{
			case "requireitem":
				return usRequireItem(args);
			case "additem":
				return usAddItem(args);
			case "takeitem":
				return usTakeItem(args);
			case "roomstate":
				return usRoomstate(args);
			case "go":
				return usGo(args);
			case "goto":
				return usGoto(args);
			case "loadgame":
				return usLoadGame(args);
			case "pause":
				return usPause(args);
			case "print":
				return usPrint(args);
			case "println":
				return usPrintln(args);
			case "description":
				return usDescription(args);
			case "score":
				return usScore(args);
			case "quitgame":
				return usQuitGame(args);
			case "inventory":
				return usInventory(args);
			case "savestate":
				return usSaveState(args);
			case "loadstate":
				return usLoadState(args);
			default:
				throw new UtopiaException(function + ": Command not found.");
		}
	}

	private static boolean usRequireItem(String args)
	{
		String[] args_arr = args.split(" ", 2);
		String itemNumString;
		int itemNum;
		String quantityString;
		int quantity;

		Pattern itemNumPattern = Pattern.compile("[^(]*");
		Matcher itemNumMatcher = itemNumPattern.matcher(args_arr[0]);
		itemNumString = itemNumMatcher.group();
		try
		{
			itemNum = Integer.parseInt(itemNumString);
		}
		catch(NumberFormatException e)
		{
			throw new UtopiaException("Invalid format for command requireItem: \"" + itemNumString + "\" is unparseable as an integer");
		}
		
		if(itemNum > _itemnames.length)
		{
			throw new UtopiaException("Invalid argument for command requireItem: \"" + itemNum + "\" is out of bounds for items list.");
		}
		
		if(args_arr[0].contains("(") && args_arr[0].contains(")"))
		{	
			Pattern quantityPattern = Pattern.compile("(?<=\\[)(.*?)(?=\\])");
			Matcher quantityMatcher = quantityPattern.matcher(args_arr[0]);
			quantityString = quantityMatcher.group(1);
			try
			{
				quantity = Integer.parseInt(itemNumString); 
			}
			catch(NumberFormatException e)
			{
				throw new UtopiaException("Invalid format for command requireItem: \"" + quantityString + "\" is unparseable as an integer.");
			}
		}
		else
		{
			quantity = 1;
		}
		
		if(_itemquantities[itemNum] < quantity)
		{
			if(args_arr.length > 1)
			{
				usPrintln(args_arr[1]);
			}
			return false;
		}
		else
		{
			return true;
		}
	}

	private static boolean usAddItem(String args)
	{
		String itemNumString;
		int itemNum;
		String quantityString;
		int quantity;

		Pattern itemNumPattern = Pattern.compile("[^(]*");
		Matcher itemNumMatcher = itemNumPattern.matcher(args);
		itemNumString = itemNumMatcher.group();
		try
		{
			itemNum = Integer.parseInt(itemNumString);
		}
		catch(NumberFormatException e)
		{
			throw new UtopiaException("Invalid format for command addItem: \"" + itemNumString + "\" is unparseable as an integer");
		}
		
		if(itemNum > _itemnames.length)
		{
			throw new UtopiaException("Invalid argument for command addItem: \"" + itemNum + "\" is out of bounds for items list.");
		}
		
		if(args.contains("(") && args.contains(")"))
		{	
			Pattern quantityPattern = Pattern.compile("(?<=\\[)(.*?)(?=\\])");
			Matcher quantityMatcher = quantityPattern.matcher(args);
			quantityString = quantityMatcher.group(1);
			try
			{
				quantity = Integer.parseInt(itemNumString); 
			}
			catch(NumberFormatException e)
			{
				throw new UtopiaException("Invalid format for command addItem: \"" + quantityString + "\" is unparseable as an integer.");
			}
		}
		else
		{
			quantity = 1;
		}
		
		_itemquantities[itemNum] += quantity;
		
		return true;
	}

	private static boolean usTakeItem(String args)
	{
		String[] args_arr = args.split(" ", 2);
		String itemNumString;
		int itemNum;
		String quantityString;
		int quantity;

		Pattern itemNumPattern = Pattern.compile("[^(]*");
		Matcher itemNumMatcher = itemNumPattern.matcher(args_arr[0]);
		itemNumString = itemNumMatcher.group();
		try
		{
			itemNum = Integer.parseInt(itemNumString);
		}
		catch(NumberFormatException e)
		{
			throw new UtopiaException("Invalid format for command takeItem: \"" + itemNumString + "\" is unparseable as an integer");
		}
		
		if(itemNum > _itemnames.length)
		{
			throw new UtopiaException("Invalid argument for command takeItem: \"" + itemNum + "\" is out of bounds for items list.");
		}
		
		if(args_arr[0].contains("(") && args_arr[0].contains(")"))
		{	
			Pattern quantityPattern = Pattern.compile("(?<=\\[)(.*?)(?=\\])");
			Matcher quantityMatcher = quantityPattern.matcher(args_arr[0]);
			quantityString = quantityMatcher.group(1);
			try
			{
				quantity = Integer.parseInt(itemNumString); 
			}
			catch(NumberFormatException e)
			{
				throw new UtopiaException("Invalid format for command takeItem: \"" + quantityString + "\" is unparseable as an integer.");
			}
		}
		else
		{
			quantity = 1;
		}
		
		if(_itemquantities[itemNum] < quantity && args_arr.length > 1)
		{
			usPrintln(args_arr[1]);
			return false;
		}
		else
		{
			_itemquantities[itemNum] -= Math.min(_itemquantities[itemNum], quantity);
			return true;
		}
	}

	private static boolean usRoomstate(String args)
	{
		try
		{
			if(args.matches("^=[0-9]{1,9}$"))
			{
				_rooms[_x][_y]._roomstate = Integer.parseInt(args.substring(1));
			}
			else
			{
				_rooms[_x][_y]._roomstate += Integer.parseInt(args);
			}
		}
		catch(NumberFormatException e)
		{
			throw new UtopiaException("Invalid format for Roomstate command. \"" + args + "\" is unparseable as an integer.");
		}
		return true;
	}

	private static boolean usGo(String args)
	{
		String[] args_arr = args.split("/| ", 2);
		try
		{
			_x += Integer.parseInt(args_arr[0]);
			_y += Integer.parseInt(args_arr[1]);
		}
		catch(NumberFormatException e)
		{
			throw new UtopiaException("Go command is formatted improperly. Arguments passed: " + args);
		}
		
		return usDescription("");
	}

	private static boolean usGoto(String args)
	{
		String[] args_arr = args.split("/| ", 2);

		try
		{
			_x = Integer.parseInt(args_arr[0]);
			_y = Integer.parseInt(args_arr[1]);
		}
		catch(NumberFormatException e)
		{
			throw new UtopiaException("GoTo command is formatted improperly. Arguments passed: " + args);
		}
		return usDescription("");
	}

	private static boolean usLoadGame(String args)
	{
		buildGameFromFile(args);
		return true;
	}
	
	private static boolean usPause(String args)
	{
		pause(args);
		return true;
	}

	// All system output will be done through the usPrint function. Thus, it will be easy to change if need be. 	
	private static boolean usPrint(String args)
	{
		System.out.print(args.replace("\\;", ";").replace("\\\\", "\\"));
		return true;
	}

	private static boolean usPrintln()
	{
		return usPrint("\n");
	}
	
	private static boolean usPrintln(String args)
	{
		return usPrint(args + "\n");
	}

	private static boolean usDescription(String args)
	{
		boolean longDesc = true;
		if(args.equalsIgnoreCase("short"))
		{
			longDesc = false;
		}
		return usPrintln(_rooms[_x][_y].description(longDesc));
	}
	
	private static boolean usScore(String args)
	{
		try
		{
			if(args.matches("^=[0-9]{1,9}$"))
			{
				_score = Integer.parseInt(args.substring(1));
			}
			else
			{
				_score += Integer.parseInt(args);
			}
		}
		catch(NumberFormatException e)
		{
			throw new UtopiaException("Invalid format for Score command. \"" + args + "\" is unparseable as an integer.");
		}
		
		try
		{
			pushScore();
		}
		catch(ScriptException e)
		{
			throw new UtopiaException(e.getMessage());
		}
		
		return true;
	}

	private static boolean usQuitGame(String args)
	{
		throw new GameEndException(args);
	}
	
	private static boolean usInventory(String args)
	{
		for(int i = 0;i < _itemnames.length; i++)
		{
			if(_itemquantities[i] > 0)
			{
				usPrintln(String.format("%-50sx%s", _itemnames[i], _itemquantities[i]));
			}
		}
		return usPrintln();
	}
	
	private static boolean usLoadState(String args)
	{
		return true;
	}
	
	private static boolean usSaveState(String args)
	{
		return true;
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

	private static void printGameList()
	{
		try
		{
			  // Directory path here
			  String path = "."; 
			 
			  String filename;
			  File folder = new File(path);
			  File[] listOfFiles = folder.listFiles(); 
			 
			  for (int i = 0; i < listOfFiles.length; i++) 
			  {
				  if (listOfFiles[i].isFile()) 
				  {
					  filename = listOfFiles[i].getName();
					  if (filename.toLowerCase().endsWith(".ueg"))
					  {
						  usPrintln(filename);
					  }
				  }
			  }
		}
		catch (Exception e)								// Generic exception handling
		{
			System.out.println("HERE #2");
			System.out.println(e.getMessage());			// Not really robust, because I haven't had the opportunity to test on various platforms
			System.out.println(e.getStackTrace());
		}
	}
}