//This class is part of the Utopian Engine, which is released by Ian McDevitt to the public under the BSD 3-clause license. See /LICENSE for more information

public class UtopiaException extends RuntimeException
{
	static final long serialVersionUID = 1;
	
	public UtopiaException(String message)
	{
		super("Error parsing UtopiaScript: `" + message + "`");
	}
}