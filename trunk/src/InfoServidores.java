
/**
 * Clase abstracta que define parámetros para comunicarse
 * exitosamente con los serviores de votación.
 * @author pagarcia
 *
 */
public abstract class InfoServidores
{
	/**
	 * Host de la urna.
	 */
	public static final String HOSTURNA = "localhost";
	
	/**
	 * Puerto de la urna.
	 */
	public static final int PORTURNA = 4080;
	
	/**
	 * Clave pública de la urna.
	 */
	public static final String publicaUrna = "pepe";
	
	/**
	 * Host de la mesa.
	 */
	public static final String HOSTMESA = "localhost";
	
	/**
	 * Puerto de la mesa.
	 */
	public static final int PORTMESA = 4034;
	
	/**
	 * Clave pública de la mesa.
	 */
	public static final String publicaMesa = "pedro";
}
