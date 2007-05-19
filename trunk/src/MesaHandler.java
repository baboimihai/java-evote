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
	
	/* Clave privada de la mesa. */
	private String rm;
	
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
	public boolean recPaso1(String msg)	throws Exception {
		String token;
		Desencriptador decrypt = new Desencriptador(this.rm);
		List<String> msg_decrypt;
		System.out.println(msg);
		
		// Desencripto msg con la clave privada de la mesa.
		try	{
			msg_decrypt = decrypt.desencriptar(msg);
			
			// Si no tiene exactamente 4 elementos el mensaje está mal formado.
			if ( msg_decrypt.size() != 4 ) throw new Exception();
		}
		catch (Exception e) {
			throw new Exception("El mensaje esta mal formado");
		}
		
		// Obtengo los valores
		this.usvu = msg_decrypt.get(0);
		this.svm  = msg_decrypt.get(1);
		this.uvi = msg_decrypt.get(2);
		token = msg_decrypt.get(3);

		// Busco uvi en el padrón y obtengo el dni
		//TODO esto.
		this.dni = "30999333";
		
		// Valido el token con uvi y obtengo los datos.
		//Validador valid = new Validador(this.uvi);
		Desencriptador valid = new Desencriptador(this.uvi);
		
		try {
			msg_decrypt = valid.desencriptar(token);

			// Si no tiene exactamente 3 elementos el mensaje está mal formado.
			if ( msg_decrypt.size() != 3 ) throw new Exception();
		}
		catch (Exception e) {
			throw new Exception("El mensaje recibido no está firmado correctamente");
		}
		
		// Verifico que el dni obtenido sea el mismo que el correspondiente a uvi.
		if (! dni.equals(msg_decrypt.get(0)))throw new Exception("El dni no corresponde al padron");
		
		// Verifico que el votante puede votar la elección requerida.
		this.idv = msg_decrypt.get(1);
		//if ( !inst_padron.puedeVotar(this.uvi, this.idv )
		//		return false;
		
		// Verifico que no haya votado
		try {
			List<String> Comprobante;
			Comprobante = Comprobantes.getInstance().obtenerComprobante(uvi, idv);
			
			// Si lo encuentra es porque ya votó o porque tubo problemas al votar.
			
			// Si cambio el usvu o el token está tratando de hacer quilombo.
			if ( !Comprobante.get(0).equals(this.usvu) || !Comprobante.get(1).equals(token) )
				throw new Exception("El votante envio un nuevo usvu o token");
			
			// Me fijo si ya votó
			if ( !Comprobantes.getInstance().yaVoto(this.usvu) )
				throw new Exception("El votante ya votó");
			
		}
		catch (Exception e)
		{
			// Si la excepción es porque no lo encontró es porque no voto.
			if ( !(e instanceof ComprobanteNotFoundException) )
				return false;
		}
		
		
		/* TODO 
		 * - Marco que el usuario ya trató de votar. (tiene que estar serializado esto).
		 * - Guardo el comprobante y el usvu
		 */
		return false;
	}
	
	/**
	 * Método que envía a la Urna el paso2, en caso
	 * de error tira excepción
	 */
	private void envPaso2()	throws Exception {
		/* TODO
		 * - Creo una conexión contra la urna
		 * - Firmo usvu concatenado a idv
		 * - Lo encripto con la clave publica de la urna y se lo envio
		 */
	}
	private void envPaso3() throws Exception {
		/* TODO
		 * - Genero un RandA
		 * - Busco las opciones para IDV
		 * - Hago las boletas firmadas
		 * - Lo encripto con la clave uvi
		 * - Se lo devuelvo al votante
		 */
	}

	private void recPaso5() throws Exception {
		/* TODO
		 * - Recibir el msg
		 * - Lo desencripto con mi clave publica 
		 * - Marco usvu como que ya voto (esto implica tmb al comprobante)
		 * - Guardo el challenge
		 */
	}
	private void envPaso6() throws Exception {
		/* TODO
		 * - Firmo el challenge
		 * - Se lo envio a la urna
		 */
	}

}
