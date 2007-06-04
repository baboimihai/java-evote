package eleccion;

import java.io.*;

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
	public static String publicaUrna = null;
	
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
	public static String publicaMesa = null;
	
	// Nombres de archivo
	public static final String resources = "../resources/"; 
	public static final String privadaMesaPath = resources + "mesa/mesa_privada.key";
	public static final String privadaUrnaPath = resources + "urna/urna_privada.key";
	public static final String archVotantes = resources + "votantes/votante_parsed.txt";
	public static final String archVotaciones = resources + "votacion/votacion_parsed.txt";
	public static final String log4jconf = resources + "log4j.properties";
	
	public static String readKey(String path) throws IOException {
		// Buffer para levantar el string
		char [] cbuf;

		// Abro los archivos de las clases.
		BufferedReader br = new BufferedReader(new FileReader(path));

		// Reservo la cantidad de bytes que ocupa el archivo
		cbuf = new char[(int)(new File(path)).length()];
		
		// Lo leo en un char[]
		br.read(cbuf);
		return new String(cbuf);
	}
	
	public static void inicializarClaves() throws IOException {
		// Lo guardo como String
		publicaUrna = readKey(resources + "general/urna_publica.key");
		publicaMesa = readKey(resources + "general/mesa_publica.key");
		
	}
	
	//TODO Borrar este main
	public static void main(String args[]) throws IOException {
		inicializarClaves();
	}
}
