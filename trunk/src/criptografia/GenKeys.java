package criptografia;
import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Clase para generar claves. //TODO revisar el exponente público
 * @author be
 */
public class GenKeys {

	/**
	 * genera un par de claves.
	 * bitsLargo largo de la clave, si es 0 => default 1024
	 * @return 1ro la pública y después la privada
	 */
	public static void generarClaves(int bitsLargo, String pathPrivada, String pathPublica) throws Exception {

		if (bitsLargo == 0 || bitsLargo < 1024)
			bitsLargo = 1024;

		if (bitsLargo % 32 != 0)
			bitsLargo = ((bitsLargo + 31) / 32) * 32;

		SecureRandom random = new SecureRandom();
		BigInteger p, q, phi, modulus, two;

		two = new BigInteger("2");
		do {
			p = BigInteger.probablePrime(bitsLargo/2, random);
			q = BigInteger.probablePrime(bitsLargo/2, random);

			if (p.compareTo(q) > 0) {
				BigInteger aux;
				aux = p;
				p = q;
				q = aux;
			}

			modulus = p.multiply(q);
		} while (modulus.bitLength() != bitsLargo);

		phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

		BigInteger publicKeyN = new BigInteger("65537"); //2^16+1 el del gpg

		while (publicKeyN.compareTo(phi) >= 0 && publicKeyN.gcd(phi) != BigInteger.ONE)
			publicKeyN = publicKeyN.add(two);

		BigInteger privateKeyN = publicKeyN.modInverse(phi);



		// Creo los writer para escribir el archivo.
		BufferedWriter priv = new BufferedWriter(new FileWriter(pathPrivada));
		BufferedWriter pub = new BufferedWriter(new FileWriter(pathPublica));

		String publica = new String("public = " + publicKeyN + "\nmodulus = " + modulus);
		String privada = new String("private = " + privateKeyN + "\nmodulus = " + modulus);

		//System.out.println(publica);
		//System.out.println(privada);
		pub.write(publica);
		priv.write(privada);

		// Cierro los archivos
		pub.close();
		priv.close();
	}

	public static void main (String [] args) throws Exception {

		String pathbase = args[0];
		String pathPrivada = pathbase + "_privada.key";
		String pathPublica = pathbase + "_publica.key";
		generarClaves(0, pathPrivada, pathPublica);
	}
}

