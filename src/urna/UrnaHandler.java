package urna;

/**
 * @author hrajchert
 * @version 0.1a
 * Esta es la clase urna, encargada de bla bla bla.
 */
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.log4j.Logger;

import criptografia.Desencriptador;
import criptografia.Encriptador;
import criptografia.Firmador;
import criptografia.Hasheador;
import criptografia.Validador;

import eleccion.Boletas;
import eleccion.ComprobanteNotFoundException;
import eleccion.ComprobantesUrna;
import eleccion.InfoServidores;

import org.apache.log4j.*;
public class UrnaHandler extends Thread {

	// Variables de conexión hacia el votante
	private Socket votante;
	//private ObjectInputStream votanteIn; TODO Ver porque no se usa
	private ObjectOutputStream votanteOut;

	// Variables de conexión hacia la urna
	private Socket mesa;
	private ObjectInputStream mesaIn;
	private ObjectOutputStream mesaOut;

	// Propiedades de esta transacción
	private String svu;
	private String usvu;
	private String idv;
	private String sobre;
	private String challenge;

	// Clave privada de la urna
	private String privadaUrna;
	// Clase de log
	private Logger logger;
	
	public UrnaHandler(Socket aMesa)	throws IOException {
		// Accedo al log del urna handler
		logger=Logger.getLogger("evote.urna.handler");
		PropertyConfigurator.configure(InfoServidores.log4jconf);
		
		mesa = aMesa;
		mesaOut = new ObjectOutputStream(mesa.getOutputStream());
		mesaIn = new ObjectInputStream(mesa.getInputStream());

		privadaUrna = InfoServidores.readKey(InfoServidores.privadaUrnaPath);
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
//			TODO: Si no se completa el paso6 se tiene que volver a habilitar el svu
		}
		catch (Exception e) {
			logger.error("Fallo la votacion",e );
		}
	}


	public void recPaso2() throws Exception {
		logger.info("Recibo un pedido de la mesa");
		// Levanto el mensaje del socket mesa
		String msg_enc = (String) mesaIn.readObject();

		// Lo desencripto con la clave publica de la urna
		Desencriptador decrypt = new Desencriptador(privadaUrna);
		String tokenFirmado = decrypt.desencriptarString(msg_enc);

		// Verifico que el svu este firmado por la Mesa
		Validador valid = new Validador(InfoServidores.publicaMesa);
		List<String> token = valid.validar(tokenFirmado);

		if (token.size() != 2) throw new Exception("El token esta mal formado");

		// Guardo el id de la votacion.
		this.idv = token.get(1);

		// Guardo el usvu para mandarselo a la mesa despues
		this.usvu = token.get(0);
		
		// Desencripto el usvu con mi clave privada
		this.svu = decrypt.desencriptarString(this.usvu);

		// Me fijo si existe el comprobante, si no lo inserto
		try {
			// Si no existe lanza excepcion ComprobanteNotFoundException
			// caso contrario me fijo si no votó.
			if (!ComprobantesUrna.getInstance().getEstado(svu).equals("no voto"))
				throw new Exception("El usuario ya voto o esta en proceso");

		}
		catch (ComprobanteNotFoundException e) {
			// Guardo el token firmado como comprobante
			ComprobantesUrna.getInstance().insertarComprobante(svu, idv, tokenFirmado);
		}
		logger.debug("Termino el paso 2");
	}
	public void recPaso4() throws Exception  {
		logger.info("Espero al votante");
		// Le pido a UrnaManager por el mensaje 4 (bloqueante).
		List aList = UrnaManager.getInstance().getVotante(svu);
		logger.info("Llego el votante");
		
		// Guardo el socket del votante
		votante = (Socket) aList.get(0);
		//votanteIn = new ObjectInputStream(votante.getInputStream());
		votanteOut = new ObjectOutputStream(votante.getOutputStream());

		// Guardo el sobre
		sobre = (String) aList.get(1);
		logger.debug("Termino el paso 4");
	}

	public void envPaso5() throws Exception {
		logger.info("Le aviso a la mesa que el votante ya voto");
		// Creo un string aleatorio challenge
		SecureRandom random = new SecureRandom();
		byte bytes[] = random.generateSeed(20);
		random.nextBytes(bytes);
		this.challenge = new String(bytes);

		// Encripto con la clave publica de la mesa el svu encriptado y el challenge
		Encriptador encrypt = new Encriptador();
		String msg = encrypt.encriptar(Arrays.asList(usvu, challenge), InfoServidores.publicaMesa);
		logger.debug("Challenge = " + challenge);
		// Envio el mensaje a la mesa.
		mesaOut.writeObject(msg);
		logger.debug("Termino el paso 5");
	}
	public void recPaso6() throws Exception{
		logger.debug("Espero el ACK de la mesa");
		// Agarro el msg de la mesa
		String msg_enc = (String) mesaIn.readObject();
		logger.info("Llego el ACK de la mesa");
		logger.debug("Challenge encriptado = " + msg_enc);
		// Lo desencripto con mi clave privada
		Desencriptador decrypt = new Desencriptador(privadaUrna);
		String msg = decrypt.desencriptarString(msg_enc);

		logger.debug("Challenge firmado = " + msg);
		
		// Verifico que obtenga el challenge firmado por la mesa
		Validador valid = new Validador(InfoServidores.publicaMesa);
		logger.debug("Challenge validado = " + valid.validarString(msg));
		if (!challenge.equals(valid.validarString(msg))) throw new Exception("Fallo el check del challenge");

		// Guardo al sobre en la base y marco que ya votó.
		Boletas.getInstance().insertarBoleta(idv, svu, sobre);
		ComprobantesUrna.getInstance().setEstado(svu, new String("ya voto"));
		logger.debug("Termino el paso6");
	}
	public void envPaso7() throws InvalidKeyException, IOException {
		logger.info("Envio el comprobante al votante");
		// Hago un hash del sobre
		String msg_hash = Hasheador.hashear(sobre);

		// Lo firmo
		Firmador firm = new Firmador(privadaUrna);
		String msg7 = firm.firmar(msg_hash);

		// Lo envio al votante
		votanteOut.writeObject(msg7);
		logger.debug("Termino el paso 7");
	}

}
