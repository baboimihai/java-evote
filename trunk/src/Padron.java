import java.io.*;
import java.sql.*;
import java.util.*;

public class Padron
{
	private BufferedReader br;
	private String line;
	private String group = null;
	
	/**
	 * Dado un DNI y un ID de votaci�n consulta si el votante puede votar en dicha elecci�n
	 * @param dni String con el DNI
	 * @param idv ID de la votaci�n
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
	 * Dado un DNI devuelve la clave p�blica asociada
	 * @param dni String con el DNI
	 */
	public String getUvi(String DNI)
	{
		return "";
	}
	
	/**
	 * Dada una clave p�blica devuelve el DNI correspondiente
	 * @param uvi String con la clave p�blica
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
	public Padron(String votantes, String votaciones) throws IOException
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