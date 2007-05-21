import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.math.BigInteger;

/**
 * Esta clase permite hashear cosas :P (el mejor javadoc de mi vida :D)
 */
public class Hasheador {

	// El messageDigest calcula hashes
	private static MessageDigest md = null;


	static {
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("No debería no existir el algoritmo: " + e.getMessage());
			e.printStackTrace();
		}
	}


	/**
	 * Calcula el hash de un string
	 * @param string cadena a hashear
	 * @return un string con el hash del string recibido como parámetro
	 */
	public static String hashear(String string) {
		return new BigInteger(1/* para el unsigned, pucha que costó encontrarlo y estaba debajo de mis narices eh */,
							  md.digest(string.getBytes())).toString(16);
	}


	/**
	 * Calcula el hash de una lista de Strings
	 * @param stringList lista a hashear
	 * @return un string con el hash de la lista recibida como parámetro
	 */
	public static String hashear(List<String> stringList) {
		StringBuffer aux = new StringBuffer();

		for (String string : stringList) {
			aux.append(string);
		}
		return hashear(aux.toString());
	}
}
