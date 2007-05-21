import java.math.BigInteger;
import java.security.SecureRandom;



public class GenKeys {

	/**
	 * genera un par de claves.
	 * bitsLargo largo de la clave, si es 0 => default 1024
	 * @return 1ro la pública y después la privada
	 */
	public static String[] generakeys (int bitsLargo) {

		if (bitsLargo == 0)
			bitsLargo = 1024;

		String[] pair = new String[2];
		BigInteger one      = new BigInteger("1");
		SecureRandom random = new SecureRandom();

		BigInteger p = BigInteger.probablePrime(bitsLargo/2, random);
		BigInteger q = BigInteger.probablePrime(bitsLargo/2, random);
		BigInteger phi = (p.subtract(one)).multiply(q.subtract(one));

		BigInteger publicKeyN = new BigInteger("65537");     // common value in practice = 2^16 + 1
		BigInteger privateKeyN = publicKeyN.modInverse(phi);
		BigInteger modulus = p.multiply(q);

		pair[0] = new String("public = " + publicKeyN + "\nmodulus = " + modulus);
		pair[1] = new String("private = " + privateKeyN + "\nmodulus = " + modulus);

		return pair;
	}
}

