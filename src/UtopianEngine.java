// Main function in this file

// Author: Ian McDevitt
// Title: The Utopian Engine
// Purpose: Text Adventure Game Engine
// Summary: This engine uses an object-oriented approach to text adventures, and uses a two-dimensional array of Rooms to represent discrete puzzles.
// When loading the game, the Engine will take in all text inside a .ueg file and parse it into a Game object.
// Within the game object are several variables. For their descriptions, see Game.java.
// In essence, the game is made of Rooms, arranged in a two-dimensional grid. The player is given a description of the room they are in,
// and then prompted for input. Once inputed, the text is sent to the appropriate room, and if that text matches a key in that room,
// then the appropriate event is output, and various things may happen. They are, in no particular order:
// * the player could expend an item
// * the player could gain an item
// * the player could win or lose (the Engine does not discriminate -- an endgame is an endgame, and in either event, the game quits. It is up
//		to the developer and the player to decide what is a win and what is a loss)
// * the player could advance or regress the state of the room - thereby changing the room

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UtopianEngine
{
	static Scanner scanner = new Scanner(System.in);

	static String _key;					// Used to hold user's input and modify it (everything is toLower()'d)
	static String _out;					// Used to create and edit the output
	
	static ScriptEngineManager mgr = new ScriptEngineManager();
	static ScriptEngine js_engine = mgr.getEngineByName("js");
    static Bindings js_binding = js_engine.getBindings(ScriptContext.ENGINE_SCOPE);
	static HashMap<Integer, String> gameFiles;
	
	public static void main(String[] args)
	{
		Settings.LoadSettings();
		
		// handle parameters
		if(args.length > 0)
		{
			File f = new File(args[0]);
			
			if(f.exists())
			{
				Game.buildFromFile(args[0]);

				if(args.length > 1)
				{
					BuiltIn.LoadState(args[1]);
				}
			}
			else
			{
				BuiltIn.Println("Sorry, it seems the specified game '" + args[0] + "' does not exist.");
				return;
			}
		}
		else	// no parameter specified
		{
			BuiltIn.Println("Welcome to the Utopian Engine. Below you will find a list of games you have placed in the appropriate folder. In order to play a game, simply type the name of the file.\n");

			// outputs a list of available games, and creates the gameFiles hashmap.
			printGameList();
			
			if(gameFiles.size() == 0)
			{
				BuiltIn.Print("Sorry, no games found. Please make sure to put them in the right folder.");
				return;
			}
				
			while(true)
			{
				String filename = "";
				String choice = getKey();	// Gets input.
				
				if(gameFiles.containsValue(choice))
				{
					filename = choice;
				}
				else if(gameFiles.containsValue(choice + ".ueg"))
				{
					filename = choice + ".ueg";
				}
				else
				{
	 				//try parse as integer and check list
	 				try
	 				{
	 					int gameId = Integer.parseInt(choice);
	 					
	 					if(gameFiles.containsKey(gameId))
	 					{
	 						filename = gameFiles.get(gameId);
	 					}
	 					
	 				}
	 				catch(NumberFormatException nfx)
	 				{
	 					
	 				}
	 			}
				
				File f = new File(filename);
				if(f.exists())
				{
					Game.buildFromFile(filename);
					break;
				}
				else
				{
					BuiltIn.Print("Sorry, game not found. Please try again.");
				}
			}
			
			if (gameFiles != null)
			{
				gameFiles.clear();
				gameFiles = null;
			}
		}
		
		run();
	}

	/**
	 * Main function of the UtopianEngine class. Runs in a loop until the game ends.
	 */
	private static void run()
	{
		try
		{
			BuiltIn.Description("long");
			
			while(true)
			{
				NodeList event = null;
				
				BuiltIn.PrintScore();

				_key = getKey();

				event = Game.rooms[Gamestate.playerX][Gamestate.playerY].checkKeys(_key);
				
				for(int i = 0; i < Game.keys.length && event == null; i++)
				{
					event = Game.keys[i].checkKey(_key);
				}

				runEvent(_key, event);
			}
		}
		catch(GameEndException e)
		{
			BuiltIn.PrintScore();
			
			BuiltIn.Println(e.getMessage() + "\n");
		}
	}
	
	private static String getKey()
	{
		BuiltIn.Print("\n\n> ", false);
		String key = scanner.nextLine().toLowerCase();
		BuiltIn.Println();
		return key;
	}
	
	/**
	 * Pauses, waiting for the player to press enter
	 */
	public static void pause()
	{
		pause("Press enter...");
	}
	
	/**
	 * Pauses, waiting for the player to press enter
	 * @param prompt The prompt that 
	 */
	public static void pause(String prompt)
	{
		if("".equals(prompt))
		{
			pause();
		}
		else
		{
			BuiltIn.Print("\n\n" + prompt, true);
			scanner.nextLine();
			BuiltIn.Println();
		}
	}
	
	/**
	 * Runs the event gotten from a key.
	 * @String event, the combination JavaScript and UtopiaScript corresponding to the matched event.
	 */
	private static void runEvent(String key, NodeList events)
	{
		if(events == null)
		{
			BuiltIn.Print("I don't understand that command.", true);
			return;
		}
		else if(events.getLength() == 0)
		{
			BuiltIn.Println("Nothing happens.");
			return;
		}
		// Runs all of the commands in a loop. Returns prematurely if a function call fails.
		try
		{
			pushScore();
	    	js_engine.eval("key = '" + key + "';");
			for(int i = 0;i < events.getLength();i++)
			{
				pushScore();
				Node event = events.item(i);
				if(event.getNodeName().equals("utopiascript") || event.getNodeName().equals("#text"))
				{
					if(!utopiaCommand(event.getTextContent()))
					{
						return;
					}
				}
				else
				{
			    	js_engine.eval(event.getTextContent());
				}
			}
			pullScore();
		}
		catch(ScriptException e)
		{
			// TODO: Real exception-handling
	    	System.out.println(e.getMessage());
	    	e.printStackTrace();
		}		
	}
	
	public static void pushScore() throws ScriptException
	{
		js_engine.eval("var UtopiaScore = " + Gamestate.score + ";");
	}
	
	public static void pullScore() throws ScriptException
	{
		Double score = (Double) js_engine.eval("UtopiaScore;");//js_binding.get("UtopiaScore");
		try
		{
			Gamestate.score = Double.parseDouble(score.toString());
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
		String args = (arr.length > 1 ? arr[1].trim() : "");
		switch(function)
		{
			case "requireitem":
				return BuiltIn.RequireItem(args);
			case "additem":
				return BuiltIn.AddItem(args);
			case "takeitem":
				return BuiltIn.TakeItem(args);
			case "roomstate":
				return BuiltIn.Roomstate(args);
			case "go":
				return BuiltIn.Go(args);
			case "goto":
				return BuiltIn.Goto(args);
			case "loadgame":
				return BuiltIn.LoadGame(args);
			case "pause":
				return BuiltIn.Pause(args);
			case "print":
				return BuiltIn.Print(args);
			case "println":
				return BuiltIn.Println(args);
			case "description":
				return BuiltIn.Description(args);
			case "score":
				return BuiltIn.Score(args);
			case "quitgame":
				return BuiltIn.QuitGame(args);
			case "inventory":
				return BuiltIn.Inventory(args);
			case "savestate":
				return BuiltIn.SaveState(args);
			case "loadstate":
				return BuiltIn.LoadState(args);
			default:
				throw new UtopiaException(function + ": Command not found.");
		}
	}

	
	
	private static void printGameList()
	{
		try
		{
			  if (gameFiles == null)
			  {
				  gameFiles = new HashMap<Integer,String>();
			  }
			  gameFiles.clear();
			  int gameId = 1;
			  
			  // Directory path here
			  String path = "."; 
			 
			  String filename;
			  String output = "";
			  File folder = new File(path);
			  File[] listOfFiles = folder.listFiles(); 

			  for(int i = 0; i < listOfFiles.length; i++) 
			  {
				  if(listOfFiles[i].isFile()) 
				  {
					  filename = listOfFiles[i].getName();
					  if(filename.toLowerCase().endsWith(".ueg"))
					  {
						  gameFiles.put(gameId, filename);
						  output = output + "\t" + gameId + ". " + filename + "\n";
						  gameId++;
					  }
				  }
			  }
			  if(output.length() > 0)
			  {
				  output = output.substring(0, output.length()-1);
				  BuiltIn.Print(output, false);
			  }
		}
		catch (Exception e)								// Generic exception handling
		{
			System.out.println(e.getMessage());			// Not really robust, because I haven't had the opportunity to test on various platforms
			System.out.println(e.getStackTrace());
		}
	}

	
	
	
	
	
}