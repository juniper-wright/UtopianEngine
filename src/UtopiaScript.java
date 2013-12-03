/*
 * The global BuiltIn-class contains all script-functions.
 */

import javax.script.ScriptException;

public class UtopiaScript {

	public static boolean AddItem(String args)
	{
		String[] arg_split = args.split(" ");
		String itemNumString = arg_split[0];
		String quantityString;
		int itemNum;
		int quantity;
		
		try
		{
			itemNum = Integer.parseInt(itemNumString);
		}
		catch(NumberFormatException e)
		{
			throw new UtopiaException("Invalid format for command addItem: \"" + itemNumString + "\" is unparseable as an integer");
		}

		if(itemNum >= Game.items.length)
		{
			throw new UtopiaException("Invalid argument for command addItem: \"" + itemNum + "\" is out of bounds for items list.");
		}
		
		if(arg_split.length > 1)
		{
			quantityString = arg_split[1];
			
			try
			{
				quantity = Integer.parseInt(quantityString); 
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
		
		Game.inventory[itemNum] += quantity;
		
		return true;
	}

	public static boolean Description(String args)
	{
		boolean longDesc = true;
		if(args.equalsIgnoreCase("short"))
		{
			longDesc = false;
		}
		return Print(Game.rooms[Game.playerX][Game.playerY].description(longDesc), true);
	}

	public static boolean Go(String args)
	{
		int x;
		int y;
		String[] args_arr = args.split("/| ", 2);
		try
		{
			x = Integer.parseInt(args_arr[0]);
			y = Integer.parseInt(args_arr[1]);
			
			x += Game.playerX;
			y += Game.playerY;
			if(Game.canTravel(x, y))
			{
				Game.playerX = x;
				Game.playerY = y;
			}
			else
			{
				return Print("You can't go that way.", true);
			}
		}
		catch(NumberFormatException e)
		{
			throw new UtopiaException("Go command is formatted improperly. Arguments passed: " + args);
		}
		
		return Description("");
	}

	public static boolean Goto(String args)
	{
		int x;
		int y;
		String[] args_arr = args.split("/| ", 2);

		try
		{
			x = Integer.parseInt(args_arr[0]);
			y = Integer.parseInt(args_arr[1]);

			if(Game.canTravel(x, y))
			{
				Game.playerX = x;
				Game.playerY = y;
			}
			else
			{
				return Print("You can't go that way.", true);
			}
		}
		catch(NumberFormatException e)
		{
			throw new UtopiaException("GoTo command is formatted improperly. Arguments passed: " + args);
		}
		return Description("");
	}
	
	public static boolean Inventory(String args)
	{
		String inv_output = "";
		for(int i = 0;i < Game.items.length; i++)
		{
			if(Game.inventory[i] > 0 && !Game.items[i].trim().equals(""))
			{
				inv_output = inv_output + String.format("%-50sx%s", Game.items[i], Game.inventory[i]) + "\n";
			}
		}
		if(inv_output.length() > 0)
		{
			inv_output = inv_output.substring(0, inv_output.length() - 1);
			Print(inv_output, false);
		}
		else
		{
			Print("You do not have any items.");
		}
		return true;
	}

	public static boolean LoadGame(String args)
	{
		Game.buildFromFile(args);
		return true;
	}
	
	public static boolean LoadState(String args)
	{
		if (Game.loadState(args))
		{
			Description("");
			return true;
		}
		
		return false;
	}

	//loads a Game, ignoring the Gamename-check and Roomstates
	public static boolean LoadStatePrequel(String args)
	{
		if (Game.loadState(args, true))
		{
			return true;
		}
		
		return false;
	}

	public static boolean Pause(String args)
	{
		UtopianEngine.pause(args);
		return true;
	}

	public static boolean Print(String args)
	{
		return Print(args, true);
	}

	/**
	 * All system output will be done through the usPrint function. Thus, it will be easy to change if need be. 	
	 * @param args the String to be printed
	 * @return boolean true
	 */
	public static boolean Print(String args, boolean linebreaks)
	{
		if(linebreaks)
		{
			int curr_line = 0;
			String[] words = args.split(" ");
			for(int i = 0; i < words.length; i++)
			{
				if(words[i].length() + curr_line > Settings.lineLength)
				{
					System.out.println();
					curr_line = 0;
				}
				System.out.print(words[i].replace("\\n", "\n"));
				curr_line += words[i].length() + 1;
				if(curr_line < Settings.lineLength && i+1 != words.length)
				{
					System.out.print(" ");
				}
			}
		}
		else
		{
			System.out.print(args);
		}
		return true;
	}

	public static boolean Println()
	{
		return Print("\n", true);
	}
	
	public static boolean Println(String args)
	{
		return Print(args + "\n", true);
	}
	
	public static boolean PrintScore()
	{
		if(Game.displayScore)
		{
			Println();
			String score = "Score: " + new Integer((int)Game.score).toString();
			if(Game.maxScore > 0)
			{
				score = score + " of " + Game.maxScore;
			}
			Print(String.format("%" + Settings.lineLength + "s", score), false);
		}
		return true;
	}

	public static boolean QuitGame(String args)
	{
		throw new GameEndException(args);
	}

	public static boolean RequireItem(String args)
	{
		String[] arg_split = args.split(" ", 3);
		String itemNumString = arg_split[0];
		String quantityString;
		int itemNum;
		int quantity;
		
		try
		{
			itemNum = Integer.parseInt(itemNumString);
		}
		catch(NumberFormatException e)
		{
			throw new UtopiaException("Invalid argument for command requireItem: \"" + itemNumString + "\" is unparseable as an integer");
		}

		if(itemNum >= Game.items.length)
		{
			throw new UtopiaException("Invalid argument for command requireItem: \"" + itemNum + "\" is out of bounds for items list.");
		}
		
		if(arg_split.length > 1)
		{
			quantityString = arg_split[1];
			
			try
			{
				quantity = Integer.parseInt(quantityString); 
			}
			catch(NumberFormatException e)
			{
				throw new UtopiaException("Invalid argument for command requireItem: \"" + quantityString + "\" is unparseable as an integer.");
			}
		}
		else
		{
			quantity = 1;
		}


		if(Game.inventory[itemNum] < quantity)
		{
			if(arg_split.length > 2)
			{
				Print(arg_split[2], true);
			}
			return false;
		}
		
		return true;
	}

	public static boolean Roomstate(String args)
	{
		String arg = args.replace(" ", "");
		try
		{
			// TODO: Check to make sure that the roomstate exists.
			if(arg.matches("^=[0-9]{1,9}$"))
			{
				Game.rooms[Game.playerX][Game.playerY].setRoomstate(Integer.parseInt(arg.substring(1)));
			}
			else
			{
				Game.rooms[Game.playerX][Game.playerY].setRoomstate(Integer.parseInt(arg) + Game.rooms[Game.playerX][Game.playerY].getRoomstate());
			}
		}
		catch(NumberFormatException e)
		{
			throw new UtopiaException("Invalid format for Roomstate command. \"" + args + "\" is unparseable as an integer.");
		}
		return true;
	}
	
	public static boolean SaveState(String args)
	{
		return Game.saveState(args);
	}
	
	public static boolean Score(String args)
	{
		String arg = args.replace(" ", "");
		try
		{
			if(arg.matches("^=[0-9]{1,9}$"))
			{
				Game.score = Integer.parseInt(arg.substring(1));
			}
			else if(arg.matches("^[0-9]{1,9}$"))
			{
				Game.score += Integer.parseInt(arg);
			}
		}
		catch(NumberFormatException e)
		{
			throw new UtopiaException("Invalid format for Score command. \"" + args + "\" is unparseable as an integer.");
		}
		
		try
		{
			UtopianEngine.pushScore();
		}
		catch(ScriptException e)
		{
			throw new UtopiaException(e.getMessage());
		}
		
		return true;
	}

	public static boolean TakeItem(String args)
	{
		String[] arg_split = args.split(" ", 3);

		if(arg_split.length < 2)
		{
			throw new UtopiaException("Invalid format for command takeItem: \"" + args + "\" does not have enough arguments.");
		}
		
		String itemNumString = arg_split[0];
		String quantityString = arg_split[1];
		int itemNum;
		int quantity;
		
		try
		{
			itemNum = Integer.parseInt(itemNumString);
		}
		catch(NumberFormatException e)
		{
			throw new UtopiaException("Invalid format for command takeItem: \"" + itemNumString + "\" is unparseable as an integer");
		}

		if(itemNum > Game.items.length)
		{
			throw new UtopiaException("Invalid argument for command takeItem: \"" + itemNum + "\" is out of bounds for items list.");
		}
		
		try
		{
			quantity = Integer.parseInt(quantityString); 
		}
		catch(NumberFormatException e)
		{
			throw new UtopiaException("Invalid format for command takeItem: \"" + quantityString + "\" is unparseable as an integer.");
		}
		
		if(Game.inventory[itemNum] < quantity && arg_split.length > 2)
		{
			Println(arg_split[2]);
			return false;
		}
		else
		{
			Game.inventory[itemNum] -= quantity;
			return true;
		}
	}
	
}
