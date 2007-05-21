package contador;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

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
		//TODO implementar!!
		String idv = args[0]; // recibo por l�nea de comandos el ID de votaci�n
		Padron padron = Padron.getInstance();
		List<String> opciones;
		Enumeration<String> opcFilename = null;
		Hashtable<String, Integer> resultados = new Hashtable<String, Integer>();
		Vector hashesSobres = new Vector();



		try {

			// Pido las opciones al padr�n electoral
			opciones = padron.getOpciones(idv);
			// y pongo los contadores de cada partido en 0.
			for (String string : opciones)
				resultados.put(string, Integer.valueOf(0));

			// Obtengo las claves p�blicas de las opciones para esta votaci�n
			opcFilename = Padron.getInstance().getUOpc(idv).keys();




		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			System.out.println("Contador uso: java Contador idvotacion");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}




		//getsobres
		//getClavesPartidos() ordenadas hacia atr�s
		//armar lista con todas menos la primera

		//foreach(sobre)
		//	foreach(clave menos primera)
		// 		sobre = desencriptarString(sobre,clave)
		//  lista = desencriptar(sobre,1ra);
		//  votolist = validar(lista(1),uMesa);
		//	if (votolist.IDv == miIDv)
		//		Diccionario.buscar(votolist(1)).agregar1(); -> si no existe, mal formado (no puede ser que no est� en la votaci�n porque el id fue validado y eso lo mand� la mesa)
		//		vectorHashes.add(hashSobre)
		//
		//imprimir diccionario y vector en html

	}

}
/*
String msg; // Mensaje
// T�rminos del mensaje
String t1, t2; // Nivel 0
String t2_1, t2_2; // Nivel 1

// Genero randB
SecureRandom random = new SecureRandom();
byte bytes[] = random.generateSeed(12);
random.nextBytes(bytes);
String randB = new String(bytes);

// Obtengo las claves p�blicas de las opciones para esta votaci�n
Enumeration<String> uOpc = Padron.getInstance().getUOpc(idv).elements();

// Inicializo encriptadores
Encriptador eUrna = new Encriptador(InfoServidores.publicaUrna);
Encriptador eOpciones = new Encriptador();


// Obtengo los t�rminos del mensaje
// T�rmino 1
t1 = svu;

//T�rmino 2
t2_1 = opcBoletas.get(opcion);
t2_2 = randB;
// Encripto con las claves p�blicas de las opciones de esta votaci�n
// Con la primera clave
t2 = eOpciones.encriptar(Arrays.asList(t2_1, t2_2), uOpc.nextElement());
// Con el resto
while (uOpc.hasMoreElements())
		t2 = eOpciones.encriptar(t2, uOpc.nextElement());

// Guardo el hash del sobre para chequear luego contra el ticket
this.sobreHasheado = Hasheador.hashear(t2);

// Obtengo el mensaje final
msg = eUrna.encriptar(Arrays.asList(t1, t2));

// Lo env�o a la urna
urnaOut.writeObject(msg);*/