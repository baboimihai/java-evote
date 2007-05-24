package jtest;

import java.util.*;

import criptografia.Desencriptador;
import criptografia.Encriptador;

import eleccion.InfoServidores;
import junit.framework.TestCase;

public class TestCripto extends TestCase {

	private Encriptador encrypt;
	private Desencriptador decrypt;
	private Desencriptador decrypt_err;
	private Encriptador encrypt_str;
	private Desencriptador decrypt_str;

	private String msg1;
	private String msg2;
	private String msgLargo;
	private List<String> lista1;
	private List<String> lista2;
	private List<String> lista3;
	private String clavePrivada;
	private String clavePublica;
	private String clavePrivadaErr;
	protected void setUp() throws Exception {
		super.setUp();
		InfoServidores.inicializarClaves();
		clavePrivada = InfoServidores.readKey(InfoServidores.privadaMesaPath);
		clavePrivadaErr = InfoServidores.readKey(InfoServidores.privadaUrnaPath);
		clavePublica = InfoServidores.publicaMesa;
		
		encrypt = new Encriptador(clavePublica);
		decrypt = new Desencriptador(clavePrivada);

		decrypt_err = new Desencriptador(clavePrivadaErr);
		
		encrypt_str = new Encriptador();
		decrypt_str = new Desencriptador();
		msg1 = new String("Hola");
		msg2 = new String("Que tal");
		msgLargo = new String("Suppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppper mensaje");
		lista1 = new LinkedList<String>();
		lista2 = new Vector<String>();
		lista1.add(msg1);
		lista1.add(msg2);
		lista2.add(msg1);
		lista2.add(msg2);
		lista3 = Arrays.asList(msg1, msg2);
	}

	public final void testEncriptarString() throws Exception {
		
		String msg_enc = encrypt.encriptar(msg1);
		 System.out.println(msg_enc);
		 assertEquals(msg1, decrypt.desencriptarString(msg_enc));
		 
		 msg_enc = encrypt.encriptar(msgLargo);
		 System.out.println(msg_enc);
		 assertEquals(msgLargo, decrypt.desencriptarString(msg_enc));
		
		 String aux= null;
		 try {
			 aux = decrypt_err.desencriptarString(msg_enc);
			 fail("tiene que fallar");
		 }catch (Exception e) {
			assertNull(aux);
		}
		
	}
	
	public final void testEncriptarListOfString() throws Exception {
		 String msg_enc = encrypt.encriptar(lista1);
		 assertEquals(lista1, decrypt.desencriptar(msg_enc));
		 assertEquals(lista2, decrypt.desencriptar(msg_enc));
		 assertEquals(lista3, decrypt.desencriptar(msg_enc));
	}

	public final void testEncriptarListOfStringString() throws Exception {
		 String msg_enc = encrypt_str.encriptar(lista1, clavePublica);
		 assertEquals(lista1, decrypt_str.desencriptar(msg_enc, clavePrivada));
	}

	public final void testEncriptarStringString() throws Exception {
		 String msg_enc = encrypt_str.encriptar(msg1, clavePublica);
		 assertEquals(msg1, decrypt_str.desencriptarString(msg_enc,clavePrivada));

	}

}
