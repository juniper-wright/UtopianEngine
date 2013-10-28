public class LoadGameException extends RuntimeException
{
	static final long serialVersionUID = 1;
	
	public LoadGameException()
	{
		super();
	}
	
	public LoadGameException(String message)
	{
		super("Unable to parse Game XML: " + message);
	}
}