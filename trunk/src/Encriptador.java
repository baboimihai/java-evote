import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
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
 */
public class Encriptador {

	// la clave que usar� este encriptador
	private static KeyFactory fact = null;
    private PublicKey pubKey = null;
	// creamos un cifrador RSA
	private Cipher cifrador = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	// RSAoverhead: bytes que necesita RSA para encriptar. Se descuentan del tama�o m�ximo
	// del mensaje que es el tama�o de la clave en bytes.
	private static int RSAoverhead = 11;
	private int keyLen = 0;
	private BASE64Encoder b64 = new BASE64Encoder();

	static {
		try {
			// inicializo fact
			fact = KeyFactory.getInstance("RSA");
		}
		catch (java.security.NoSuchAlgorithmException e)
		{
			System.out.println("No deber�amos pasar por ac�!! est� cableado RSA");
			e.printStackTrace();
		}
	}


	/**
	 * Construye un nuevo encriptador a partir de una clave
	 * @param key clave p�blica usada para encriptar
	 * <h1>nota, voy a revisar el tipo de exception</h1>
	 * @throws InvalidKeyException si la clave est� mal formada
	 * @throws Exception en cualquier otro error
	 * <p/>
	 * <b>Formato de la clave</b>
	 * <code>public = clave\n
	 * modulus = modulo\n</code>
	 */
	public Encriptador (String key) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
		BigInteger modulo = null;
		BigInteger exponente = null;

		// parseamos la key que nos llega
		Pattern parser = Pattern.compile("^public = ([0-9]+)\nmodulus = ([0-9]+)$");
		Matcher match = parser.matcher(key);

		if (! match.matches()) {
			// Si la clave est� mal formada, tiramos exception
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
			throw new InvalidKeyException("La clave no es v�lida");
		} catch (InvalidKeyException e) {
			throw new InvalidKeyException("No se puede cifrar con esta clave");
		} catch (NumberFormatException ex) {
			throw new InvalidKeyException("La clave no es un n�mero v�lido");
		}
	}


	/**
	 * Core de encriptaci�n: encripta un mensaje que tiene que tener menos largo que la clave RSA
	 * @param mensaje: el mensaje a encriptar
	 * @return un arreglo de bytes con el mensaje encriptado
	 * @throws BadPaddingException si la clave es inv�lida
	 * @throws IllegalBlockSizeException si el mensaje es muy largo
	 */
	private byte[] encriptarBase (String mensaje) throws BadPaddingException, IllegalBlockSizeException {
		byte encText[] = null;

		// cifrar el texto
		try {
			encText = cifrador.doFinal(mensaje.getBytes());
		} catch (IllegalBlockSizeException e) {
			throw new IllegalBlockSizeException("Mensaje muy largo");
		} catch (BadPaddingException e) {
			throw new BadPaddingException();
		}

		return encText;
	}


	/**
	 * Encripta una lista de mensajes y retorna un string con los mensajes concatenados
	 * y encriptados con la clave ingresada en el constructor
	 * @param messageList Lista de mensajes a encriptar
	 * @return Retorna un string con el contenido de la lista encriptado
	 * @throws InvalidKeyException si la clave no es v�lida
	 */
	public String encriptar (List<String> messageList) throws InvalidKeyException {
		StringBuffer rta = new StringBuffer();

		for (String string : messageList) {
			rta.append(encriptar("__largo: " + string.length() + "item__" + string));
		}
		rta.append("__fin");
		return rta.toString();

	}


	/**
	 * encripta un string usando la clave del constructor
	 * @param mensaje mensaje a encriptar
	 * @return mensaje encriptado
	 * @throws InvalidKeyException si la clave es inv�lida
	 */
	public String encriptar (String mensaje) throws InvalidKeyException {

		StringBuffer auxiliar = new StringBuffer(mensaje);
		int outPutLen = mensaje.length() + RSAoverhead; // largo de la salida

		// buscamos el pr�ximo m�ltiplo del largo de la clave para el tama�o del mensaje
		byte[] rta = new byte[outPutLen + keyLen - outPutLen % keyLen ];
		int pos = 0;

		/* Algoritmo: partir en pedazos de keyLen - RSAoverhead (el tama�o m�ximo del mensaje
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
			throw new InvalidKeyException("La clave es inv�lida, o realmente hubo un problema raro");
		} catch (IllegalBlockSizeException e) {
			System.out.println("Esto deber�a manejarse aqu�: " + e.getMessage());
			e.printStackTrace();
		}

		return b64.encode(rta);
	}
}


/* Aqu� comienza el c�digo robado :D */
/*
	SecureRandom random = new SecureRandom();
byte bytes[] = random.generateSeed(12);
random.nextBytes(bytes);
String RandA = new String(bytes);
*/

/*
/**
* Encrypt a text using public key.
* @param text The original unencrypted text
* @param key The public key
* @return Encrypted text
* @throws java.lang.Exception
*
public static byte[] encrypt(byte[] text, PublicKey key) throws Exception
{

byte[] cipherText = null;
// get an RSA cipher object and print the provider
Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

System.out.println("nProvider is: " + cipher.getProvider().getInfo());

// encrypt the plaintext using the public key
cipher.init(Cipher.ENCRYPT_MODE, key);
cipherText = cipher.doFinal(text);
return cipherText;

}*/

/*/**
* Decrypt text using private key
* @param text The encrypted text
* @param key The private key
* @return The unencrypted text
* @throws java.lang.Exception
*
public static byte[] decrypt(byte[] text, PrivateKey key) throws Exception
{

byte[] dectyptedText = null;
// decrypt the text using the private key
Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
cipher.init(Cipher.DECRYPT_MODE, key);
dectyptedText = cipher.doFinal(text);
return dectyptedText;

}*/

//import com.sun.org.apache.xml.internal.security.utils.Base64;
/*private String encoded = new String(Base64.encode("test".getBytes()));*/

/*/**
* Encode bytes array to BASE64 string
* @param bytes
* @return Encoded string
*
private static String encodeBASE64(byte[] bytes)
{

BASE64Encoder b64 = new BASE64Encoder();
return b64.encode(bytes);

}
*//*
/**
* Decode BASE64 encoded string to bytes array
* @param text The string
* @return Bytes array
* @throws IOException
*
private static byte[] decodeBASE64(String text) throws IOException
{

BASE64Decoder b64 = new BASE64Decoder();
return b64.decodeBuffer(text);

}*/
