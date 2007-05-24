package eleccion;

/**
 * @author pagarcia
 *
 */
public class FraudeException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6801678648949263046L;

	/**
	 * 
	 */
	public FraudeException()
	{

	}

	/**
	 * @param message
	 */
	public FraudeException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public FraudeException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FraudeException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
