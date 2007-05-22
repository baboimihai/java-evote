package eleccion;
//
//TODO: Falta definir pero tengo noni
//

import java.lang.Iterable;
import java.util.Iterator;

class ComprobantesUrnaIterador implements Iterator<String> {
	public ComprobantesUrnaIterador() {
		// TODO Auto-generated constructor stub
	}
	
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}
	public String next() {
		// TODO Auto-generated method stub
		return null;
	}
	public void remove() {
		throw new UnsupportedOperationException();
		
	}
	
}
public class ComprobantesUrna implements Iterable {
	private ComprobantesUrna() {
		//TODO Inicializaciones necesarias.
	}

	// Variable que contiene la única instancia de ComprobantesUrna.
	private static ComprobantesUrna ref;

	/**
	 *  Esta clase es singleton y no se puede clonar. 
	 */
	  public Object clone()	throws CloneNotSupportedException {
	    throw new CloneNotSupportedException(); 
	  }
	
	/**
	 * Devuelve la instancia a la clase.
	 * @return La instancia de ComprobantesUrna
	 */
	public static synchronized ComprobantesUrna getInstance()
	{
		if ( ref == null )
			ref = new ComprobantesUrna();
		return ref;
	}
	
	/**
	 * Inserto un comprobante en la base
	 * @param comprobante Es el comprobante en si.
	 */
	public boolean insertarComprobante(String comprobante) throws Exception {
		return false;
	}
	
	public Iterator<String> iterator(){
		return new ComprobantesMesaIterador();
	}
}
