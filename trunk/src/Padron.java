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
	 * "Constructor" para el Singleton
	 * @param votantes Path al archivo de votantes
	 * @param votaciones Path al archivo de votaciones
	 * @return Instancia de la clase
	 * @throws IOException 
	 */
	public static synchronized Padron getInstance(String votantes, String votaciones) throws IOException
	{
		if ( ref == null )
			ref = new Padron(votantes, votaciones);
		return ref;
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
	 */
	public boolean puedeVotar(String dni, String idv)
	{
		return false;
	}
	
	/**
	 * Dado un DNI devuelve una lista con las votaciones en que puede participar el votante
	 * @param dni String con el DNI
	 */
	public List<String> getVotaciones(String dni)
	{
		return null;
	}
	
	/**
	 * Dado un DNI devuelve la clave pública asociada
	 * @param dni String con el DNI
	 */
	public String getUvi(String DNI)
	{
		return "";
	}
	
	/**
	 * Dada una clave pública devuelve el DNI correspondiente
	 * @param uvi String con la clave pública
	 */
	public String getDNI(String uvi)
	{
		return "";
	}
	
	/**
	 * Carga en memoria el padron
	 * @param votantes Path al archivo de votantes
	 * @param votaciones Path al archivo de votaciones
	 * @throws IOException
	 */
	private Padron(String votantes, String votaciones) throws IOException
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
}