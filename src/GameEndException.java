//This class is part of the Utopian Engine, which is released by Ian McDevitt to the public under the BSD 3-clause license. See /LICENSE for more information

public class GameEndException extends RuntimeException
{
	static final long serialVersionUID = 1;
	
	public GameEndException()
	{
		super();
	}
	
	public GameEndException(String message)
	{
		super(message);
	}
}