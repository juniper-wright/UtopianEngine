import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Gamestate
{
	private static boolean isValid = false;
	
	public static int playerX;
	public static int playerY;
	public static double score;
	public static int[] inventory;
	
	public static void reset()
	{
		score = 0;
		isValid = true;
	}
	
	public static boolean save(String filename)
	{
		BuiltIn.Print("Saving...");
		if (!isValid) return false;
		
		try
		{
			FileWriter out = new FileWriter(filename);
			BufferedWriter bw = new BufferedWriter(out);
			
			bw.write(Game.gameName);
			bw.newLine();
			
			bw.write(Integer.toString(Gamestate.playerX));
			bw.newLine();
			
			bw.write(Integer.toString(Gamestate.playerY));
			bw.newLine();
			
			bw.write(Integer.toString((int)Gamestate.score));
			bw.newLine();
			
			bw.write(Integer.toString(Gamestate.inventory.length));
			bw.newLine();
			
			for (int i=0; i<Gamestate.inventory.length; i++)
			{
				bw.write(Integer.toString(Gamestate.inventory[i]));
				bw.newLine();	
			}
			
			for (Room[] row : Game.rooms)
			{
				for(Room cell : row)
				{
					bw.write(Integer.toString(cell.getRoomstate()));
					bw.newLine();	
				}
			}
			
			bw.close();
			out.close();
			BuiltIn.Print("Done.");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	public static boolean load(String filename)
	{
		return load(filename, false);
	}
	
	public static boolean load(String filename, boolean isSequel)
	{
		String fn = filename;
		if (!fileExists(fn))
		{
			fn = filename + ".ues";
			if (!fileExists(fn))
			{
				if (!isSequel)
				{
					BuiltIn.Print("Save does not exist.");
				}
				return false;
			}
		}
	
		try
		{
			FileReader in = new FileReader(fn);
			BufferedReader br = new BufferedReader(in);
			
			String gameName = br.readLine();
			
			if (!isSequel && !gameName.equals(Game.gameName))
			{
				//error Games not match!
				BuiltIn.Print("Save is for a different game.");
				br.close();
				in.close();
				return false;
			}
			
			Gamestate.playerX = Integer.parseInt(br.readLine());
			Gamestate.playerY = Integer.parseInt(br.readLine());
			Gamestate.score = Integer.parseInt(br.readLine());
			int itemcount = Integer.parseInt(br.readLine());
			
			for (int i=0; i<itemcount; i++)
			{
				Gamestate.inventory[i] = Integer.parseInt(br.readLine());
			}
			
			if (!isSequel)
			{
				for (Room[] row : Game.rooms)
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
	
	private static boolean fileExists(String filename){
		File f = new File(filename);
		return f.exists();
	}
}
