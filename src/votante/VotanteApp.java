package votante;

import java.io.*;
import java.util.*;
import eleccion.*;


/**
 * La clase VotanteApp se encarga de iniciar el cliente de votación.
 * @author pabloggarcia
 * @version 0.1a
 */
public class VotanteApp {

	// Lectores de stdin
	private static InputStreamReader lectorStream = new InputStreamReader(System.in);
	private static BufferedReader lector = new BufferedReader(lectorStream);
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		
		// Inicializaciones
		try
		{
			// Inicializo las claves de los servidores
			InfoServidores.inicializarClaves();
			// Inicializo el padrón
			Padron.getInstance().cargarPadron(InfoServidores.archVotantes, InfoServidores.archVotaciones);
		}
		catch (IOException e)
		{
			System.out.println("ERROR: Problema en la inicialización. Verifique que existan los archivos de claves de servidores, votantes y votaciones.\n" + e.getMessage());
			System.exit(1);
		}

		try
		{
			
			// Bienvenida y pedido de ingreso de datos
			System.out.println("Bienvenido al sistema de votación.\n");
	
			
			// Pido su DNI
			System.out.print("Ingrese su DNI: ");
			String dni = lector.readLine();


			// Obtengo su clave privada
			String nombreArchivoClavePrivada = "../resources/votantes/" + dni + "_privada.key";
			File archivoClavePrivada = new File(nombreArchivoClavePrivada);

			// Chequeo que pueda leerse
			if (!archivoClavePrivada.canRead())
				throw new Exception("No se encontró el archivo de clave privada asociado a su DNI. Colóquelo en la carpeta resources/votantes.");
			
			String clavePrivada = obtenerClave(archivoClavePrivada);
			
	
			// Comienza el proceso de votación para este votante: creo un nuevo votante
			// con el DNI y clave privada suministrados
			Votante votante = new Votante(dni, clavePrivada);

			
			// Muestro sus votaciones y estados asociados
			System.out.println("Obteniendo el estado de sus votaciones. Espere por favor...");
			List<List<Object>> estadoVotaciones = votante.getEstadoVotaciones();
			
			
			// Si no tiene votaciones, sale
			if (estadoVotaciones.size() == 0)
			{
				System.out.println("Usted no tiene votaciones en las que haya podido o pueda participar.");
				System.exit(0);
			}
			
			List<Object> votacion;
			System.out.println("Estado de Votaciones");
			for (int i = 0; i < estadoVotaciones.size(); i++)
			{
				votacion = estadoVotaciones.get(i);
				System.out.println((i+1) + " - " + votacion.get(0) + "\t" + ((((Boolean)votacion.get(1)) == true)? "Participó" : "No participó"));
			}
			
			
			// Chequeo que pueda votar
			if (!votante.tieneVotacionesPendientes(estadoVotaciones))
			{
				System.out.println("Usted ya no tiene votaciones pendientes.");
				System.exit(0);
			}


			// Pido que elija una votación para votar
			int opcionVot = obtenerOpcion(1, estadoVotaciones.size(), "Ingrese el número de la votación de la que desea participar: ");
			
			
			// Muestro las opciones disponibles para esa votación
			System.out.println("Obteniendo las opciones posibles. Espere por favor...");
			votante.envPaso1(estadoVotaciones.get(opcionVot-1).get(0).toString());
			List<String> opcionesDeVotacion = (List<String>) votante.recPaso3();
			for (int i = 0; i < opcionesDeVotacion.size(); i++)
				System.out.println((i+1) + "-" + opcionesDeVotacion.get(i));
			
			
			// Chequeo si tiene opciones
			if (opcionesDeVotacion.size() == 0)
				throw new Exception("La votación no tiene opciones.");
			
			// Pido que elija una opción para votar
			int opcionOpc = obtenerOpcion(1, opcionesDeVotacion.size(), "Ingrese el número de la opción que desea votar: ");
			
			
			// Envío su voto
			votante.envPaso4(opcionesDeVotacion.get(opcionOpc-1));
			
			
			// Recibo el ticket de votación
			String ticket = votante.recPaso7();
			
			
			// Lo almaceno donde pida el usuario
			System.out.println("Se ha recibido su ticket de votación. Ingrese la ruta al archivo donde desea guardarlo: ");
			
			String nombreArchivoTicket;
			File archivoTicket;
			do
			{
				nombreArchivoTicket = lector.readLine();
				archivoTicket = new File(nombreArchivoTicket);
				
			} while (!archivoTicket.canWrite());
			
			guardarTicket(ticket, archivoTicket);
			
			System.out.println("Se ha guardado exitosamente su ticket. El mismo se encuentra en " + nombreArchivoTicket + ". La votación ha concluido.");

		}
		catch (VotanteInvalidoException vie)
		{
			System.out.println("ERROR: El votante es inválido.");
			System.exit(1);
		}
		catch (IOException ioe)
		{
			System.out.println("ERROR: Ha habido un problema en la conexión.");
			System.exit(1);
		}
		catch (FraudeException fe)
		{
			System.out.println("FRAUDE! El sistema detectó fraude. Contáctese con las autoridades. Detalle: " + fe.getMessage());
			System.exit(1);
		}
		catch (Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
			System.exit(1);
		}

	}
	
	/**
	 * Obtiene la clave privada de un usuario desde un archivo.
	 * @param archivo El handle del archivo del cual extraerla.
	 * @return La clave privada del usuario.
	 * @throws IOException
	 */
	private static String obtenerClave(File archivo) throws IOException
	{
		// Buffer para levantar el string
		char cbuf[];

		// Creo el lector
		BufferedReader br = new BufferedReader(new FileReader(archivo));

		// Reservo la cantidad de bytes que ocupa el archivo
		cbuf = new char[(int)archivo.length()];
			
		// Lo leo en un char[]
		br.read(cbuf);
		return new String(cbuf);
	}
	
	
	/**
	 * Almacena un ticket en un archivo.
	 * @param ticket Contenido del ticket.
	 * @param archivo Archivo donde se almacenará el ticket.
	 * @throws IOException
	 */
	private static void guardarTicket(String ticket, File archivo) throws IOException
	{
		// Creo el escritor
		BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));
		
		bw.write(ticket);
	}

	
	/**
	 * Obtiene una opción del usuario.
	 * @param minimo Mínimo valor que puede tomar la opción.
	 * @param maximo Máximo valor que puede tomar la opción.
	 * @param mensaje Mensaje que se le muestra al usuario cada vez que se le requiere el valor.
	 * @return La opción.
	 */
	private static int obtenerOpcion(int minimo, int maximo, String mensaje) throws IOException
	{
		String entrada;
		int opcion;
		
		do
		{
			// Requiere al usuario la entrada
			System.out.print(mensaje);
			entrada = lector.readLine();
			try
			{
				opcion = Integer.parseInt(entrada);
			}
			catch (NumberFormatException e)
			{
				opcion = minimo-1;
			}
		} while (opcion < minimo || opcion > maximo);
		
		return opcion;
	}
}
