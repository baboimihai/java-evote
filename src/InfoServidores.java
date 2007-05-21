
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
	public static final String hostUrna = "localhost";
	
	/**
	 * Puerto de la urna para la mesa.
	 */
	public static final int puertoUrnaDesdeMesa = 4080;
	
	/**
	 * Puerto de la urna para el votante.
	 */
	public static final int puertoUrnaDesdeVotante = 4081;
	
	/**
	 * Clave pública de la urna.
	 */
	public static final String publicaUrna = "pepe";
	
	/**
	 * Host de la mesa.
	 */
	public static final String hostMesa = "localhost";
	
	/**
	 * Puerto de la mesa para la mesa.
	 */
	public static final int puertoMesaDesdeUrna = 4034;
	
	/**
	 * Puerto de la mesa para el votante.
	 */
	public static final int puertoMesaDesdeVotante = 4035;

	/**
	 * Puerto de la mesa que responde preguntas de Estado de Votaciones.
	 */
	public static final int puertoMesaEV = 4036;
	
	/**
	 * Clave pública de la mesa.
	 */
	public static final String publicaMesa = "pedro";
}
