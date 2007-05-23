package eleccion;
//
//TODO: Falta definir pero tengo noni
//

import java.lang.Iterable;
import java.util.Iterator;

class ComprobantesUrnaIterador implements Iterator<String> {
	public ComprobantesUrnaIterador(String idv) {
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
	private String idv;

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
	 * Inserto un comprobante en la base. El estado inicial es "no voto".
	 * @param svu El secreto compartido entre la urna y el votante (único).
	 * @param idv El id de la votación
	 * @param tokenFirmado El comprobante en si.
	 * @throws Exception Si no se pudo insertar el comprobante.
	 */
	public void insertarComprobante(String svu, String idv, String tokenFirmado) throws Exception 
	{
	}
	
	/**
	 * Devuelve el comprobante para un svu dado.
	 * @param svu El secreto compartido entre la urna y el votante (único). 
	 * @return El token firmado asociado a ese svu.
	 * @throws ComprobanteNotFoundException Si no se encontró el comprobante.
	 */
	public String getComprobante(String svu) throws ComprobanteNotFoundException
	{
		return null;
	}
	
	/**
	 * Cambia el estado de la votacion de svu.
	 * @param svu El secreto compartido entre la urna y el votante (único).
	 * @param estado Puede ser "no voto", "en proceso" o "ya voto".
	 * @throws ComprobanteNotFoundException Si no se encontró el comprobante.
	 */
	public void setEstado(String svu, String estado) throws ComprobanteNotFoundException
	{
		
	}
	
	/**
	 * Devuelve el estado de svu.
	 * @param svu El secreto compartido entre la urna y el votante (único).
	 * @return El estado del svu. Puede ser "no voto", "en proceso" o "ya voto".
	 * @throws ComprobanteNotFoundException Si no se encontró el comprobante.
	 */
	public String getEstado(String svu) throws ComprobanteNotFoundException
	{
		return null;
	}
	
	public void setIteratorIdv(String idv)
	{
		this.idv = idv;
	}
	
	public Iterator<String> iterator()
	{
		return new ComprobantesUrnaIterador(idv);
	}
}
