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
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Scanner;

public class UtopianEngine
{
	static String[] longdescs;		// One-dimensional array of long descriptions of a room. Reused once per room
	static String[] shortdescs;		// One-dimensional array of short descriptions of a room. Reused once per room.
	static String[][] events;		// Two-dimensional array of events of a room. Reused once per room.
	static String[][] keys;			// Two-dimensional array of keys of a room. Reused once per room.
	static Scanner scanner = new Scanner(System.in);

	public static void printGameList()
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

	public static Game loadGame(String game)				// Easily the largest function in this project. This function loads a game from a .ueg file
	{														// once the user specifies a game to be loaded
		try
		{
			FileReader fr = new FileReader(game);				// Pull the game file into a file reader
			BufferedReader br = new BufferedReader(fr);		// Buffer said file reader

			game = "";										// Clear the "game" string, which previously specified the filename, for variable reuse.
			int linenumber = 0;
			String s;
			while ((s = br.readLine()) != null)				// While the bufferedreader is not empty
			{
			game = game + "\n" + s;							// Add each line to the game string
			}
			fr.close();										// Then close the file
			
			String[] gameparts = game.split("\n<>GAMEPARTSPLIT<>\n");		// Tokenize the file around the <>GAMEPARTSPLIT<> delimiter
			
			String name = gameparts[0];						// The first gamepart is the name of the game.
			name = name.split("\n")[1];						// Peel off the newline character. Using the split function was probably not the quickest solution
			
			int x = Integer.parseInt(gameparts[1]);			// x is the starting x coordinate of the player
			
			int y = Integer.parseInt(gameparts[2]);			// y is the starting y coordinate of the player
			
			String[] intro = getIntro(gameparts[3]);			// grab the variable-length introduction to the game
			
			String[][] endgames = getEndgames(gameparts[4]);	// grab the list of endgame names and their respective outputs
			
			boolean[] invHave = getInvHave(gameparts[5]);		// Grab the list of booleans determining starting items
			
			String[] invNames = getInvNames(gameparts[6]);	// Grab the list of names detailing what items are available
			
			String helpMessage = getHelpMessage(gameparts[7]);	// Grab the custom help message that is displayed when a player types "help", if any
			
			int width = Integer.parseInt(gameparts[8]);		// Grab the width of the array of rooms (allows for custom rectangular ratios
			
			Room[][] rooms = getRooms(gameparts[9], width);	// Extremely large function. Parses the room information into the two-dimensional array of rooms
			Game theGame = new Game(name, x, y, intro, endgames, invHave, invNames, helpMessage, rooms);
			return theGame;
		}
		catch (Exception e)
		{
			System.out.println("HERE #1");
			e.printStackTrace();
		}
		return new Game();
	}
	
	// Parses the various paragraphs of the intro from the intro section of the game file.
	public static String[] getIntro(String sourceString)
	{
		String[] intro = sourceString.split("<>GETINTROSPLIT<>\n");
		return intro;
	}

	// Parses the endgames and their respective indices from the endgame section of the game file
	public static String[][] getEndgames(String sourceString)
	{
		String[] endgamelist = sourceString.split("<>ENDGAMESPLIT1<>\n");
		String[][] endgames = new String[endgamelist.length][];
		for (int i = 0; i < endgamelist.length; i++)
		{
			endgames[i] = endgamelist[i].split("<>ENDGAMESPLIT2<>");
		}
		return endgames;
	}

	// Parses the list of starting items from the game file
	public static boolean[] getInvHave(String sourceString)
	{
		String[] invHaveList = sourceString.split("<>INVHAVESPLIT<>\n");
		boolean[] invHave = new boolean[invHaveList.length];
		for (int i = 0; i < invHaveList.length; i++)
		{
			if (invHaveList[i].equals("true"))
			{
				invHave[i] = true;
			}
			else
			{
				invHave[i] = false;
			}
		}
		return invHave;
	}

	// Parses the list of item names from the game file
	public static String[] getInvNames(String sourceString)
	{
		String[] invNames = sourceString.split("<>INVNAMESPLIT<>\n");
		return invNames;
	}

	// Parses the help message from the game file.
	// Returns a default message if developer did not specify one.
	public static String getHelpMessage(String sourceString)
	{
		if (sourceString.equals(" "))
		{
			return "To move between rooms, type MOVE or GO and a cardinal direction. To look at your inventory, type INV or INVENTORY. To get a description of the room you're in, type DESC or DESCRIPTION. To quit, type EXIT or QUIT. To save or load, type SAVE or LOAD.";
		}
		return sourceString;
	}

