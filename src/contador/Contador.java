package contador;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import criptografia.Desencriptador;
import criptografia.Hasheador;
import criptografia.Validador;
import eleccion.Boletas;
import eleccion.InfoServidores;
import eleccion.Padron;

/**
 *
 */

/**
 * @author be
 * Programa aparte para contar.
 */
public class Contador {

	public static void main(String[] args) {
		// Accedo al log del contador
		Logger logger=Logger.getLogger("contador");
		PropertyConfigurator.configure(InfoServidores.log4jconf);

		// Stack de claves... LIFO necesitamos
		Stack<String> kOpcStack = new Stack<String>();
		// lista de claves, para cuando est�n ordenadas
		List<String> kOpc = new Vector<String>();
		// tabla de resultados ordenado por partido
		Hashtable<String, Integer> resultados = new Hashtable<String, Integer>();
		// hashes de los sobres para publicar
		Vector<String> hashesSobres = new Vector<String>();

		// Motor de desencriptaci�n
		Desencriptador desencriptador = new Desencriptador();
		// Motor de validaci�n
		Validador validador = null;
		// Clave p�blica de la mesa
		String uMesa = null;
		// ID de la votaci�n
		String idv = null;

		try {
			InfoServidores.inicializarClaves();
		} catch (IOException e) {
			logger.fatal("Hubo un problema al inicializar las claves privadas: " + e.getMessage());
			System.exit(1);
		}

		try {
			Padron.getInstance().cargarPadron(InfoServidores.archVotantes, InfoServidores.archVotaciones);
		} catch (IOException e) {
			logger.fatal("Hubo un problema incializando al padron: " + e.getMessage());
			System.exit(1);
		}

		try {

			validador = new Validador(InfoServidores.publicaMesa);
			logger.debug("Me llaman con argumento:" + args[0]);
			idv = args[0]; // recibo por l�nea de comandos el ID de votaci�n

			/* Armo la lista inversa de claves que tengo que levantar en memoria */

			// primero pido los nombres de los partidos en el orden que fueron guardados
		     for (Enumeration<String> e = Padron.getInstance().getUOpc(idv).keys();
		     	  												e.hasMoreElements() ;) {
		    	 String elem = e.nextElement();
		    	 String key;

		    	 // armo el hash de resultados poniendo los contadores de cada partido en 0.
		    	 resultados.put(elem, Integer.valueOf(0));
		    	 logger.debug("arm� el hash de resultados poniendo los contadores de cada partido en 0");

		    	 // Obtengo la clave privada de la opci�n desde un archivo

		    	 logger.debug("Obtengo la clave privada de la opci�n desde el archivo: " + InfoServidores.resources + "votacion/" + elem + "_privada.key");


		    	 key = InfoServidores.readKey(InfoServidores.resources + "votacion/" + elem + "_privada.key");
		    	 logger.debug(key);

		    	 // agrego las claves a mi pila para tenerlas hacia atr�s.
		    	 kOpcStack.push(key);
		    	 logger.debug("agregu� las claves a mi pila para tenerlas hacia atr�s");

		     }
		     logger.error("Arm� la lista inversa de claves que tengo que levantar en memoria");

		     // invierto la lista
		     while(! kOpcStack.empty()) {
		    	 kOpc.add(kOpcStack.pop());
		     }

		}
		catch (java.lang.ArrayIndexOutOfBoundsException e) // esta exception salta por el arg[0]
		{
			System.out.println("Contador uso: java Contador idvotacion.");
			System.exit(1);
		}
		catch (FileNotFoundException e) // esta exception salta si no est� el archivo
		{
			System.out.println("Contador: falta archivo de clave privada.");
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch (IOException e) // esta exception salta si el archivo est� mal formado
		{
			System.out.println("Contador: archivo de clave privada inv�lido.");
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch (InvalidKeyException e) // esta exception salta si la clave es inv�lida
		{
			System.out.println("Contador: la clave de la mesa es inv�lida.");
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch (Exception e)
		{
			System.out.println("Contador: el ID de votaci�n ingresado: "+ idv +
					" no corresponde a un ID v�lido."); // jaja me qued� como el mensaje de telef�nica!!
			System.exit(1);
		}


		/* ahora que estoy listo para contar, procedo con la cuenta */
		// pido los sobres. Ac� hay un tema con los nombres :( TODO ver

		Boletas sobres = null;
		try {
			sobres = Boletas.getInstance();
			sobres.setIteratorIdv(idv);
		} catch (Exception e)
		{
			logger.fatal("fall� el getInstance de los sobres: " + e.getMessage());
			e.printStackTrace();
		}

		for ( Iterator<String> iter = sobres.iterator(); iter.hasNext();) {

			String sobre = iter.next();
			// antes de hacer bosta el sobre, le calculo el hash.
			String sobreHash = Hasheador.hashear(sobre);
			logger.debug("tomo el sobre con hash: " + sobreHash);

			List<String> boleta = null; //TODO ver si esto no cambia!

			// le aplico todas las claves privadas
			for (Iterator<String> iterator = kOpc.iterator(); iterator.hasNext();)
			{
				String clave = iterator.next();
				try
				{
					if (iterator.hasNext()) // necesito saber si tengo que sacar un string o una lista
/* aqu� muere */						sobre = desencriptador.desencriptarString(sobre, clave);
					else
						boleta = desencriptador.desencriptar(sobre, clave);
				}
				catch (Exception e) // pich� el desencriptador
				{
					System.out.println("Contador: No se pudo desencriptar el sobre: " +
							sobre + "con la clave: " + clave);
					e.printStackTrace();
					System.exit(1);
				}
			}

			try
			{
				boleta = validador.validar(boleta.get(1));
			}
			catch (Exception e) // pinch� la validaci�n
			{
				System.out.println("Contador: No se pudo validar que la boleta: " +
						boleta + "haya sido creada por la mesa");
				e.printStackTrace();
				System.exit(1);
			}

			// cambio de variable para claridad (falta mucha eh!)
			String voto = boleta.get(1);
			String boletaidv = boleta.get(2);

			if (boletaidv.equals(idv)) // si el sobre pertenece a la votaci�n,
			{
				// (leer buen dise�o en java porque esto es un p�simo dise�o de
				// objetos distribu�dos, TODO preguntar a Leticia)

				// cuento el voto
				resultados.put(voto, resultados.get(voto) + 1);
				// y lo agrego a la lista de hashes
				hashesSobres.add(sobreHash);

			/* Si lo de arriba no funca, probar intercalando esto entre el if y el put
				Integer cant = resultados.get(voto);
				if ( cant == null )
				{
					System.out.println("Ocurri� un error que no entiendo... ver esto plz");
					System.exit(1); // TODO ver este caso.
				}
			*/

			}


		}

		System.out.println("Los resultados de la elecci�n " + idv + " son: ");
		System.out.println(resultados);

		System.out.println("La siguiente es la lista de sobres que valieron en " +
				"esta elecci�n: ");
		System.out.println(hashesSobres);

	}

}
