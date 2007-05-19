/**
 * 
 * @author hrajchert
 * @version 0.1a
 * La clase MesaApp es la encargada de ofrecer los servicios de la clase Mesa
 *
 */
import java.net.*;
import java.io.*;

public class MesaApp {

	public static final int PUERTO = 4034;
	public static void main (String args[]) {
	
		Socket s;
		ServerSocket servidor = null;

		// Escucho en el puerto deseado
		try {
			servidor = new ServerSocket(PUERTO);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	
		// Me quedo esperando por nuevos clientes, cuando los encuentro
		// creo un thread.
		while (true)
		{
			try {
				s = servidor.accept();
				(new MesaHandler(s)).start();
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
	}
}
