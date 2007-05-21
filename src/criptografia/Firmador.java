package criptografia;
import java.security.InvalidKeyException;
import java.util.List;


/**
 * Esta clase implementa el firmador. Se utiliza para firmar mensajes con una
 * clave dada.
 * @author be
 */
public class Firmador {

	private Encriptador encriptador;
	//firmar en este programa es lo mismo que encriptar. No se usan hashes.


	/**
	 * Construye un nuevo firmador a partir de una clave privada
	 * @param key clave a usar para el firmador
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws Exception si la clave es inválida
	 */
	public Firmador (String key) throws InvalidKeyException {
		encriptador = new Encriptador(key.replaceFirst("private", "public"));
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
