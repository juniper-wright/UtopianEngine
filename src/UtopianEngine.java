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
		}
		String instring = "";
		
		System.out.print("Welcome to the Utopian Engine. Below you will find a list of games you have\nplaced in the appropriate folder. In order to play a game, simply type the name\nof the file.\n\n");
		
		printGameList();				// Outputs a list of available games.
		System.out.print("\n> ");
		instring = scanner.nextLine();	// Gets input.
		if ((instring.equals("quit")) || (instring.equals("exit")))
		{
			System.out.println();
		}
		else
		{
			if (instring.indexOf(".ueg") == -1)
			{
				instring = instring + ".ueg";
			}
			File f = new File(instring);
			while (!f.exists())		// Makes sure the game exists
			{
				System.out.print("\nSorry, game not found. Please try again.\n\n> ");
				instring = scanner.nextLine();
				if (instring.indexOf(".ueg") == -1)
				{
					instring = instring + ".ueg";
				}
				f = new File(instring);
			}
//			Game game = loadGame(instring);		// Loads game
			run();		// Runs game
		}
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
	
	/**
	 * Builds the game's _globalkeys array from the <commands> node parsed from the XML
	 * @param commandsNode the <commands> node from the game XML
	 * @return Void; directly modifies the object's _globalkeys member
	 */
	private static void buildGameCommands(Node commandsNode)
	{
		Element commands = (Element)commandsNode;
		
		Node helpNode = commands.getElementsByTagName("help").item(0);
		NodeList directionNodes = commands.getElementsByTagName("direction");
		NodeList globalKeys = ((Element)commands.getElementsByTagName("globalkeys").item(0)).getChildNodes();
		
		// N, E, S, and W.
		int direction_list_length = 4;
		
		for(int i = 0;i < directionNodes.getLength();i++)
		{
			if(!stringIn(((Element)directionNodes.item(i)).getAttribute("direction"), new String[]{"n", "e", "s", "w"}, false))
			{
				direction_list_length++;
			}
		}
		
		_globalkeys = new KeyCombo[6+direction_list_length+globalKeys.getLength()];
		
		_globalkeys[0] = new KeyCombo("", "");
		
		_globalkeys[1] = new KeyCombo("inv(entory)?", "<utopiascript>inventory;</utopiascript>");
		
		if(helpNode == null)
		{
			_globalkeys[2] = new KeyCombo("^help$", "<utopiascript>print To move between rooms, type MOVE or GO and a cardinal direction. To look at your inventory, type INV or INVENTORY. To get a description of the room you're in, type DESC or DESCRIPTION. To quit, type EXIT or QUIT. To save or load, type SAVE or LOAD.;</utopiascript>");
		}
		else
		{
			_globalkeys[2] = new KeyCombo(helpNode);
		}

		_globalkeys[3] = new KeyCombo("(move )?n(orth)?", "<utopiascript>go +0/+1;</utopiascript>");
		_globalkeys[4] = new KeyCombo("(move )?e(ast)?", "<utopiascript>go +1/+0;</utopiascript>");
		_globalkeys[5] = new KeyCombo("(move )?s(outh)?", "<utopiascript>go -0/-1;</utopiascript>");
		_globalkeys[6] = new KeyCombo("(move )?w(est)?", "<utopiascript>go -1/-0;</utopiascript>");
		
		int global_key_index = 7;
		for(int i = 0; i < directionNodes.getLength(); i++)
		{
			Element directionCommand = (Element)directionNodes.item(i);
			
			String direction = directionCommand.getAttribute("direction");
			if(direction.equals("n"))
			{
				_globalkeys[3] = new KeyCombo(directionCommand);
			}
			else if(direction.equals("e"))
			{
				_globalkeys[4] = new KeyCombo(directionCommand);
			}
			else if(direction.equals("s"))
			{
				_globalkeys[5] = new KeyCombo(directionCommand);
			}
			else if(direction.equals("w"))
			{
				_globalkeys[6] = new KeyCombo(directionCommand);
			}
			else if(!direction.equals(""))
			{
				_globalkeys[global_key_index] = new KeyCombo(directionCommand);
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
			else if(!eRoom.getAttribute("x").equals("") && eRoom.getAttribute("y").equals(""))
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
	 * Main function of the UtopianEngine class. Runs in a loop until the Game ends.
	 */
	private static void run()
	{
		try
		{
			while(true)
			{
				String event = "";
				_key = scanner.nextLine().toLowerCase();
				
				for(int i = 0; i < _globalkeys.length && event.isEmpty(); i++)
				{
					event = _globalkeys[i].checkKey(_key);
				}
				if(event.isEmpty())
				{
					_rooms[_x][_x].checkKeys(_key);
				}
						
				runEvent(_key, event);
			}
		}
		catch(GameEndException e)
		{
			usPrintln(e.getMessage());
		}
		return;
/*		
		System.out.print("\nWelcome to " + this._name + "!");	// Welcomes the user to the game.
		do													// do-while loops usually aren't my thing, but it's a huge code block
		{      
			while (true)									// Probably not a good idea to use a while(true) with a break...
			{
				System.out.print("\n\nYou may play a New Game, Load Game, Delete Game, or Quit.\n\n> ");
				_in = scanner.nextLine().toLowerCase();		// toLowerCase() is very important, because EVERY key is lowercase.
															// A much better way to handle this is to use equalsIgnoreCase() when comparing strings.
				while ((!_in.equals("new game")) && (!_in.equals("new")) && (!_in.equals("load game")) && (!_in.equals("load")) && (!_in.equals("quit game")) && (!_in.equals("quit")) && (!_in.equals("delete game")) && (!_in.equals("delete")))
				{
					System.out.print("\nInvalid response. New Game, Load Game, or Quit?\n\n> ");
					_in = scanner.nextLine().toLowerCase();
				}
				
				// If the player wants to load their game, then load it.
				if ((_in.equals("load game")) || (_in.equals("load")))
				{
					System.out.print("\n");
					_endgame = 50000;
					if (!loadState())		// If there's no game to load, go back to the in-game menu.
					{
						_endgame = 2147483646;
					}
				}
				else if ((_in.equals("delete game")) || (_in.equals("delete")))
				{
					System.out.print("\n");
					deleteGame();
					_endgame = 2147483646;	// If they delete their game, go back to the in-game menu.
				}
				else if ((_in.equals("quit game")) || (_in.equals("quit")))
				{
					_endgame = 2147483647;	// If they quit, go back to the in-game menu, at which point the game will close.
				}
				else
				{
					_endgame = 0;
				}
	
				if (_endgame < 10000)		// First time in the game, game will output the intro
				{
					System.out.print("\n");
					for (int i = 0; i < _intro.length; i++)
					{
						System.out.print(_intro[i]);
						pause();
					}
					
				}
	
				if (_endgame < 100000)		// Then initialize the rooms
				{
					for (int i = 0; i < _rooms.length; i++)
					{
						for (int j = 0; j < _rooms[i].length; j++)
						{
							this._rooms[i][j].setRoomstate(0);
							this._rooms[i][j]._seen = false;
						}
					}
					
					_invHave = _invHaveBackup;
					System.out.print(_rooms[_x][_y].description() + "\n\n> ");
				}
				while (_endgame < 100000)									// MAIN GAME LOOP.
				{															// THIS LOOP WILL CONTINUE UNTIL THE PLAYER ENDS IT
					_in = scanner.nextLine().toLowerCase();					// BY HITTING AN ENDGAME OR BY QUITTING
					System.out.print("\n");
					_endgame = iterate(_in);
					if ((_endgame > 99999) && (_endgame != 2147483647))
					{
						_endgame -= 100000;
						break;
					}
					if (_endgame == 2147483647)
						continue;
					System.out.print("\n\n> ");
				}
				
				if ((_endgame == 2147483647) || (_endgame == 2147483646))
					break;
				System.out.print("\n\n");
				for (int i = 0; i < _endgames[_endgame].length; i++)
				{
					if (_endgames[_endgame][i].equals(""))
						continue;
					System.out.print(_endgames[_endgame][i]);
					pause();
				}
			}
		}
		while ((_endgame == 2147483646) || (_endgame != 2147483647));
	System.out.print("\n");
*/
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
		if(prompt.isEmpty())
		{
			pause();
		}
		else
		{
			usPrint("\n\n" + prompt);
			scanner.nextLine();
			usPrintln("");
		}
	}
	
	/**
	 * Runs the event gotten from a key.
	 * @String event, the combination JavaScript and UtopiaScript corresponding to the matched event.
	 */
	private static void runEvent(String key, String event)
	{
		js_engine.put("key", key);
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
			pushScore();
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
			}
		}
	}
	
	private static void pullScore() throws ScriptException
	{
		Double score = (Double) js_engine.eval("UtopiaScore;");//js_binding.get("UtopiaScore");
		try
		{
			_score = Double.parseDouble(score.toString());
			System.out.printf("%.0f\n", _score);
		}
		catch(Exception e)
		{
			throw new GameEndException("FATAL ERROR: Unable to parse the UtopiaScore variable from JavaScript as a number. Value: `" + score + "`");
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
			throw new UtopiaException("Invalid format for command requireItem: `" + itemNumString + "` is unparseable as an integer");
		}
		
		if(itemNum > _itemnames.length)
		{
			throw new UtopiaException("Invalid argument for command requireItem: `" + itemNum + "` is out of bounds for items list.");
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
				throw new UtopiaException("Invalid format for command requireItem: `" + quantityString + "` is unparseable as an integer.");
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
			throw new UtopiaException("Invalid format for command addItem: `" + itemNumString + "` is unparseable as an integer");
		}
		
		if(itemNum > _itemnames.length)
		{
			throw new UtopiaException("Invalid argument for command addItem: `" + itemNum + "` is out of bounds for items list.");
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
				throw new UtopiaException("Invalid format for command addItem: `" + quantityString + "` is unparseable as an integer.");
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
			throw new UtopiaException("Invalid format for command takeItem: `" + itemNumString + "` is unparseable as an integer");
		}
		
		if(itemNum > _itemnames.length)
		{
			throw new UtopiaException("Invalid argument for command takeItem: `" + itemNum + "` is out of bounds for items list.");
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
				throw new UtopiaException("Invalid format for command takeItem: `" + quantityString + "` is unparseable as an integer.");
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

	// All system output will be done through these two functions. Thus, it will be easy to change them if need be. 	
	private static boolean usPrint(String args)
	{
		System.out.print(args.replace("\\;", ";").replace("\\\\", "\\"));
		return true;
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
		return usPrintln("");
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
			String[] command = new String[4];
			int index = 0;
			command[0] = "cmd";
			command[1] = "/C";
			command[2] = "dir";
			command[3] = "*.ueg";
			Process p = Runtime.getRuntime().exec(command);
			// runs cmd to open a command prompt with /C option to close upon finishing
			// dir *.ueg returns a list of files with the *.ueg file extension

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String s = null;
			for (int i = 0; i < 5; i++)	// Skips the first five lines of the output (superfluous data from the command prompt)
			{
				s = stdInput.readLine();
			}
			while ((s = stdInput.readLine()) != null)	// until the end of the buffered reader
			{
				index = s.indexOf(".ueg");				// find ".ueg"
				if (index == -1)						// if it doesn't exist,
					continue;							// go to the next line
				for (int i = 1; i < s.length(); i++)	// otherwise, backtrack through the string
				{										// to the first whitespace character.
					if (s.charAt(index - i) != ' ')		// in other words, the beginning of the filename
						continue;
					s = s.substring(index - (i - 1));	// Get the filename
					System.out.println(s);				// Print the filename
					break;
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