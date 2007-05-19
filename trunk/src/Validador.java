import java.util.List;
import java.util.Vector;

/**
 * Esta clase implementa el validador. Se utiliza para validar que un mensaje firmado
 * sea válido.
 */
public class Validador {

	/**
	 * Construye un nuevo validador a partir de una clave pública
	 * @param key clave a usar para el validador
	 * @throws Exception si la clave es inválida
	 */
	public Validador (String key) throws Exception {
		return;
	}

	/**
	 * Valida un mensaje
	 * @param mensaje el mensaje a validar
	 * @return una lista de string con todos los mensajes validados
	 * @throws Exception si el mensaje es inválido
	 */
	public List<String> validar (String mensaje) throws Exception {
		Vector<String> aVect = new Vector<String>();

		aVect.add("MENSAJE");
		aVect.add("VALIDO");

		return aVect;
	}

}
