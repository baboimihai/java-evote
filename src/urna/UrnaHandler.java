package urna;

/**
 * @author hrajchert
 * @version 0.1a
 * Esta es la clase urna, encargada de bla bla bla.
 */
import java.util.*;
import java.io.*;
import java.net.*;

public class UrnaHandler extends Thread {

	// Variables de conexión hacia el votante
	private Socket votante;
	private ObjectInputStream votanteIn;
	private ObjectOutputStream votanteOut;
	
	// Variables de conexión hacia la urna
	private Socket mesa;
	private ObjectInputStream mesaIn;
	private ObjectOutputStream mesaOut;
	
	public UrnaHandler(Socket aMesa)	throws IOException {
		mesa = aMesa;
		mesaIn = new ObjectInputStream(votante.getInputStream());
		mesaOut = new ObjectOutputStream(votante.getOutputStream());
	}
	
	/**
	 * El run sirve para correr el thread.
	 */
	public void run() 
	{
		//
		// TODO: Responder los errores etc.
		//
		try {
			this.recPaso2();
			this.recPaso4();
			this.envPaso5();
			this.recPaso6();
			this.envPaso7();

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	
	public void recPaso2() {
		// Levanto el mensaje del socket mesa
		//String msg = (String) mesaIn.readObject();
		
		// Lo desencripto con la clave publica de la urna
		//Desencriptador
		// Guardo el token firmado como comprobante
		// Verifico que el svu este firmado por la Mesa 
		// Guardo el id de la votacion.
		// Desencripto el svu con mi clave privada
		
	}
	public void recPaso4() {
		// Le pido a UrnaManager por el mensaje 4 (bloqueante).
		// Guardo el socket del votante
		// Guardo el sobre
	}
	
	public void envPaso5() {
		// Creo un string aleatorio challenge
		// Encripto svu con mi clave publica
		// Encripto con la clave publica de la mesa el svu encriptado y el challenge
		// Envio el mensaje a la mesa.
	}
	public void recPaso6() {
		// Agarro el msg de la mesa
		// Lo desencripto con mi clave privada
		// Verifico que obtenga el challenge firmado por la mesa
		
	}
	public void envPaso7() {
		// Hago un hash del sobre
		// Lo firmo
		// Lo envio al votante
	}

}
