import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class Settings {

	public static int lineLength = 80;
	
	public static void LoadSettings()
	{
		File f = new File ("ue.ini");
		if (!f.exists())
		{
			CreateDefaultSettings();
		}else{
			try
			{
				FileReader in = new FileReader("ue.ini");
				BufferedReader br = new BufferedReader(in);
				
				String currentLine = "";
				while( (currentLine = br.readLine()) != null)
				{
				
					if (currentLine.indexOf(";") == 0)
					{
						continue;
					}
					
					if (currentLine.contains("="))
					{
						String[] parts = currentLine.split("=");
						
						String setting = parts[0].trim().toLowerCase();
						String value = parts[1].trim();
						
						if (setting.equals("linelength")){
							try
							{
								lineLength = Integer.parseInt(value);	
							}
							catch(NumberFormatException nfe)
							{
							}
						}
						
						//add other settings
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
		}
	}
	
	public static void CreateDefaultSettings()
	{
		try
		{
			FileWriter out = new FileWriter("ue.ini");
			BufferedWriter bw = new BufferedWriter(out);
			
			bw.write("; UtopiaEngine Settings");
			bw.newLine();
			
			bw.write("linelength=" + Integer.toString(lineLength));
			bw.newLine();
			
			bw.close();
			out.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
