import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;



public class GenKeys {

	/**
	 * genera un par de claves.
	 * bitsLargo largo de la clave, si es 0 => default 1024
	 * @return 1ro la p�blica y despu�s la privada
	 */
	public static void main (String [] args) throws Exception {

		int bitsLargo = new Integer(args[0]);
		if (bitsLargo == 0)
			bitsLargo = 1024;
		
		BigInteger one      = new BigInteger("1");
		SecureRandom random = new SecureRandom();

		BigInteger p = BigInteger.probablePrime(bitsLargo/2, random);
		BigInteger q = BigInteger.probablePrime(bitsLargo/2, random);
		BigInteger phi = (p.subtract(one)).multiply(q.subtract(one));

		BigInteger publicKeyN = new BigInteger("65537");     // common value in practice = 2^16 + 1
		BigInteger privateKeyN = publicKeyN.modInverse(phi);
		BigInteger modulus = p.multiply(q);
		

		DataOutputStream priv = new DataOutputStream(
				 new BufferedOutputStream( new FileOutputStream("privada.key")));
		DataOutputStream pub = new DataOutputStream(
				 new BufferedOutputStream( new FileOutputStream("publica.key")));

		String publica = new String("public = " + publicKeyN + "\nmodulus = " + modulus);
		String privada = new String("private = " + privateKeyN + "\nmodulus = " + modulus);
		System.out.println(publica);
		System.out.println(privada);
		pub.writeChars(publica);
		priv.writeChars(privada);
		pub.close();
		priv.close();
	}
}
