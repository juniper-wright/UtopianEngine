public class UtopiaException extends RuntimeException
{
	static final long serialVersionUID = 1;
	
	public UtopiaException(String message)
	{
		super("Error parsing UtopiaScript: `" + message + "`");
	}
}