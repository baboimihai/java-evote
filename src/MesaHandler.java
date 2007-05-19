/**
 * 
 * @author hrajchert
 * @version 0.1a
 * La clase MesaHandler es la encargada de atender a un votante para autenticarlo etc etc.
 *
 */
import java.net.*;
import java.io.*;
import java.util.*;
public class MesaHandler extends Thread{
	
	private Socket votante;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String usvu;
	private String uvi;
	private String svm;
	private String dni;
	private String idv;
	
	public MesaHandler(Socket aVotante)	throws IOException {
		votante = aVotante;
		in = new ObjectInputStream(votante.getInputStream());
		out = new ObjectOutputStream(votante.getOutputStream());
	}
	
	public void run() 
	{
		try {
			String msg = (String) in.readObject();
			if (this.recPaso1(msg))
			{
				this.envPaso2();
				this.envPaso3();
				this.recPaso5();
				this.envPaso6();
			}
			else
			{
				System.out.println("Trato de votar un usuario no valido");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	

	/**
	 * Método para recibir el paso 1 del protocolo.
	 * En caso de error tira excepcion
	 * Devuelve true si el usuario podía votar, false caso contrario
	 * @param msg El mensaje recibido
	 * @return Devuelve true si el votante está autorizado, false en otro caso. 
	 */
	public boolean recPaso1(String msg) 
					throws Exception {
		String token;
		
		System.out.println(msg);
		/* TODO 
		 * - Desencriptar msg con clave privada
		 * - Buscar uvi en el padron, obtener el dni
		 * - Desencriptar el token con uvi.
		 * - Marco que el usuario ya trató de votar. (tiene que estar serializado esto).
		 * - Validar al usuario, ver que no votó, caso contrario devolver false
		 * - Guardo el comprobante y el usvu
		 * - Llamar a envPaso2 con el secreto compartido
		 */
		return false;
	}
	
	/**
	 * Método que envía a la Urna el paso2, en caso
	 * de error tira excepción
	 */
	private void envPaso2()
					throws Exception {
		/* TODO
		 * - Creo una conexión contra la urna
		 * - Firmo usvu concatenado a idv
		 * - Lo encripto con la clave publica de la urna y se lo envio
		 */
	}
	private void envPaso3() {
	}

	private void recPaso5() {
	}
	private void envPaso6() {
	}

}
