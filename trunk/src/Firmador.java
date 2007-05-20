import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.NoSuchPaddingException;

/**
 * Esta clase implementa el firmador. Se utiliza para firmar mensajes con una
 * clave dada.
 */
public class Firmador {

	private Encriptador encriptador;

	/**
	 * Construye un nuevo firmador a partir de una clave privada
	 * @param key clave a usar para el firmador
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws Exception si la clave es inválida
	 */
	public Firmador (String key) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
		encriptador = new Encriptador(key);
	}

	/**
	 * Firma una lista de mensajes
	 * @param messageList lista de mensajes a firmar
	 * @return un String con todos los mensajes firmados
	 * @throws InvalidKeyException si no se puede firmar
	 */
	public String firmar (List<String> messageList) throws InvalidKeyException {
		return encriptador.encriptar(messageList);
	}

	/**
	 * Firma un mensaje
	 * @param message: mensaje a firmar
	 * @return el mensaje firmado con la clave privada
	 * @throws InvalidKeyException si no se puede firmar
	 */
	public String firmar (String message) throws InvalidKeyException {
		return encriptador.encriptar(message);
	}

}
