package eleccion;
/**
 * @author ezequiel85
 * @version 0.1a
 * La clase Comprobantes contiene todos los comprobantes de la mesa, utiliza una base de datos
 * 
 */

import java.util.*;
import java.sql.*;

class ComprobantesMesaIterador implements Iterator<String> 
{
	public ComprobantesMesaIterador(String idv) 
	{
		// TODO Auto-generated constructor stub
	}
	
	public boolean hasNext() 
	{
		// TODO Auto-generated method stub
		return false;
	}
	public String next() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	public void remove() 
	{
		throw new UnsupportedOperationException();
	}
	
}

public class ComprobantesMesa implements Iterable 
{
	// Variable que contiene la única instancia de Comprobantes.
	private static ComprobantesMesa ref;
	private Baseconn b;
	private String idv;
	
	// El constructor es privado para evitar que lo instancien otras clases
	private ComprobantesMesa() throws ClassNotFoundException, SQLException 
	{
		this.b = Baseconn.getInstance();
	}

	/**
	 *  Esta clase es singleton y no se puede clonar. 
	 */
	  public Object clone()	throws CloneNotSupportedException 
	  {
	    throw new CloneNotSupportedException(); 
	  }
	
	/**
	 * Devuelve la instancia a la clase.
	 * @return La instancia de Comprobantes
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static synchronized ComprobantesMesa getInstance() throws ClassNotFoundException, SQLException
	{
		if ( ref == null )
			ref = new ComprobantesMesa();
		return ref;
	}
	
	/**
	 * Inserto un comprobante en la base
	 * @param usvu Secreto compartido entre el votante y la urna encriptado con Uu
	 * @param uvi Clave publica del iesimo votante
	 * @param tokenFirmado Es el comprobante en si.
	 */
	public boolean insertarComprobante(String usvu, String uvi, String tokenFirmado) throws SQLException 
	{
		b.doQuery("insert into ");
		return false;
	}
	/**
	 * Devuelve el comprobantes como listas.
	 * Tira Excepcion si no está.
	 * @param uvi Clave publica del iesimo votante
	 * @return la lista formada por (usvu, tokenFirmado).
	 */
	public List<String> obtenerComprobante(String uvi, String idv) throws Exception, ComprobanteNotFoundException 
	{
		//TODO: Internamente tenes que buscar en la base todos los token que tengan 
		// el uvi, desencriptarlos (con el uvi) y fijarte si el id es el que te mando
		// yo.
		return null;
	}
	/**
	 * Marca a un votante como que ya votó
	 * @param usvu Secreto compartido entre el votante y la urna encriptado con Uu
	 */
	public void marcarVotado(String usvu) throws Exception 
	{
		
	}
	
	/**
	 * Devuelve si está marcado como que votó o no.
	 */
	public boolean yaVoto(String usvu) throws Exception 
	{
		return true;
	}
	
	public void setIteratorIdv(String idv)
	{
		this.idv = idv;
	}

	public Iterator<String> iterator()
	{
		return new ComprobantesMesaIterador(idv);
	}
}
