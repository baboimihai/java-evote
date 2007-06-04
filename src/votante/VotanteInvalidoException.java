package votante;

/**
 * @author pagarcia
 *
 */
public class VotanteInvalidoException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 925092039546601516L;

	/**
	 * 
	 */
	public VotanteInvalidoException()
	{

	}

	/**
	 * @param message
	 */
	public VotanteInvalidoException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public VotanteInvalidoException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public VotanteInvalidoException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