	// Parses the rooms from the game file.  This is a lot of data to parse
	public static Room[][] getRooms(String sourceString, int width)
	{
		String[] listOfRooms = sourceString.split("\n<>ROOMSPLIT<>\n");		// Splits into discrete rooms
		String mandatoryData = "";											// Declaring variables to be used. Mandatory data is data that is in every room
		String inventoryCheckData = "";										// Inventory check is not used in every room
		String inventoryUpdateData = "";									// Inventory update is not used in every room
		String moveDirectionData = "";										// Used for non-euclidean geometry. Not used in every room.
		boolean whichConstructor_Check = false;								// Three flags used to determine which constructor to use
		boolean whichConstructor_Update = false;							// Currently, there are eight.
		boolean whichConstructor_Move = false;								// There needs to be two.
		Room[][] rooms = new Room[width][(int)Math.ceil(listOfRooms.length / width)];
		for (int i = 0; i < listOfRooms.length; i++)			// For each room...
		{
			/*		Split room data into mandatory and each non-mandatory section		*/
		
			mandatoryData = listOfRooms[i].split("\n<>MANDATORYMARKER<>\n")[0];					// Grab mandatory data out of the room data
			listOfRooms[i] = listOfRooms[i].split("\n<>MANDATORYMARKER<>\n")[1];				// Remove that data from the room data
			
			inventoryCheckData = listOfRooms[i].split("\n<>INVENTORYCHECKMARKER1<>\n")[0];		// Grab inventorycheck data from the room data (if any)
			listOfRooms[i] = listOfRooms[i].split("\n<>INVENTORYCHECKMARKER1<>\n")[1];			// Remove that data from the room data
			
			inventoryUpdateData = listOfRooms[i].split("\n<>INVENTORYUPDATEMARKER1<>\n")[0];	// Grab inventoryupdate data from the room data (if any)
			listOfRooms[i] = listOfRooms[i].split("\n<>INVENTORYUPDATEMARKER1<>\n")[1];			// Remove that data from the room data
			
			moveDirectionData = listOfRooms[i];													// Grab non-euclidean move data from the room data (if any)
			listOfRooms[i] = "";																// Remove that data from the room data. All data has been removed
			
			/*		Begin splitting the mandatory data		*/
			
			String descriptionData = mandatoryData.split("\n<>DESCRIPTIONSMARKER1<>\n")[0];		// Grab description data
			mandatoryData = mandatoryData.split("\n<>DESCRIPTIONSMARKER1<>\n")[1];				// Remove that data from the mandatory data
			
			String[] descriptions = descriptionData.split("<>DESCRIPTIONSMARKER2<>\n");			// Split description data into an array
			
			String shortDescriptionData = mandatoryData.split("\n<>SHORTDESCRIPTIONSMARKER1<>\n")[0];	// Grab short description data
			mandatoryData = mandatoryData.split("\n<>SHORTDESCRIPTIONSMARKER1<>\n")[1];					// Remove that data from the mandatory data
			
			String[] shortDescriptions = shortDescriptionData.split("<>SHORTDESCRIPTIONSMARKER2<>");	// Split short description data into an array
			
			String keysData = mandatoryData.split("\n<>KEYSMARKER1<>\n")[0];					// Grab keys data
			mandatoryData = mandatoryData.split("\n<>KEYSMARKER1<>\n")[1];						// Remove that data from the mandatory data
			
			String[] keysList = keysData.split("<>KEYSMARKER2<>\n");							// Split key data into a one-dimensional array
			
			String[][] keys = new String[keysList.length][];									// Split key data into a two-dimensional array
			for (int j = 0; j < keysList.length; j++)
			{
				keys[j] = keysList[j].split("<>KEYSMARKER3<>");
			}
				
			String eventsData = mandatoryData.split("\n<>EVENTSMARKER1<>\n")[0];				// Grab events data
			mandatoryData = mandatoryData.split("\n<>EVENTSMARKER1<>\n")[1];					// Remove that data from the mandatory data
			
			String[] eventsList = eventsData.split("<>EVENTSMARKER2<>\n");						// Split event data into a one-dimensional array
			
			String[][] events = new String[eventsList.length][];								// Split event data into a two-dimensional array
			for (int j = 0; j < eventsList.length; j++)
			{
				events[j] = eventsList[j].split("<>EVENTSMARKER3<>");
			}
				
			String roomstateFactorData = mandatoryData.split("\n<>ROOMSTATEFACTORMARKER1<>\n")[0];		// Grab roomstate factor data
			mandatoryData = "";																			// Remove that data from the mandatory data. All data has been removed
			
			String[] roomstateFactorList = roomstateFactorData.split("\n<>ROOMSTATEFACTORMARKER3<>\n");	// Split roomstate factors into one-dimensional array
			
			String[][] roomstateFactorStrings = new String[roomstateFactorList.length][];				// Split roomstate factors into two-dimensional array
			for (int j = 0; j < roomstateFactorList.length; j++)
			{
				roomstateFactorStrings[j] = roomstateFactorList[j].split("<>ROOMSTATEFACTORMARKER2<>\n");
			}
			
			int[][] roomstateFactor = new int[roomstateFactorStrings.length][];
			for (int j = 0; j < roomstateFactor.length; j++)
			{
				roomstateFactor[j] = new int[roomstateFactorStrings[j].length];
				for (int k = 0; k < roomstateFactor[j].length; k++)
				{
					System.out.println(roomstateFactor[j].length);
					System.out.println(j + "   " + k);
					roomstateFactor[j][k] = Integer.parseInt(roomstateFactorStrings[j][k]);				// Parse roomstate factors into integers (can't add strings to ints!)
					System.out.println(roomstateFactor[j][k]);
				}
			}
			
			InventoryCheck[][] inventoryCheck = new InventoryCheck[eventsList.length][];			// In case inventory check data exists, parse that into individual strings
																									// Then into integers
			if (!inventoryCheckData.equals(""))
			{
				System.out.println("-->" + inventoryCheckData + "<--");
				whichConstructor_Check = true;
				
				String[] inventoryCheckList = inventoryCheckData.split("<>INVENTORYCHECKMARKER2<>");
				inventoryCheckData = "";
				
				String[][] inventoryCheckLists = new String[inventoryCheckList.length][];
				for (int j = 0; j < inventoryCheckList.length; j++)
				{
					inventoryCheckLists[j] = inventoryCheckList[j].split("<>INVENTORYCHECKMARKER3<>");
				}

				String[][][] inventoryCheckStrings = new String[inventoryCheckList.length][][];
				for (int j = 0; j < inventoryCheckLists.length; j++)
				{
					inventoryCheckStrings[j] = new String[inventoryCheckLists[j].length][];
					inventoryCheck[j] = new InventoryCheck[inventoryCheckLists[j].length];
					for (int k = 0; k < inventoryCheckLists[j].length; k++)
					{
						inventoryCheckStrings[j][k] = inventoryCheckLists[j][k].split("<>INVENTORYCHECKMARKER4<>");
					}
					
				}
				
				for (int j = 0; j < inventoryCheckStrings.length; j++)
				{
					for (int k = 0; k < inventoryCheckStrings[j].length; k++)
					{
						int[] inventoryCheckInts = new int[inventoryCheckStrings[j].length];
						for (int l = 0; l < inventoryCheckStrings[j][k].length; l++)
						{
							inventoryCheckInts[l] = Integer.parseInt(inventoryCheckStrings[j][k][l]);
						}
						inventoryCheck[j][k] = new InventoryCheck(inventoryCheckInts);
					}
				}
			}
			
			InventoryUpdate[][] inventoryUpdate = new InventoryUpdate[eventsList.length][];		// In case inventory update data exists, parse that into individual strings
																								// Then into integers
			if (!inventoryUpdateData.equals(""))
			{
				whichConstructor_Update = true;
				
				String[] inventoryUpdateList = inventoryUpdateData.split("<>INVENTORYUPDATEMARKER2<>");
				inventoryUpdateData = "";
				
				String[][] inventoryUpdateLists = new String[inventoryUpdateList.length][];
				for (int j = 0; j < inventoryUpdateList.length; j++)
				{
					inventoryUpdateLists[j] = inventoryUpdateList[j].split("<>INVENTORYUPDATEMARKER3<>");
				}
				
				String[][][] inventoryUpdateStrings = new String[inventoryUpdateList.length][][];
				for (int j = 0; j < inventoryUpdateLists.length; j++)
				{
					inventoryUpdateStrings[j] = new String[inventoryUpdateLists[j].length][];
					inventoryUpdate[j] = new InventoryUpdate[inventoryUpdateLists[j].length];
					for (int k = 0; k < inventoryUpdateLists[j].length; k++)
					{
						inventoryUpdateStrings[j][k] = inventoryUpdateLists[j][k].split("<>INVENTORYUPDATEMARKER4<>");
					}
				
				}
				
				for (int j = 0; j < inventoryUpdateStrings.length; j++)
				{
					for (int k = 0; k < inventoryUpdateStrings[j].length; k++)
					{
						int[] inventoryUpdateInts = new int[inventoryUpdateStrings[j].length];
						for (int l = 0; l < inventoryUpdateStrings[j][k].length; l++)
						{
							inventoryUpdateInts[l] = Integer.parseInt(inventoryUpdateStrings[j][k][l]);
						}
						inventoryUpdate[j][k] = new InventoryUpdate(inventoryUpdateInts);
					}
				}
			}
			
			int[] moveNorth = new int[eventsList.length];
			int[] moveEast = new int[eventsList.length];
			int[] moveSouth = new int[eventsList.length];
			int[] moveWest = new int[eventsList.length];
			
			if (moveDirectionData.length() > 10)					// Parse movedirection data into their strings
			{
				whichConstructor_Move = true;
			
				String moveNorthData = moveDirectionData.split("<>MOVENORTHMARKER1<>\n")[0];
				moveDirectionData = moveDirectionData.split("<>MOVENORTHMARKER1<>\n")[1];
				
				String moveEastData = moveDirectionData.split("<>MOVEEASTMARKER1<>\n")[0];
				moveDirectionData = moveDirectionData.split("<>MOVEEASTMARKER1<>\n")[1];
				
				String moveSouthData = moveDirectionData.split("<>MOVESOUTHMARKER1<>\n")[0];
				moveDirectionData = moveDirectionData.split("<>MOVESOUTHMARKER1<>\n")[1];
				
				String moveWestData = moveDirectionData;
				moveDirectionData = "";
				
				String[] moveNorthStrings = moveNorthData.split("<>MOVENORTHMARKER2<>\n");
				for (int j = 0; j < moveNorthStrings.length; j++)			// Then into integers
				{
					moveNorth[j] = Integer.parseInt(moveNorthStrings[j]);
				}
				
				String[] moveEastStrings = moveEastData.split("<>MOVEEASTMARKER2<>\n");
				for (int j = 0; j < moveEastStrings.length; j++)
				{
					moveEast[j] = Integer.parseInt(moveEastStrings[j]);
				}
				
				String[] moveSouthStrings = moveSouthData.split("<>MOVESOUTHMARKER2<>\n");
				for (int j = 0; j < moveSouthStrings.length; j++)
				{
					moveSouth[j] = Integer.parseInt(moveSouthStrings[j]);
				}
				
				String[] moveWestStrings = moveWestData.split("<>MOVEWESTMARKER2<>\n");
				for (int j = 0; j < moveWestStrings.length; j++)
				{
					moveWest[j] = Integer.parseInt(moveWestStrings[j]);
				}
			}
			
			if (whichConstructor_Check)
			{
				if (whichConstructor_Update)
				{
					if (whichConstructor_Move)
					{
						rooms[(i % width)][((int)Math.ceil((float)(i + 1) / width) - 1)] = new Room(descriptions, shortDescriptions, events, keys, roomstateFactor, inventoryCheck, inventoryUpdate, moveNorth, moveEast, moveSouth, moveWest);
					}
					else
					{
						rooms[(i % width)][((int)Math.ceil((float)(i + 1) / width) - 1)] = new Room(descriptions, shortDescriptions, events, keys, roomstateFactor, inventoryCheck, inventoryUpdate);
					}
					
				}
				else if (whichConstructor_Move)
				{
					rooms[(i % width)][((int)Math.ceil((float)(i + 1) / width) - 1)] = new Room(descriptions, shortDescriptions, events, keys, roomstateFactor, inventoryCheck, moveNorth, moveEast, moveSouth, moveWest);
				}
				else
				{
					rooms[(i % width)][((int)Math.ceil((float)(i + 1) / width) - 1)] = new Room(descriptions, shortDescriptions, events, keys, roomstateFactor, inventoryCheck);
				}
			}
			else if (whichConstructor_Update)
			{
				if (whichConstructor_Move)
				{
					rooms[(i % width)][((int)Math.ceil((float)(i + 1) / width) - 1)] = new Room(descriptions, shortDescriptions, events, keys, roomstateFactor, inventoryUpdate, moveNorth, moveEast, moveSouth, moveWest);
				}
				else
				{
					rooms[(i % width)][((int)Math.ceil((float)(i + 1) / width) - 1)] = new Room(descriptions, shortDescriptions, events, keys, roomstateFactor, inventoryUpdate);
				}
			}
			else if (whichConstructor_Move)
			{
				rooms[(i % width)][((int)Math.ceil((float)(i + 1) / width) - 1)] = new Room(descriptions, shortDescriptions, events, keys, roomstateFactor, moveNorth, moveEast, moveSouth, moveWest);
			}
			else
			{
				rooms[(i % width)][((int)Math.ceil((float)(i + 1) / width) - 1)] = new Room(descriptions, shortDescriptions, events, keys, roomstateFactor);
			}
		}
	return rooms;
}

	public static void main(String[] args)
	{
		if(args.length > 0)
		{
			Game game = new Game(args[0]);
		}
		String instring = "";
		String outstring = "";
		
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
			Game game = loadGame(instring);		// Loads game
			Game.run();		// Runs game
		}
	}
}