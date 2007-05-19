/**
 * 
 * @author ebalaguer
 * @version 0.1a
 * La clase Padron bla bla bla
 *
 */
import java.io.*;
import java.sql.*;
import java.util.*;

public class Padron
{
	private BufferedReader br;
	private String line;
	private String group = null;
	private static Padron ref;
	
	/**
	 * Constructor privado del padron para evitar que lo creen.
	 */
	private Padron()
	{
		// TODO: Inicializaciones
	}
			
	/**
	 * "Constructor" para el Singleton
	 * @return Instancia de la clase
	 */
	public static synchronized Padron getInstance() 
	{
		if ( ref == null )
			ref = new Padron();
		return ref;
	}

	/**
	 * Carga en memoria el padron
	 * @param votantes Path al archivo de votantes
	 * @param votaciones Path al archivo de votaciones
	 * @throws IOException
	 */
	public void cargarPadron(String votantes, String votaciones) throws IOException
	{
		br = new BufferedReader(new FileReader(votantes));

		while ((line = br.readLine()) != null)
		{
			if (line.charAt(0) == '0')
			{
				group = line.substring(3);
			}
			
		}
	}

	/**
	 *  Esta clase es singleton y no se puede clonar. 
	 */
	  public Object clone()	throws CloneNotSupportedException 
	  {
	    throw new CloneNotSupportedException(); 
	  }
	
	/**
	 * Dado un DNI y un ID de votación consulta si el votante puede votar en dicha elección
	 * @param dni String con el DNI
	 * @param idv ID de la votación
	 * @throws Exception Si no se encontró el par (dni, idv)
	 */
	public boolean puedeVotar(String dni, String idv) throws Exception
	{
		return false;
	}
	
	/**
	 * Dado un DNI devuelve una lista con las votaciones en que puede participar el votante
	 * @param dni String con el DNI
	 * @throws Exception Si no se encontró el dni
	 */
	public List<String> getVotaciones(String dni) throws Exception
	{
		return null;
	}
	
	/**
	 * Devuelve las opciones para un idv
	 * @param idv El id de la votación
	 * @return La lista de Strings con las opciones
	 * @trows Exception Si no existía la votacion
	*/
	public List<String> getOpciones(String idv) throws Exception
	{
		return Arrays.asList("a", "b");
	}
	
	/**
	 * Dado un DNI devuelve la clave pública asociada
	 * @param dni String con el DNI
	 * @throws Exception Si no se encontró el dni
	 */
	public String getUvi(String dni) throws Exception
	{
		return "";
	}
	
	/**
	 * Dada una clave pública devuelve el DNI correspondiente
	 * @param uvi String con la clave pública
	 * @throws Exception Si no se encontró el uvi
	 * 
	 */
	public String getDNI(String uvi) throws Exception
	{
		return "";
	}
	

}