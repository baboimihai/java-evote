package mesa;
/**
 * 
 * @author hrajchert
 * @version 0.2a
 * La clase MesaHandler es la encargada de atender a un votante para autenticarlo etc etc.
 *
 */


import java.net.*;
import java.io.*;
import java.util.*;
import java.security.SecureRandom;
import org.apache.log4j.*;

import criptografia.*;

import eleccion.*;

public class MesaHandler extends Thread{
	
	// Variables de conexión hacia el votante
	private Socket votante;
	private ObjectInputStream votanteIn;
	private ObjectOutputStream votanteOut;
	
	// Variables de conexión hacia la urna
	private Socket urna;
	private ObjectInputStream urnaIn;
	private ObjectOutputStream urnaOut;
	
	// Propiedades de esta transacción
	private String usvu;
	private String uvi;
	private String svm;
	private String dni;
	private String idv;
	private String challenge;
	
	// Claves privada de la mesa
	private String privadaMesa;

	// Clase de log
	private Logger logger;
	
	public MesaHandler(Socket aVotante)	throws IOException {
		// Accedo al log de la mesa
		logger=Logger.getLogger("evote.mesa.handler");
		PropertyConfigurator.configure(InfoServidores.log4jconf);
		
		votante = aVotante;
		votanteOut = new ObjectOutputStream(votante.getOutputStream());
		votanteIn = new ObjectInputStream(votante.getInputStream());
		
		
		privadaMesa = InfoServidores.readKey(InfoServidores.privadaMesaPath);
	}
	
