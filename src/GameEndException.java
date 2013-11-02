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