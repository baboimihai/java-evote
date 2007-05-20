
import java.io.*;
import java.util.*;

/**
 * 
 * @author pagarcia
 * @version 0.1a
 * La clase Votante representa una persona que se conecta al sistema para depositar votos.
 *
 */

public class Votante
{
	// Claves p�blicas conocidas
	private static String publicaUrna = "pepe";
	private static String publicaMesa = "pedro";
	
	// Propiedades de este votante
	private String dni;
	private String rvi;
	private String uvi;
	

	/**
	 * Crea un votante asociado a un DNI con una clave privada.
	 * @param dni Documento Nacional de Identidad del votante a crear.
	 * @param contrasenia Contrase�a del votante. En particular, su clave privada.
	 * @throws Exception Si hubo un problema en la creaci�n.
	 */
	public Votante(String dni, String contrasenia) throws Exception//, LoginInvalidoException
	{
		this.dni = dni;
	
		try
		{
			// Obtengo la clave publica del votante
			this.uvi = Padron.getInstance().getUvi(dni);
		}
		catch (Exception e)
		{
			// No se encontro el DNI en el padron
			throw new Exception("DNI o contrase�a inv�lidos.");
			// TODO: ver si tiro otra excepci�n particular, como LoginInvalidoException
		}
		
		// Chequeo la contrase�a que me dio
		if (!esValidaContrasenia(dni, contrasenia))
			throw new Exception("DNI o contrase�a inv�lidos.");
			// TODO: ver si tiro otra excepci�n particular, como LoginInvalidoException

		this.rvi = contrasenia;
	}
	
	/**
	 * Chequea si una contrasenia es valida para un DNI dado.
	 * @param dni El DNI del votante.
	 * @param contrasenia La contrasena ingresada, que debiera ser su clave privada.
	 * @return Indica si es valida o no.
	 * @throws Exception Si hubo un problema en la encriptacion o desencriptacion.
	 */
	private boolean esValidaContrasenia(String dni, String contrasenia) throws Exception
	{
		// Creo el encriptador y desencriptador para chequear la validez
		Encriptador encrypt = new Encriptador(uvi);
		Desencriptador decrypt = new Desencriptador(contrasenia);
		
		// Encripto su DNI con su clave publica
		// Si logro obtenerlo nuevamente desencriptandolo con su contrasenia,
		// es porque era su clave privada
		if (decrypt.desencriptar(encrypt.encriptar(dni)).equals(dni))
			return true;
		else
			return false;
	}

	/**
	 * Pide a la mesa el estado de las votaciones en que puede participar.
	 * @param dni El DNI del votante.
	 * @return Una tabla donde se encuentran los pares "nombre de la votaci�n" y "vot� / no vot�".
	 */
	public Hashtable<String,Boolean> getEstadoVotaciones(String dni)
	{
		//TODO: Completar.
		return new Hashtable();
	}
	
	/**
	 * Env�a el Paso 1 del protocolo [Identificaci�n para una votaci�n].
	 * @throws Exception
	 */
	public void envPaso1() throws Exception
	{
		//TODO: Completar.
	}
	
	/**
	 * Env�a el Paso 4 del protocolo [Mete boleta en Urna].
	 * @throws Exception
	 */
	public void envPaso4() throws Exception
	{
		//TODO: Completar.
	}
	
	/**
	 * Recibe el Paso 3 del protocolo [Recibe boletas con opciones posibles].
	 * @return una lista de opciones posibles para la votaci�n elegida.
	 * @throws Exception
	 */
	public List<String> recPaso3() throws Exception
	{
		//TODO: Completar.
		return null;
	}
	
	/**
	 * Recibe el Paso 7 del protocolo [Recibe el ticket de la votaci�n].
	 * @return El ticket de votaci�n.
	 * @throws Exception
	 */
	public String recPaso7() throws Exception
	{
		//TODO: Completar.
		return new String();
	}
	
}
