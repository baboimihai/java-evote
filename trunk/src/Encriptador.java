import java.util.List;

/**
 * Esta clase implementa el encriptador. Se utiliza para encriptar una lista de mensajes
 * con una clave dada.
 */
public class Encriptador {

	/**
	 * Construye un nuevo encriptador a partir de una clave
	 * @param key clave pública usada para encriptar
	 * @throws Exception si la clave key es inválida
	 * <h1>nota, voy a revisar el tipo de exception</h1>
	 */
	public Encriptador (String key) throws Exception{
		return;
	}

	/**
	 * encripta un string usando la clave del constructor
	 * @param mensaje mensaje a encriptar
	 * @return mensaje encriptado
	 */
/*	private String encriptar (String mensaje) {
		return "MENSAJE ENCRIPTADO";
	}*/

	/**
	 * Encripta una lista de mensajes y retorna un string con los mensajes concatenados
	 * y encriptados con la clave ingresada en el constructor
	 * @param messageList Lista de mensajes a encriptar
	 * @return Retorna un string con el contenido de la lista encriptado
	 */
	public String encriptar (List<String> messageList) {
		return "MENSAJE ENCRIPTADO";
	}

}
