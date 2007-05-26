package jtest;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import eleccion.ComprobantesUrna;

public class TestComprobanteUrna extends TestCase {

	private Map<String, String> votantes;
	protected void setUp() throws Exception {
		super.setUp();
		votantes = new HashMap<String, String>();
		votantes.put("svupepe", "token1");
		votantes.put("svupepe2", "token2");
		votantes.put("svupepe3", "token3");
		votantes.put("svupepe4", "token4");
		votantes.put("svupepe5", "token5");
	}

	public final void testGetInstance() throws Exception {
		ComprobantesUrna.getInstance();
	}

	public final void testInsertarComprobante() throws Exception {
		for (String aKey : votantes.keySet()) {
			System.out.println("Insertando:" + aKey);
					ComprobantesUrna.getInstance().insertarComprobante(aKey, "vot1", votantes.get(aKey));
		}
		ComprobantesUrna.getInstance().insertarComprobante("svusas", "vot2", "saraza");
		try {
			ComprobantesUrna.getInstance().insertarComprobante("svupepe", "vot1", "saraza");
			 fail("Se inserto un repetido");
		 }catch (Exception e) {
			//todo ok
			 System.out.println("Funco loca");
		}
	}

	public final void testGetComprobante() throws Exception {
		assertEquals("token1", ComprobantesUrna.getInstance().getComprobante("svupepe"));
	}

	public final void testSetEstado() throws Exception {
		ComprobantesUrna.getInstance().setEstado("svupepe", "chau");
	}

	public final void testGetEstado() throws Exception {
		assertEquals("chau", ComprobantesUrna.getInstance().getEstado("svupepe"));
	}
/*
	public final void testIterator() {
		ComprobantesUrna.getInstance().setIteratorIdv("vot1");
		for (String aIdv : ComprobantesUrna.getInstance()) {
			assertNotNull(aIdv);
		}
	}
*/
}
