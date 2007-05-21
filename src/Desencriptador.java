import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import sun.misc.BASE64Decoder;
//import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
//import com.sun.org.apache.xml.internal.security.utils.Base64;


/**
 * Esta clase implementa el desencriptador de mensajes. Se utiliza para desencriptar
 * un mensaje con una clave dada.
 * @author be
 */
public class Desencriptador {

	// la clave que usará este desencriptador
	private static KeyFactory fact = null;
	// creamos un cifrador RSA
	private static Cipher cifrador = null;
	private int keyLen = 0;
    private PrivateKey privKey = null;
    private BASE64Decoder b64 = new BASE64Decoder();


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
	 * Construye un nuevo desencriptador
	 * @param key: Clave privada usada para desencriptar
	 * @throws InvalidKeyException si la clave es inválida
	 * <p/>
	 * <b>Formato de la clave</b>
	 * <code>private = clave\n
	 * modulus = modulo\n</code>
	 */
	public Desencriptador (String key) throws InvalidKeyException {
		startDesencriptador(key);
	}


	/**
	 * Construye un nuevo desencriptador sin clave
	 */
	public Desencriptador() {

	}

	/**
	 * Inicializa el desencriptador parseando el string con la clave e iniciando el cifrador
	 * @param key clave a usar
	 * @throws InvalidKeyException si la clave es inválida
	 */
	private void startDesencriptador (String key) throws InvalidKeyException {
		BigInteger modulo = null;
		BigInteger exponente = null;

		// parseamos la key que nos llega
		Pattern parser = Pattern.compile("^private = ([0-9]+)\nmodulus = ([0-9]+)$");
		Matcher match = parser.matcher(key);

		if (! match.matches()) {
			// Si la clave está mal formada, tiramos exception
			throw new InvalidKeyException("El formato de la clave es incorrecto");
		}

		try
		{
			// levantamos los tokens de las claves en los bigints
			exponente = new BigInteger(match.group(1));
			modulo = new BigInteger(match.group(2));

			//creamos la nueva clave
			privKey = fact.generatePrivate(new RSAPrivateKeySpec(modulo, exponente));

			// e iniciamos el cifrador
			cifrador.init(Cipher.DECRYPT_MODE, privKey);
			keyLen = cifrador.getOutputSize(1/*indisinto para RSA*/);
		} catch (NumberFormatException ex) {
			throw new InvalidKeyException("La clave no es un número válido");
		} catch (InvalidKeySpecException e) {
			throw new InvalidKeyException("La clave no válida");
		} catch (InvalidKeyException e) {
			throw new InvalidKeyException("No se puede descifrar con esta clave");
		}
	}


	/**
	 * Core de desencripción. Este método desencripta un mensaje
	 * @param mensaje arreglo de bytes a desencriptar
	 * @return un string con los bytes desencriptados
	 * @throws IllegalBlockSizeException si el mensaje era muy largo
	 * @throws InvalidKeyException si la clave no es válida
	 */
	private String desencriptarBase (byte[] mensaje) throws IllegalBlockSizeException, InvalidKeyException {
		byte desencText[] = null;

		try {
			desencText = cifrador.doFinal(mensaje);
		} catch (IllegalBlockSizeException e) {
			throw new IllegalBlockSizeException("Mensaje muy largo");
		} catch (BadPaddingException e) {
			throw new InvalidKeyException("O la clave era inválida, o realmente hubo un problema raro");
		}
		return new String(desencText);
	}


	/**
	 * @param mensaje: mensaje a desencriptar
	 * @return un String con el mensaje desencriptado
	 * @throws InvalidKeyException si la clave no pudo desencriptar el mensaje
	 * @throws IOException si el mensaje no tiene codificación base64 (léase: lo tocaron)
	 */
	public String desencriptarString (String mensaje) throws InvalidKeyException, IOException {

		StringBuffer rta = new StringBuffer();
		byte[] mess = b64.decodeBuffer(mensaje);
		int pos = 0;

		/* Algoritmo: descodear de base64 y partir los bytes en bloques del tamaño de la clave
		 * para así poder desencriptarlos y finalmente appendear al string
		 * */
		try {
			while ( pos < mess.length) {
				byte[] aux = new byte[keyLen];
				for (int i = 0; i < aux.length; i++)
					aux[i] = mess[pos++];
				rta.append(desencriptarBase(aux));
			}
		} catch (IllegalBlockSizeException e) {
			System.out.println("Esto debería manejarse aquí: " + e.getMessage());
			e.printStackTrace();
		}

		return rta.toString();
	}


	/**
	 * Desencripta un String conteniendo una lista de strings
	 * @param mensaje: mensaje a desencriptar
	 * @return Una lista con Strings desencriptados
	 * @throws InvalidKeyException si la clave no pudo desencriptar el mensaje
	 * @throws IOException si el mensaje no tiene codificación base64 (léase: lo tocaron)
	 */
	public List<String> desencriptar (String mensaje) throws InvalidKeyException, IOException {

		// lista = destino
		List<String> lista = new Vector<String>();

		// Mandamos a desencriptar el string y lo convertimos en lista
		String desencriptado = desencriptarString (mensaje);

		// parseamos el mensaje que queda
		/* Algoritmo:
		 * usando expresiones regulares, levanto el largo del texto
		 * tomo un substring con ese largo, los guardo en la lista,
		 * y pido la misma expresión regular para el resto del texto
		 * ESTO DEBERÍA SER RECURSIVO, pero así es más rápido de codear :(
		 */
		Pattern p = Pattern.compile("__largo:([1-9][0-9]*)item__(.*)");
		Matcher m = p.matcher(desencriptado);

		try {
			while (m.find())
			{
				lista.add(m.group(2).substring(0, Integer.decode(m.group(1))));
				m = p.matcher(m.group(2).substring(Integer.decode(m.group(1))));
			}
		} catch (Exception e) {
			System.out.println("Error en el mini-protocolo: " + e.getMessage());
		}
		return lista;

	}


	/**
	 * Desencripta un mensaje con una clave dada y retorna una lista de mensajes
	 * @param msg mensaje a desencriptar
	 * @param key clave a usar
	 * @return una lista de mensajes
	 * @throws InvalidKeyException si la clave es inválida
	 * @throws IOException si no está bien codificado
	 */
	public List<String> desencriptar (String msg, String key) throws InvalidKeyException, IOException {
		startDesencriptador(key);
		return desencriptar(msg);
	}


	/**
	 * Desencripta un mensaje con una clave dada y retorna el mensaje desencriptado
	 * @param msg mensaje a desencripta
	 * @param key clave a utilizar
	 * @return un String con el mensaje desencriptado
	 * @throws InvalidKeyException si la clave es inválida
	 * @throws IOException si está mal codificado
	 */
	public String desencriptarString (String msg, String key) throws InvalidKeyException, IOException {
		startDesencriptador(key);
		return desencriptarString(msg);
	}
}