package criptografia;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.math.BigInteger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import sun.misc.BASE64Encoder;
//import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * Esta clase implementa el encriptador. Se utiliza para encriptar una lista de mensajes
 * con una clave dada.
 * @author be
 */
public class Encriptador {

	// la clave que usará este encriptador
	private static KeyFactory fact = null;
    private PublicKey pubKey = null;
	// creamos un cifrador RSA
	private static Cipher cifrador = null;
	// RSAoverhead: bytes que necesita RSA para encriptar. Se descuentan del tamaño máximo
	// del mensaje que es el tamaño de la clave en bytes.
	private static int RSAoverhead = 11;
	private int keyLen = 0;
	private BASE64Encoder b64 = new BASE64Encoder();


	static {
		try {
			// inicializo fact
			fact = KeyFactory.getInstance("RSA");
			cifrador = Cipher.getInstance("RSA/ECB/PKCS1Padding");

		} catch (java.security.NoSuchAlgorithmException e) {
			System.out.println("No deberíamos pasar por acá!! está cableado RSA");
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			System.out.println("No deberíamos pasar por acá!! está cableado RSA/ECB/PKCS1Padding");
			e.printStackTrace();
		}
	}


	/**
	 * Construye un nuevo encriptador a partir de una clave
	 * @param key clave pública usada para encriptar
	 * <h1>nota, voy a revisar el tipo de exception</h1>
	 * @throws InvalidKeyException si la clave está mal formada
	 * @throws Exception en cualquier otro error
	 * <p/>
	 * <b>Formato de la clave</b>
	 * <code>public = clave\n
	 * modulus = modulo\n</code>
	 */
	public Encriptador (String key) throws InvalidKeyException {
		startEncriptador(key);
	}


	/**
	 * Parsea la clave e inicializa el cifrador
	 * @param key clave a usar
	 * @throws InvalidKeyException si la clave está mal formada
	 */
	private void startEncriptador (String key) throws InvalidKeyException {
		BigInteger modulo = null;
		BigInteger exponente = null;

		// parseamos la key que nos llega
		Pattern parser = Pattern.compile("^public = ([0-9]+)\nmodulus = ([0-9]+)$");
		Matcher match = parser.matcher(key);

		if (! match.matches()) {
			// Si la clave está mal formada, tiramos exception
			throw new InvalidKeyException("El formato de la clave es incorrecto");
		}

		try {
			// levantamos los tokens de las claves en los bigints
			exponente = new BigInteger(match.group(1));
			modulo = new BigInteger(match.group(2));

			//creamos la nueva clave
			pubKey = fact.generatePublic(new RSAPublicKeySpec(modulo, exponente));

			// e iniciamos el cifrador
			cifrador.init(Cipher.ENCRYPT_MODE, pubKey);
			keyLen = cifrador.getOutputSize(1/*cualquier valor es indisinto*/);
		} catch (InvalidKeySpecException e) {
			throw new InvalidKeyException("La clave no es válida");
		} catch (InvalidKeyException e) {
			throw new InvalidKeyException("No se puede cifrar con esta clave");
		} catch (NumberFormatException ex) {
			throw new InvalidKeyException("La clave no es un número válido");
		}
	}

	/**
	 * Construye un nuevo encriptador sin clave
	 */
	public Encriptador () {

	}


	/**
	 * Core de encriptación: encripta un mensaje que tiene que tener menos largo que la clave RSA
	 * @param mensaje: el mensaje a encriptar
	 * @return un arreglo de bytes con el mensaje encriptado
	 * @throws BadPaddingException si la clave es inválida
	 * @throws IllegalBlockSizeException si el mensaje es muy largo
	 * @throws InvalidKeyException si el cifrador no funca
	 */
	private byte[] encriptarBase (String mensaje) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
		byte encText[] = null;

		// cifrar el texto
		try {
			encText = cifrador.doFinal(mensaje.getBytes());
		} catch (IllegalBlockSizeException e) {
			throw new IllegalBlockSizeException("Mensaje muy largo");
		} catch (BadPaddingException e) {
			throw new BadPaddingException();
		} catch (IllegalStateException e) {
			throw new InvalidKeyException("No se ha incializado el cifrador");
		}

		return encText;
	}


	/**
	 * Encripta una lista de mensajes y retorna un string con los mensajes concatenados
	 * y encriptados con la clave ingresada en el constructor
	 * @param messageList Lista de mensajes a encriptar
	 * @return Retorna un string con el contenido de la lista encriptado
	 * @throws InvalidKeyException si la clave no es válida
	 */
	public String encriptar (List<String> messageList) throws InvalidKeyException {
		StringBuffer rta = new StringBuffer();

		for (String string : messageList) {
			rta.append(encriptar("__largo:" + string.length() + "item__" + string));
		}
		return rta.toString();

	}


	/**
	 * encripta un string usando la clave del constructor
	 * @param mensaje mensaje a encriptar
	 * @return mensaje encriptado
	 * @throws InvalidKeyException si la clave es inválida
	 */
	public String encriptar (String mensaje) throws InvalidKeyException {

		StringBuffer auxiliar = new StringBuffer(mensaje);
		int outPutLen = mensaje.length() + RSAoverhead; // largo de la salida

		// buscamos el próximo múltiplo del largo de la clave para el tamaño del mensaje
		byte[] rta = new byte[outPutLen + keyLen - outPutLen % keyLen ];
		int pos = 0;

		/* Algoritmo: partir en pedazos de keyLen - RSAoverhead (el tamaño máximo del mensaje
		 * tomar el substring, encriptarlo, eliminar el substring y guardar los bytes en un arreglo
		 * finalmente transformar el arreglo de bytes en un base64 encoding para poder guardarlo
		 * como un string.
		 */
		try {
			while (auxiliar.length() > keyLen - RSAoverhead) {

				byte[] aux = encriptarBase(auxiliar.substring(0, keyLen - RSAoverhead));
				for ( int i = 0; i < aux.length; i++)
					rta[pos++] = aux [i];

				auxiliar.delete(0, keyLen - RSAoverhead);
			}

			byte[] aux = encriptarBase(auxiliar.toString());
			for ( int i = 0; i < aux.length; i++)
				rta[pos++] = aux [i];

		} catch (BadPaddingException e) {
			throw new InvalidKeyException("La clave es inválida, o realmente hubo un problema raro");
		} catch (IllegalBlockSizeException e) {
			System.out.println("Esto debería manejarse aquí: " + e.getMessage());
			e.printStackTrace();
		}

		return b64.encode(rta);
	}


	/**
	 * Encripta una lista de strings con una clave dada
	 * @param msgList lista de mensajes a encriptar
	 * @param key clave a utilizar
	 * @return un String con el contenido de la lista encriptado
	 * @throws InvalidKeyException si la clave que se recibió es inválida
	 */
	public String encriptar(List<String> msgList, String key) throws InvalidKeyException {
		startEncriptador(key);
		return encriptar(msgList);
	}


	/**
	 * Encripta un string con una clave dada
	 * @param msg mensaje a encriptar
	 * @param key clave a utilizar
	 * @return un string con el mensaje encriptado
	 * @throws InvalidKeyException si la clave es inválida
	 */
	public String encriptar(String msg, String key) throws InvalidKeyException {
		startEncriptador(key);
		return encriptar(msg);
	}
}


