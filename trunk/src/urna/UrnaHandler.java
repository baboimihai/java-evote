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

import criptografia.Desencriptador;
import criptografia.Encriptador;
import criptografia.Firmador;
import criptografia.Hasheador;
import criptografia.Validador;

import eleccion.Boletas;
import eleccion.ComprobanteNotFoundException;
import eleccion.ComprobantesUrna;
import eleccion.InfoServidores;

public class UrnaHandler extends Thread {

	// Variables de conexión hacia el votante
	private Socket votante;
	private ObjectInputStream votanteIn;
	private ObjectOutputStream votanteOut;
	
	// Variables de conexión hacia la urna
	private Socket mesa;
	private ObjectInputStream mesaIn;
	private ObjectOutputStream mesaOut;
	
	// Propiedades de esta transacción
	private String svu;
	private String idv;
	private String sobre;
	private String challenge;
	
	// Clave privada de la urna
	private String privadaUrna;
	public UrnaHandler(Socket aMesa)	throws IOException {
		mesa = aMesa;
		mesaIn = new ObjectInputStream(mesa.getInputStream());
		mesaOut = new ObjectOutputStream(mesa.getOutputStream());
		
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
			e.printStackTrace();
		}
	}	
	
	
	public void recPaso2() throws Exception {
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
		
		// Desencripto el svu con mi clave privada
		this.svu = decrypt.desencriptarString(token.get(0));
		
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
		
	}
	public void recPaso4() throws Exception  {
		// Le pido a UrnaManager por el mensaje 4 (bloqueante).
		List aList = UrnaManager.getInstance().getVotante(svu);
		
		// Guardo el socket del votante
		votante = (Socket) aList.get(0);
		votanteIn = new ObjectInputStream(votante.getInputStream());
		votanteOut = new ObjectOutputStream(votante.getOutputStream());

		// Guardo el sobre
		sobre = (String) aList.get(1);
	}
	
	public void envPaso5() throws Exception {
		// Creo un string aleatorio challenge
		SecureRandom random = new SecureRandom();
		byte bytes[] = random.generateSeed(20);
		random.nextBytes(bytes);
		this.challenge = new String(bytes);

		// Encripto svu con mi clave publica
		Encriptador encrypt = new Encriptador();
		String usvu = encrypt.encriptar(this.svu, InfoServidores.publicaUrna);
				
		// Encripto con la clave publica de la mesa el svu encriptado y el challenge
		String msg = encrypt.encriptar(Arrays.asList(usvu, challenge), InfoServidores.publicaMesa);
		
		// Envio el mensaje a la mesa.
		mesaOut.writeObject(msg);
	}
	public void recPaso6() throws Exception{
		// Agarro el msg de la mesa
		String msg_enc = (String) mesaIn.readObject();
		
		// Lo desencripto con mi clave privada
		Desencriptador decrypt = new Desencriptador(privadaUrna);
		String msg = decrypt.desencriptarString(msg_enc);
		
		// Verifico que obtenga el challenge firmado por la mesa
		Validador valid = new Validador(InfoServidores.publicaMesa);
		if (!challenge.equals(valid.validarString(msg))) throw new Exception("Fallo el check del challenge");
		
		// Guardo al sobre en la base y marco que ya votó.
		Boletas.getInstance().insertarBoleta(idv, svu, sobre);
		ComprobantesUrna.getInstance().setEstado(svu, new String("ya voto"));
		
	}
	public void envPaso7() throws InvalidKeyException, IOException {
		// Hago un hash del sobre
		String msg_hash = Hasheador.hashear(sobre);
		
		// Lo firmo
		Firmador firm = new Firmador(privadaUrna);
		String msg7 = firm.firmar(msg_hash);
	
		// Lo envio al votante
		votanteOut.writeObject(msg7);
	}

}
