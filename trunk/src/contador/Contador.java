package contador;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.Enumeration;
import java.util.HashSet;
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
import eleccion.ComprobantesMesa;
import eleccion.ComprobantesUrna;
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
		// lista de claves, para cuando estén ordenadas
		List<String> kOpc = new Vector<String>();
		// tabla de resultados ordenado por partido
		Hashtable<String, Integer> resultados = new Hashtable<String, Integer>();
		// hashes de los sobres para publicar
		HashSet<String> hashesSobres = new HashSet<String>();

		// conjunto de comprobantes de la urna
		HashSet<String> comprobantesUrnaValidos = new HashSet<String>();


		// Motor de desencriptación
		Desencriptador desencriptador = new Desencriptador();
		// Motor de validación
		Validador validador = null;
		// ID de la votación
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
		//logger.debug("Me llaman con argumento:" + args[0]);
			idv = args[0]; // recibo por línea de comandos el ID de votación

			/* Armo la lista inversa de claves que tengo que levantar en memoria */

			// primero pido los nombres de los partidos en el orden que fueron guardados
		     for (Enumeration<String> e = Padron.getInstance().getUOpc(idv).keys();
		     	  												e.hasMoreElements() ;) {
		    	 String elem = e.nextElement();
		    	 String key;

		    	 // armo el hash de resultados poniendo los contadores de cada partido en 0.
		    	 resultados.put(elem, Integer.valueOf(0));
		    //logger.debug("armé el hash de resultados poniendo los contadores de cada partido en 0");

		    	 // Obtengo la clave privada de la opción desde un archivo

		    //logger.debug("Obtengo la clave privada de la opción desde el archivo: " + InfoServidores.resources + "votacion/" + elem + "_privada.key");


		    	 key = InfoServidores.readKey(InfoServidores.resources + "votacion/"
		    			 + idv.replace(' ', '_').replace('¿', '_').replace('/', '_').replace('?', '_')
		    			 + "_" + elem.replace(' ', '_').replace('¿', '_').replace('/', '_').replace('?', '_')
		    			 + "_privada.key");
		    //logger.debug(key);

		    	 // agrego las claves a mi pila para tenerlas hacia atrás.
		    	 //kOpc.add(key);//
		    	 kOpcStack.push(key);
		    //logger.debug("agregué las claves a mi pila para tenerlas hacia atrás");

		     }
		     logger.error("Armé la lista inversa de claves que tengo que levantar en memoria");

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
		catch (FileNotFoundException e) // esta exception salta si no está el archivo
		{
			System.out.println("Contador: falta archivo de clave privada.");
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch (IOException e) // esta exception salta si el archivo está mal formado
		{
			System.out.println("Contador: archivo de clave privada inválido.");
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch (InvalidKeyException e) // esta exception salta si la clave es inválida
		{
			System.out.println("Contador: la clave de la mesa es inválida.");
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch (Exception e)
		{
			System.out.println("Contador: el ID de votación ingresado: "+ idv +
					" no corresponde a un ID válido."); // jaja me quedó como el mensaje de telefónica!!
			System.exit(1);
		}



		/* ahora le pido a la urna sus comprobantes, si alguno no es correcto, o está duplicado
		 * la mandamos al paredón.
		 */

		ComprobantesUrna comprobantesU = null;
		try {
			comprobantesU = ComprobantesUrna.getInstance();
			comprobantesU.setIteratorIdv(idv);
		} catch (Exception e)
		{
			logger.fatal("falló el getInstance de los comprobantes de la urna: " + e.getMessage());
			e.printStackTrace();
		}

		System.out.println("Contador: Verificando honestidad de la Unra...");
		for ( Iterator<String> iter = comprobantesU.iterator(); iter.hasNext();) {

			String comprobante = iter.next();

			List<String> comprobanteValido;

			try {
				comprobanteValido = validador.validar(comprobante);
				if ( comprobanteValido.get(1).equals(idv) && !comprobantesUrnaValidos.add(comprobante)) {
					//si no lo agrego, es porque está repetido, entonces hay lío
					System.out.println("Contador: El comprobante: " +
							comprobante + " está repetido. ");
					System.exit(1); // si salta siempre por acá olvidar el comprobanteValido.get
				}

			} catch (InvalidKeyException e) {
				System.out.println("Contador: El comprobante: " +
						comprobante + " no fue generado por la mesa. ");
				e.printStackTrace();
				System.exit(1);
			} catch (IOException e) {
				System.out.println("Contador: El comprobante: " +
						comprobante + " está mal codificado. ");
				//e.printStackTrace();
				System.exit(1);
			}

		}



		/* ahora que estoy listo para contar, procedo con la cuenta */
		// pido los sobres. Acá hay un tema con los nombres :( TODO ver

		Boletas sobres = null;
		try {
			sobres = Boletas.getInstance();
			sobres.setIteratorIdv(idv);
		} catch (Exception e)
		{
			logger.fatal("falló el getInstance de los sobres: " + e.getMessage());
			e.printStackTrace();
		}

		for ( Iterator<String> iter = sobres.iterator(); iter.hasNext();) {

			String sobre = iter.next();
			// antes de hacer bosta el sobre, le calculo el hash.
			String sobreHash = Hasheador.hashear(sobre);
		//logger.debug("tomo el sobre con hash: " + sobreHash);

			List<String> boleta = null; //TODO ver si esto no cambia!

			// le aplico todas las claves privadas
			for (Iterator<String> iterator = kOpc.iterator(); iterator.hasNext();)
			{
				String clave = iterator.next();
				try
				{
					if (iterator.hasNext()) // necesito saber si tengo que sacar un string o una lista
/* aquí muere */		sobre = desencriptador.desencriptarString(sobre, clave);
					else
						boleta = desencriptador.desencriptar(sobre, clave);
				}
				catch (Exception e) // pichó el desencriptador
				{
					System.out.println("Contador: No se pudo desencriptar el sobre: " +
							sobre + " con la clave: " + clave);
					//e.printStackTrace();
					continue;//System.exit(1);
				}
			}

			try
			{
				boleta = validador.validar(boleta.get(0));
			}
			catch (Exception e) // pinchó la validación
			{
				System.out.println("Contador: No se pudo validar que la boleta: " +
						boleta + " haya sido creada por la mesa");
				//e.printStackTrace();
				continue;//System.exit(1);
			}

			// cambio de variable para claridad (falta mucha eh!)
			String voto = boleta.get(0);
			String boletaidv = boleta.get(1);

			if (boletaidv.equals(idv)) // si el sobre pertenece a la votación,
			{
				// cuento el voto
				resultados.put(voto, resultados.get(voto) + 1);
				// y lo agrego a la lista de hashes
				if (!hashesSobres.add(sobreHash)) {
					//si no lo agrego, es porque está repetido, entonces hay lío
					System.out.println("Contador: El sobre: " +
							sobreHash + " está repetido. ");
					System.exit(1);
				}

			/* Si lo de arriba no funca, probar intercalando esto entre el if y el put
				Integer cant = resultados.get(voto);
				if ( cant == null )
				{
					System.out.println("Ocurrió un error que no entiendo... ver esto plz");
					System.exit(1); // TODO ver este caso.
				}
			*/

			}


		}

/*		String resultHTML = new String();
		resultHTML.concat("<html>");
		resultHTML.concat("<head><title>Resultados de la elección " + idv + "</title></head>");
		resultHTML.concat("<body>Los resultados de la elección " + idv + " son: <p/>");
		resultHTML.concat(resultados.toString());
*/		System.out.println(resultados);
/*		resultHTML.concat("<p/>");
*/

		System.out.println("La siguiente es la lista de sobres que valieron en " +
				"esta elección: ");
//		resultHTML.concat("<p/>");
		int i= 0;
		for (String string : hashesSobres) {
			System.out.println("hash " + i++ + ": " + string);
//			resultHTML.concat("<p/>");
		}
//		resultHTML.concat("</body><html>");
		// Creo el escritor
/*		try {
			File archivo = new File("resultados.htm");
			BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));
			bw.write(resultHTML);
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
*/
		/* ahora le pido a la mesa sus comprobantes, si alguno no es correcto, o está duplicado
		 * la mandamos al paredón.
		 */

		ComprobantesMesa comprobantesM = null;
		try {
			comprobantesM = ComprobantesMesa.getInstance();
			comprobantesM.setIteratorIdv(idv);
		} catch (Exception e)
		{
			logger.fatal("falló el getInstance de los comprobantes de la urna: " + e.getMessage());
			e.printStackTrace();
		}

		System.out.println("Contador: Verificando honestidad de la Mesa...");
		for ( Iterator<String> iter = comprobantesM.iterator(); iter.hasNext();) {

			String comprobante = iter.next();

			List<String> comprobanteValido;

			try {
				comprobanteValido = validador.validar(comprobante);
				if ( comprobanteValido.get(1).equals(idv) && !comprobantesUrnaValidos.add(comprobante)) {
					//si no lo agrego, es porque está repetido, entonces hay lío
					System.out.println("Contador: El comprobante: " +
							comprobante + " está repetido. ");
					System.exit(1); // si salta siempre por acá olvidar el comprobanteValido.get
				}

			} catch (InvalidKeyException e) {
				System.out.println("Contador: El comprobante: " +
						comprobante + " no fue generado por la mesa. ");
				//e.printStackTrace();
				System.exit(1);
			} catch (IOException e) {
				System.out.println("Contador: El comprobante: " +
						comprobante + " está mal codificado. ");
				//e.printStackTrace();
				System.exit(1);
			}

		}

		System.out.println("Contador: Comparando registros de la urna contra registros de la mesa...");


		System.out.println("Contador: Tanto la urna como la mesa parecen honestas. " +
				"Ahora es tarea de los votantes chequear que su voto contó.");



	}

}