	/**
	 * El run sirve para correr el thread.
	 */
	public void run() 
	{
		//
		// TODO: Responder al votante los errores etc.
		//
		try {
			String msg = (String) votanteIn.readObject();
			if (this.recPaso1(msg))
			{
				this.envPaso2();
				this.envPaso3();
				this.recPaso5();
				this.envPaso6();
			}
			else
			{
				logger.warn("Trato de votar un usuario no valido");
			}
		}
		catch (Exception e) {
			logger.error("Fallo la votacion", e);
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
		Desencriptador decrypt = new Desencriptador(privadaMesa);
		List<String> msg_decrypt;
		
		logger.info("Atendiendo a un votante");
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
		try {
			this.dni =	Padron.getInstance().getDNI(this.uvi);
		}
		catch (Exception e)
		{
			// Si no está el dni el usuario no puede votar.
			return false;
		}
		logger.debug("dni = " + dni);
		
		// Valido el token con uvi y obtengo los datos.
		Validador valid = new Validador(this.uvi);
		
		try {
			msg_decrypt = valid.validar(token);

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
		if ( !(Padron.getInstance().puedeVotar(this.dni, this.idv)))
				return false;
		
		
		// Verifico que el hash comprobante sea correcto.
		String hashVotante = msg_decrypt.get(2);
		 
		String hashMesa = Hasheador.hashear(Arrays.asList(usvu, svm));
		if ( !hashVotante.equals(hashMesa)) throw new Exception("No coincide el hash del votante con el de la mesa");
		
		// Verifico que no haya votado
		try {
			List<String> Comprobante;
			Comprobante = ComprobantesMesa.getInstance().obtenerComprobante(uvi, idv);
			
			// Si lo encuentra es porque ya votó o porque tubo problemas al votar.
			
			// Si cambió el usvu o el token está tratando de hacer quilombo.
			if ( !Comprobante.get(0).equals(this.usvu) || !Comprobante.get(1).equals(token) )
				throw new Exception("El votante envio un nuevo usvu o token");
			
			// Me fijo si ya votó
			if ( !ComprobantesMesa.getInstance().yaVoto(this.usvu) )
				throw new Exception("El votante ya votó");
			
		}
		catch (Exception e)
		{
			// Si la excepción es porque no lo encontró es porque no voto.
			// caso contrario devuelvo false porque ya voto o hizo quilombo.
			if ( !(e instanceof ComprobanteNotFoundException) )
				return false;
		}
		
		// Agrego el comprobante a la lista de comprobantes.
		//TODO: Ver que onda con el error aca.
		ComprobantesMesa.getInstance().insertarComprobante(this.usvu, this.uvi,this.idv, token);
		/* TODO 
		 * - Marco que el usuario ya trató de votar. (tiene que estar serializado esto). Ver si con lo de arriba funca.
		 * - Guardo el comprobante y el usvu
		 */
		logger.info(dni + " esta habilitado para votar en" + idv);
		return true;
	}
	
	/**
	 * Método que envía a la Urna el paso2, en caso
	 * de error tira excepción
	 */
	private void envPaso2()	throws Exception {
		logger.info("Informandole a la urna que " + dni + " puede votar en " + idv );
		// Creo una conexión contra la urna
		urna = new Socket(InfoServidores.hostUrna, InfoServidores.puertoUrnaDesdeMesa);
		urnaIn = new ObjectInputStream(urna.getInputStream());
		urnaOut = new ObjectOutputStream(urna.getOutputStream());
 
		// Firmo usvu concatenado a idv
		Firmador firm = new Firmador(privadaMesa);
		String mensaje2 = firm.firmar(Arrays.asList(usvu, idv));
		
		// Lo encripto con la clave publica de la urna
		Encriptador encrypt = new Encriptador(InfoServidores.publicaUrna);
		String mensaje2_enc = encrypt.encriptar(mensaje2);
		
		// Se lo envio
		urnaOut.writeObject(mensaje2_enc);
		logger.debug("Terminado paso 2");
	}
	private void envPaso3() throws Exception {
		logger.info("Enviando boletas al votante");
		// Genero un RandA
		// TODO Verificar esto de los random
		SecureRandom random = new SecureRandom();
		byte bytes[] = random.generateSeed(12);
		random.nextBytes(bytes);
		String randA = new String(bytes);

		// Busco las opciones para IDV
		List<String> opciones =	Padron.getInstance().getOpciones(this.idv);
		
		// Hago las boletas firmadas
		Firmador firm = new Firmador(privadaMesa);
		List<String> opc_firmadas = new Vector<String>();
		for (String aOpc : opciones) {
			String aOpc_firmada = firm.firmar(Arrays.asList(aOpc, this.idv, randA));
			opc_firmadas.add(aOpc_firmada);
		}
		
		//  Encripto las boletas firmadas con la clave uvi
		Encriptador encrypt = new Encriptador(this.uvi);
		String mensaje3 = encrypt.encriptar(opc_firmadas);
		
		
		// Se lo envío al votante
		votanteOut.writeObject(mensaje3);
		logger.debug("Terminado paso 3");
	}

	private void recPaso5() throws Exception {
		logger.info("Recibiendo comprobante de la urna");
		// Recibo el mensaje TODO: mirar por recibir excepciones.
		String mensaje_enc = (String) urnaIn.readObject();
		
		 // Lo desencripto con mi clave privada
		Desencriptador decrypt = new Desencriptador(privadaMesa);
		List<String> mensaje = decrypt.desencriptar(mensaje_enc);
		
		// Si usvu es el que tengo marco al comprobante como que ya votó
		if ( !this.usvu.equals(mensaje.get(0)))
			throw new Exception("Mensaje invalido"); //TODO: Ver a quien informarle esto
		
		ComprobantesMesa.getInstance().marcarVotado(usvu); //TODO Ver si atrapar la excepcion
		
		this.challenge = mensaje.get(1);
		logger.debug("Terminando paso 5");
	}
	private void envPaso6() throws Exception {
		logger.info("Enviando ACK a la urna");
		 // Firmo el challenge y lo encripto con la clave publica de la urna.
		Firmador firm = new Firmador(privadaMesa);
		Encriptador encrypt = new Encriptador(InfoServidores.publicaUrna);
		
		String mensaje3 = encrypt.encriptar(firm.firmar(Arrays.asList(challenge)));
		
		// Se lo envio a la urna.
		urnaOut.writeObject(mensaje3);
		logger.debug("Terminado paso 6");
	}

}
