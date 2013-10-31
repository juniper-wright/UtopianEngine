import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Game
{
	public String _name;						// The name of the game.
	public int _x;							// The current x coordinate of the player
	public int _y;							// The current y coordinate of the player
	public int _endgame;						// The "endgame" state. If less than 100000, then the game continues
	public String[] _intro;					// List of intro phrases; will be output in a row, pausing while the player reads inbetween
													// Only outputs when the game begins
	public String[][] _endgames;				// Contains a list of endgame descriptions
	public String _in;						// Used to hold user's input and modify it (everything is toLower()'d)
	public String _out;						// Used to create and edit the output
	public boolean[] _invHave;				// Used to hold a list of items the player has
	public boolean[] _invHaveBackup;			// Used when outputting; makes sure nothing gets deleted by accident
	public String[] _invNames;				// Contains the names of the items available
	public String _helpMessage;				// Contains the help message to be displayed whenever the player types "help"
	public Room[][] _rooms;					// HUGE variable; contains all of the rooms
	public Scanner scanner = new Scanner(System.in);		// The scanner! I'm surprised I didn't name it scanly; I usually name my scanners scanly
	ScriptEngineManager mgr = new ScriptEngineManager();
	ScriptEngine js_engine = mgr.getEngineByName("js");
    Bindings js_binding = js_engine.getBindings(ScriptContext.ENGINE_SCOPE);
	Object score;

	public Game()	// Default constructor! Never used!
	{
	}

	// Constructor. Very simple one at that.
	// This function just passes through to buildGameFromFile(),
	// because that function needs to be able to rebuild the game if given the loadGame utopiaScript command 
	public Game(String filename)
	{
		buildGameFromFile(filename);
	}
	
	private void buildGameFromFile(String filename)
	{
		String name;
		String s_x;
		String s_y;
		String s_width;
		String s_height;
		int x;
		int y;
		int width;
		int height;
		String progress = "";
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
			s_width = gameNode.getAttribute("width");
			s_height = gameNode.getAttribute("height");
			
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
				_x = Integer.parseInt(s_x);
				
				progress = "y";
				if(s_y.equals(""))
				{
					_y = 0;
				}
				_y = Integer.parseInt(s_y);
				
				progress = "width";
				if(s_width.equals(""))
				{
					throw new LoadGameException("Parameter width on Game node is not specified.");
				}
				width = Integer.parseInt(s_width);
				
				progress = "height";
				if(s_height.equals(""))
				{
					throw new LoadGameException("Parameter height on Game node is not specified.");
				}
				height = Integer.parseInt(s_height);
				
				if(_x > width || _y > height)
				{
					throw new LoadGameException("Starting coordinates are outside game boundaries.");
				}
			}
			catch(NumberFormatException e)
			{			
				throw new LoadGameException("Parameter " + progress + " on Game node is unparsable as Integer.");
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
			
			// Add <commands> and <inventory> parsing
			
			Node roomsNode = doc.getElementsByTagName("rooms").item(0);
			
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
					
					_rooms[i][j] = new Room(eRoom);
					
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
						throw new LoadGameException("Coordinates on room node " + (index+1) + " are unparsable as Integer.");
					}

					// Check to see if the coordinate specified already has a room in it.
					if(_rooms[x][y].canTravel())
					{
						throw new LoadGameException("Two rooms specified for the same position: (" + x + "," + y + ")");
					}
				}
				else
				{
					throw new LoadGameException("Either x or y is unspecified on room node " + (index+1));
				}
			}
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

	// Main function of the Game class.
	// The run function will continue to loop until the player hits an endgame or quits.
	public void run()
	{
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
	}

	public static int iterate(String in)		// This function represents a single time through the game loop.
	{
		if (_in.equals("help"))
		{
			System.out.print(_helpMessage);
		}
		else if ((_in.equals("quit")) || (_in.equals("exit")))		// if the player specifies they want to quit
		{
			System.out.print("Are you sure? Y/N\n\n> ");			// Make sure it was purposeful
			_in = scanner.nextLine().toLowerCase();
			while ((!_in.equals("y")) && (!_in.equals("yes")))
			{
				if ((_in.equals("n")) || (_in.equals("no")))
				{
					_in = "nvm";
					System.out.print("\n" + _rooms[_x][_y].description());
					break;
				}

				System.out.print("\nYes or no, please. If you're feeling lazy, you can shorten it to Y or N.\n\n> ");
				_in = scanner.nextLine().toLowerCase();
			}
	
			if (!_in.equals("nvm"))
			{
				return 2147483647;		// If they're sure, quick.
			}
		}
		else if ((_in.equals("inv")) || (_in.equals("inventory")))		// If the player wants to see their inventory,
		{
			outputInv();												// display the inventory
		}
		else if ((_in.equals("north")) || (_in.equals("move north")) || (_in.equals("n")) || (_in.equals("go north")))	// If move north,
		{
			moveNorth();																								// move north!
		}
		else if ((_in.equals("east")) || (_in.equals("move east")) || (_in.equals("e")) || (_in.equals("go east")))		// So on
		{
			moveEast();																									// And so forth
		}
		else if ((_in.equals("south")) || (_in.equals("move south")) || (_in.equals("s")) || (_in.equals("go south")))	// For each
		{
			moveSouth();																								// of the
		}
		else if ((_in.equals("west")) || (_in.equals("move west")) || (_in.equals("w")) || (_in.equals("go west")))		// cardinal
		{
			moveWest();																									// directions
		}
		else if (_in.equals("move"))							// "move" is not super helpful
		{
			System.out.print("Move which way?");
		}
		else if (_in.equals("go"))								// same with "go"
		{
			System.out.print("Go which way?");
		}
		else if ((_in.equals("desc")) || (_in.equals("describe")) || (_in.equals("description")) || (_in.equals("see")) || (_in.equals("inspect")))
		{
			System.out.print(_rooms[_x][_y].description(false));	// Used to output the description of the current room
		}
		else if (_in.equals("save"))
		{
			saveState();						// Save's player's game state
		}
		else if (_in.equals("load"))			// Load's the player's game state, then outputs the description
		{
			loadState();
			System.out.print(_rooms[_x][_y].description());
		}
		else									// If none of the special commands are met, then do the normal comparison:
		{
			int key = _rooms[_x][_y].getKey(_in);	// Check to see if the input matches a key
			if (key >= 0)							// If it does,
			{
				int roomstate = _rooms[_x][_y].getRoomstate();		// get the roomstate of the room
				if (checkInventory(roomstate, key) == true)			// Check to see if any inventorycheck conditions are met. If so,
				{
					System.out.print(_rooms[_x][_y].getEvent(roomstate, key));		// Print the corresponding event.
	
					updateInventory(roomstate, key);					// Update the inventory (if applicable)
		
					int roomstatefactor = _rooms[_x][_y].getRoomstateFactor(roomstate, key);	// update the roomstate
					_rooms[_x][_y].changeRoomstate(roomstatefactor);

					return roomstatefactor;		// return a random integer!
				}

				System.out.print("You don't have the right item(s) to do that.");
				return -2147483648;
			}

			System.out.print("You can't do that.");
		}

		return -2147483648;
	}

	// Calculates how many items the player has in their inventory
	static int howMany()
	{
		int howmany = 0;
		for (int i = 0; i < _invHave.length; i++)
		{
			if (_invHave[i] != true)
				continue;
				howmany++;
		}
		
		return howmany;
	}

	// Outputs the player's inventory
	public static void outputInv()
	{
		String finished = "You have in your inventory:\n\n";
		int count = 1;
		for (int i = 0; i < _invHave.length; i++)
		{
			if ((_invHave[i] == true) && (count < howMany()))
			{
				finished = finished + _invNames[i] + "\n";
				count++;
			}
			else
			{
				if (_invHave[i] != true)
					continue;
				finished = finished + _invNames[i];
				count++;
			}
		}
		if (finished.equals("You have in your inventory:\n\n"))
		{
			finished = "You don't have anything!";
		}
		System.out.print(finished);
	}

	// Makes sure the player has any items required for the given key
	// Removes items if so
	public static boolean checkInventory(int roomstate, int key)
	{
		InventoryCheck check = _rooms[_x][_y].getInventoryCheck(roomstate, key);
		check.checkNext();

		while (check.checkNext() == true)
		{
			int item = check.getNext();
			if (_invHave[item] == false)
			{
				return false;
			}
		}
		return true;
	}

	// Updates player's inventory to have any items given by the associated key
	public static void updateInventory(int roomstate, int key)
	{
		InventoryUpdate update = _rooms[_x][_y].getInventoryUpdate(roomstate, key);

		while (update.checkNext() == true)
		{
			int item = update.getNext();
			if (_invHave[item] != false)
			{
				_invHave[item] = false; continue;
			}

			_invHave[item] = true;
		}
	}

	// Moves the player north!
	// Uses a silly bit of modulus arithmetic to determine where the player is going
	public static void moveNorth()
	{
		int x = _rooms[_x][_y].checkNorth();

		if (x < 10000)
		{
			int y = x % 100;
			x = (x - x % 100) / 100;
			moveLocation(x, y);
			System.out.print(_rooms[_x][_y].description());
		}
		else if (x == 10200)
		{
			if (_y == 0)
			{
				System.out.print("You can't go that way.");
			}
			else
			{
				moveTranslate(0, -1);
				System.out.print(_rooms[_x][_y].description());
			}
		}
		else if (x == 10403)
		{
			System.out.print("You can't go that way.");
		}
	}
	
	// Moves the player east!
	// Uses a silly bit of modulus arithmetic to determine where the player is going
	public static void moveEast()
	{
		int y = _rooms[_x][_y].checkEast();
		if (y < 10000)
		{
			int x = (y - y % 100) / 100;
			y %= 100;
			moveLocation(x, y);
			System.out.print(_rooms[_x][_y].description());
		}
		else if (y == 10200)
		{
			if (_x == _rooms.length - 1)
			{
				System.out.print("You can't go that way.");
			}
			else
			{
				moveTranslate(1, 0);
				System.out.print(_rooms[_x][_y].description());
			}
		}
		else if (y == 10403)
		{
			System.out.print("You can't go that way.");
		}
	}

	// Moves the player south!
	// Uses a silly bit of modulus arithmetic to determine where the player is going
	public static void moveSouth()
	{
		int x = _rooms[_x][_y].checkSouth();

		if (x < 10000)
		{
			int y = x % 100;
			x = (x - x % 100) / 100;
			moveLocation(x, y);
			System.out.print(_rooms[_x][_y].description());
		}
		else if (x == 10200)
		{
			if (_y == _rooms[_x].length - 1)
			{
				System.out.print("You can't go that way.");
			}
			else
			{
				moveTranslate(0, 1);
				System.out.print(_rooms[_x][_y].description());
			}
		}
		else if (x == 10403)
		{
			System.out.print("You can't go that way.");
		}
	}

	// Moves the player west!
	// Uses a silly bit of modulus arithmetic to determine where the player is going
	public static void moveWest()
	{
		int y = _rooms[_x][_y].checkWest();
		if (y < 10000)
		{
			int x = (y - y % 100) / 100;
			y %= 100;
			moveLocation(x, y);
			System.out.print(_rooms[_x][_y].description());
		}
		else if (y == 10200)
		{
			if (_x == 0)
			{
				System.out.print("You can't go that way.");
			}
			else
			{
				moveTranslate(-1, 0);
				System.out.print(_rooms[_x][_y].description());
			}
		}
		else if (y == 10403)
		{
			System.out.print("You can't go that way.");
		}
	}

	// Saves the state of the game.
	public static void saveState()
	{
		System.out.print(getSaveData() + "Which savestate would you like to save over? Type \"cancel\" to cancel.\n\n> ");
		_in = scanner.nextLine();
		_in = _in.toLowerCase();
		while ((!_in.equals("1")) && (!_in.equals("2")) && (!_in.equals("3")) && (!_in.equals("state 1")) && (!_in.equals("state 2")) && (!_in.equals("state 3")) && (!_in.equals("cancel")))
		{
			System.out.print("\nInvalid input. Please try again.\n\n> ");
			_in = scanner.nextLine();
		}
		if (!_in.equals("cancel"))
		{
			if (!_in.equals(_in.split(" ")[0]))
			{
				_in = _in.split(" ")[1];
			}
			try
			{
				File file = new File("..\\" + _name + ".ug" + _in);
				FileWriter fileWriter = new FileWriter("..\\" + _name + ".ug" + _in);
				String savefile = "";
				System.out.print("\nPlease enter an identifying name for this savestate.\n\n> ");
				_in = scanner.nextLine();
				savefile = savefile + _in.toUpperCase();
				savefile = savefile + "QLNK";
				savefile = savefile + _x + "<" + _y + "QLNK";
				for (int i = 0; i < _invHave.length; i++)
				{
					if (_invHave[i] == true)
					{
						savefile = savefile + "76ANQ<";
					}
					else
					{
						savefile = savefile + "QNA67<";
					}
				}
				savefile = savefile.substring(0, savefile.length() - 1);
				savefile = savefile + "QLNK";
				for (int i = 0; i < _rooms.length; i++)
				{
					for (int j = 0; j < _rooms[i].length; j++)
					{
						savefile = savefile + _rooms[i][j].getRoomstate();
						savefile = savefile + "<";
					}
					savefile = savefile.substring(0, savefile.length() - 1);
					savefile = savefile + ">";
				}
				savefile = savefile.substring(0, savefile.length() - 1);
				fileWriter.write(savefile);
				fileWriter.close();
				System.out.print("\nGame successfully saved.");
			}
			catch (Exception e)
			{
				System.out.println("HERE #3");
				System.out.println(e.getMessage());			// Not really robust, because I haven't had the opportunity to test on various platforms
				System.out.println(e.getStackTrace());
			}
		}
		else
		{
			System.out.print("\nSave cancelled.");
		}
	}

	// Load's the player's state from a state file
	public static boolean loadState()
	{
		System.out.print(getSaveData() + "Which savestate would you like to load? Type \"cancel\" to cancel.\n\n> ");
		_in = scanner.nextLine();
		_in = _in.toLowerCase();
		while ((!_in.equals("1")) && (!_in.equals("2")) && (!_in.equals("3")) && (!_in.equals("state 1")) && (!_in.equals("state 2")) && (!_in.equals("state 3")) && (!_in.equals("cancel")))
		{
			System.out.print("\nInvalid input. Please try again.\n\n> ");
			_in = scanner.nextLine();
		}
		if (!_in.equals("cancel"))
		{
			if (!_in.equals(_in.split(" ")[0]))
			{
				_in = _in.split(" ")[1];
			}
			try
			{
				FileReader fr = new FileReader("..\\" + _name + ".ug" + _in);
				BufferedReader fileReader = new BufferedReader(fr);
				String[] savestate = { "", "", "", "" };
				String[] coordinates = { "", "" };
				String[] itemsHave = { "", "" };
				String[][] roomstates = { { "", "" }, { "", "" } };
				
				boolean[] invHave = new boolean[_invHave.length];
				int[][] rooms = new int[_rooms.length][_rooms[0].length];
				
				savestate[0] = fileReader.readLine();
				
				savestate = savestate[0].split("QLNK");
				try
				{
					savestate[3].equals(savestate[3]);
				}
				catch (Exception e)
				{
					throw new Exception("loadfail");
				}
				
				coordinates = savestate[1].split("<");
				int x = Integer.parseInt(coordinates[0]);
				int y = Integer.parseInt(coordinates[1]);
				
				itemsHave = savestate[2].split("<");
				for (int i = 0; i < itemsHave.length; i++)
				{
					if (itemsHave[i].equals("76ANQ"))
					{
						invHave[i] = true;
					}
					else if (itemsHave[i].equals("QNA67"))
					{
						invHave[i] = false;
					}
					else
					{
						throw new Exception("loadfail");
					}
					
				}
				int k = savestate[3].split(">").length;
				for (int i = 0; i < k; i++)
				{
					roomstates[i] = savestate[3].split(">")[i].split("<");
				}
				for (int i = 0; i < roomstates.length; i++)
				{
					for (int j = 0; j < roomstates[i].length; j++)
					{
						rooms[i][j] = Integer.parseInt(roomstates[i][j]);
					}
				}
				_x = x;
				_y = y;
				_invHave = invHave;
				for (int i = 0; i < _rooms.length; i++)
				{
					for (int j = 0; j < _rooms[i].length; j++)
					{
						_rooms[i][j].setRoomstate(rooms[i][j]);
					}
				}

				System.out.print("\nLoad State successful.\n\n");
				return true;
			}
			catch (Exception e)
			{
				System.out.print("\nLoad State failed.");
				return false;
			}
			
		}
		
		System.out.print("\nLoad cancelled.");
		return false;
	}

	// Deletes a savestate
	public static void deleteGame()
	{
		System.out.print(getSaveData() + "Which savestate would you like to delete? Type \"cancel\" to cancel.\n\n> ");
		_in = scanner.nextLine();
		_in = _in.toLowerCase();
		while ((!_in.equals("1")) && (!_in.equals("2")) && (!_in.equals("3")) && (!_in.equals("state 1")) && (!_in.equals("state 2")) && (!_in.equals("state 3")) && (!_in.equals("cancel")))
		{
			System.out.print("\nInvalid input. Please try again.\n\n> ");
			_in = scanner.nextLine();
		}
		if (!_in.equals("cancel"))
		{
			if (!_in.equals(_in.split(" ")[0]))
			{
				_in = _in.split(" ")[1];
			}
			try
			{
				File file = new File("..\\" + _name + ".ug" + _in);
				if (!file.exists())
				{
					throw new Exception("");
				}
				FileWriter fileWriter = new FileWriter(file);
				fileWriter.write("");
				fileWriter.close();
				System.out.print("\nState deleted.");
			}
			catch (Exception e)
			{
				System.out.print("\nState deletion failed.");
			}
		}
	}

	// Gets a list of available savestates
	public static String getSaveData()
	{
		String listOfGames = "";
		String[] savestate = { "", "", "", "" };

		for (int i = 1; i < 4; i++)
		{
			listOfGames = listOfGames + "State " + i + ": ";
			File f = new File("..\\" + _name + ".ug" + i);
			if (f.exists())
			{
				try
				{
					FileReader fr = new FileReader("..\\" + _name + ".ug" + i);
					BufferedReader fileReader = new BufferedReader(fr);
					savestate[0] = fileReader.readLine();
					savestate = savestate[0].split("QLNK");
					if (savestate[0].equals(savestate[0]))
					{
						listOfGames = listOfGames + savestate[0];
						listOfGames = listOfGames + "\n";
					}
				}
				catch (Exception e)
				{
					listOfGames = listOfGames + "Blank\n";
				}
			}
			else
			{
				listOfGames = listOfGames + "Blank\n";
			}
		}
		listOfGames = listOfGames + "\n";
		return listOfGames;
	}

	// Moves a specified number of rooms in each direction.
	public static void moveTranslate(int horiz, int vert)
	{
		_x += horiz;
		_y += vert;
	}

	// Moves to a given coordinate
	public static void moveLocation(int x, int y)
	{
		_x = x;
		_y = y;
	}

	// Pauses, waiting for the player to press enter
	public static void pause()
	{
		System.out.print("\n\nPress enter...");
		scanner.nextLine();
		System.out.println();
		return;
	}
	
	public static void pause(String prompt)
	{
		if(prompt.length() == 0)
		{
			pause();
		}
		else
		{
			System.out.print("\n\n" + prompt);
			scanner.nextLine();
			System.out.println();
		}
	}
	
	/**
	 * Runs the event gotten from a key.
	 * @String event, the combination JavaScript and UtopiaScript corresponding to the matched event.
	 */
	public void runEvent(String key, String event)
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
			runCommands(commands, uscript);
			updateScore();
		}
		catch(ScriptException e)
		{
			// TODO: Better exception-handling
	    	System.out.println(e.getMessage());
	    	System.out.println(e.getStackTrace());
		}
		catch(UtopiaException e)
		{
			// TODO: Better exception-handling
	    	System.out.println(e.getMessage());
	    	System.out.println(e.getStackTrace());
		}
		
	}
	
	private void runCommands(String[] commands, boolean[] uscript) throws ScriptException, UtopiaException
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
	
	private void updateScore() throws ScriptException
	{
		Double score = (Double) js_engine.eval("UtopiaScore;");//js_binding.get("UtopiaScore");
		try
		{
			System.out.printf("%.0f\n", Double.parseDouble(score.toString()));
		}
		catch(Exception e)
		{
			
		}
	}
	
	private boolean utopiaCommand(String command) throws UtopiaException
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
				usPause(args);
				return true;
			default:
				throw new UtopiaException("Command not found: " + function);
		}
	}

	public boolean usRequireItem(String args)
	{
		return true;
	}

	public boolean usAddItem(String args)
	{
		return true;
	}

	public boolean usTakeItem(String args)
	{
		return true;
	}

	public boolean usRoomstate(String args)
	{
		return true;
	}

	public boolean usGo(String args)
	{
		return true;
	}

	public boolean usGoto(String args)
	{
		return true;
	}

	public boolean usLoadGame(String args)
	{
		try
		{
			File fXmlFile = new File("staff.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
			
			Element gameNode = (Element)doc.getDocumentElement();
			
			System.out.println(gameNode.getAttribute("name"));
			
			NodeList nList = doc.getElementsByTagName("staff");
			
			System.out.println("----------------------------");
			
			for (int temp = 0; temp < nList.getLength(); temp++)
			{
				Node nNode = nList.item(temp);
				System.out.println("\nCurrent Element :" + nNode.getNodeName());
				if (nNode.getNodeType() == Node.ELEMENT_NODE)
				{
					Element eElement = (Element) nNode;
					System.out.println("Staff id : " + eElement.getAttribute("id"));
					System.out.println("First Name : " + eElement.getElementsByTagName("firstname").item(0).getTextContent());
					System.out.println("Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
					System.out.println("Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
					System.out.println("Salary : " + eElement.getElementsByTagName("salary").item(0).getTextContent());
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean usPause(String args)
	{
		pause(args);
		return true;
	}
	
	public boolean stringIn(String needle, String haystack[], boolean caseSensitive)
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