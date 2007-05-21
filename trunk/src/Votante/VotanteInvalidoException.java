package Votante;

/**
 * @author pagarcia
 *
 */
public class VotanteInvalidoException extends Exception
{
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
