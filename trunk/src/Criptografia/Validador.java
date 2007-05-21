package Criptografia;
import java.util.List;
import java.io.IOException;
import java.security.InvalidKeyException;
//import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;


/**
 * Esta clase implementa el validador. Se utiliza para validar que un mensaje firmado
 * sea v�lido.
 * @author be
 */
public class Validador {

	private Desencriptador desencriptador;
	//firmar en este programa es lo mismo que encriptar. No se usan hashes.


	/**
	 * Construye un nuevo validador a partir de una clave p�blica
	 * @param key clave a usar para el validador
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 */
	public Validador (String key) throws InvalidKeyException {
		desencriptador = new Desencriptador(key.replaceFirst("public", "private"));
	}


	/**
	 * Valida un mensaje
	 * @param mensaje el mensaje a validar
	 * @return una lista de string con todos los mensajes validados
	 * @throws IOException si el mensaje est� mal codificado
	 * @throws InvalidKeyException si no se puede validar contra la firma usada
	 */
	public List<String> validar (String mensaje) throws InvalidKeyException, IOException  {
		return desencriptador.desencriptar(mensaje);
	}


	/**
	 * Valida un �nico mensaje
	 * @param mensaje: el mensaje a validar
	 * @return una cadena con el mensaje validado
	 * @throws InvalidKeyException si el mensaje no se pudo validar contra la firma
	 * @throws IOException si el mensaje ten�a un encoding distinto a base64
	 */
	public String validarString (String mensaje) throws InvalidKeyException, IOException {
		return desencriptador.desencriptarString(mensaje);
	}
}
