import java.util.List;

/**
 * Esta clase implementa el firmador. Se utiliza para firmar mensajes con una
 * clave dada.
 */
public class Firmador {

	/**
	 * Construye un nuevo firmador a partir de una clave privada
	 * @param key clave a usar para el firmador
	 * @throws Exception si la clave es inválida
	 */
	public Firmador (String key) throws Exception {
		return;
	}


	/**
	 * Firma una lista de mensajes
	 * @param messageList lista de mensajes a firmar
	 * @return un String con todos los mensajes firmados
	 */
	public String firmar (List<String> messageList) {
		return "MENSAJE FIRMADO";
	}


}
