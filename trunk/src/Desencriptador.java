import java.util.List;
import java.util.Vector;

/**
 * Esta clase implementa el desencriptador de mensajes. Se utiliza para desencriptar
 * un mensaje con una clave dada.
 */
public class Desencriptador {

	/**
	 * Construye un nuevo desencriptador
	 * @param key Clave privada usada para desencriptar
	 */
	public Desencriptador (String key) throws Exception {
		return;
	}

	/**
	 * Desencripta un mensaje
	 * @param mensaje
	 * @return
	 */
	public List<String> desencriptar (String mensaje) throws Exception {
		Vector<String> aVect = new Vector<String>();

		aVect.add("MENSAJE");
		aVect.add("DESENCRIPTADO");
		aVect.add("Fin");

		return aVect;
	}



}