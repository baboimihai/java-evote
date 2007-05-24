package jtest;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import eleccion.ComprobantesUrna;

import urna.UrnaManager;
import junit.framework.TestCase;

class TestUrnaManagerThread extends Thread {
	String svu;
	List lista;
	public TestUrnaManagerThread(String aSvu, List aLista) {
		this.svu = aSvu;
		this.lista = aLista;
	}

	@Override
	public void run() {
		super.run();
		try {
			List rta = UrnaManager.getInstance().getVotante(svu);
			if ( !rta.equals(lista)) throw new Exception("no es la lista que esperaba");
			System.out.println(svu + " obtubo lo que esperaba");
		}
		catch (Exception e) {
			//e.printStackTrace();
			//System.out.println("Hubo un error: " + e.getMessage());
			System.out.println(Thread.currentThread().getId() + ": Hubo un error: " + e.getMessage());
		}
	}
}


public class TestUrnaManager extends TestCase {

	private List<String> threadList;
	protected void setUp() throws Exception {
		super.setUp();
		threadList = new Vector<String>();
		threadList.add("jose");
		threadList.add("pepe");
		threadList.add("jorge");
		threadList.add("tangalanga");
		threadList.add("suco");
		for (String aSvu : threadList) {
			try {
			ComprobantesUrna.getInstance().insertarComprobante(aSvu, "vot1", aSvu);
			}catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	public final void testGetVotante() {
		for (String aSvu : threadList) {
			List<String> aux = new Vector<String>();
			aux.add(aSvu);
			aux.add("1");
			(new TestUrnaManagerThread(aSvu, aux)).start();
		}
		
	}
	public final void testSetVotante() throws Exception {
		for (String aSvu : threadList) {
			List<String> aux = new Vector<String>();
			aux.add(aSvu);
			aux.add("1");
			UrnaManager.getInstance().setVotante(aSvu, aux);
			Thread.sleep(1000);
		}
	}



}
