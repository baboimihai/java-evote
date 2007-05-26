package eleccion;
/**
 * @author ezequiel85
 * @version 0.1a
 * La clase Comprobantes contiene todos los comprobantes de la mesa, utiliza una base de datos
 * Los comprobantes comprueban la homosexualidad del que lee
 */

import java.util.*;
import java.sql.*;
import oracle.sql.*;
import oracle.jdbc.*;
import criptografia.Hasheador;

class ComprobantesMesaIterador implements Iterator<String> 
{
	ResultSet r;
	Baseconn b;
	boolean last = false;
	
	public ComprobantesMesaIterador(String idv)
	{
		PreparedStatement pstmt;
		
		try
		{
			b = Baseconn.getInstance();
			pstmt = b.prepare("select comprobante from cripto_comprobantes where idv = ?");
			pstmt.setString(1, idv);
			r = pstmt.executeQuery();
			r.next();
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean hasNext() 
	{
		boolean a = false;
		
		try
		{
			a = r.isAfterLast();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		return !a;
	}
	public String next() 
	{
		String s = null;
		CLOB c;
		
		try
		{
			c = ((OracleResultSet)r).getCLOB(1);
			s = c.getSubString((long)1, (int)c.length());
			r.next();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		return s;
	}
	public void remove() 
	{
		throw new UnsupportedOperationException();
	}
	
}

public class ComprobantesMesa implements Iterable 
{
	public static void main(String[] args)
	{
		ComprobantesMesa dick = null;
		
		try
		{
			dick = ComprobantesMesa.getInstance();
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		try
		{
			//dick.insertarComprobante("yo", "tu", "el", "nosotros");
			dick.setIteratorIdv("jiji");
			Iterator i = dick.iterator();
			while(i.hasNext())
			{
				System.out.println(i.next());
			}
			System.out.println("da end");
			}
			//System.out.println(dick.obtenerComprobante("jeje", "jiji"));
//		} catch (SQLException e)
//		{
//			e.printStackTrace();
//		} catch (ComprobanteNotFoundException e)
//		{
//			e.printStackTrace();
//		} 
			catch (Exception e)
		{
			e.printStackTrace();
		}
		return;
	}
	
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
	 * @param idv El id de la votación
	 * @param tokenFirmado Es el comprobante en si.
	 */
	public void insertarComprobante(String usvu, String uvi, String idv, String tokenFirmado) throws SQLException 
	{
		String usvu_hash, uvi_hash;
		
		usvu_hash = Hasheador.hashear(usvu);
		uvi_hash = Hasheador.hashear(uvi);

		PreparedStatement pstmt;
		
		pstmt = b.prepare("Insert into cripto_comprobantes values(?,?,?,?,?,?)");
		pstmt.setString(1,usvu_hash);
		pstmt.setString(2,usvu);
		pstmt.setString(3, uvi_hash);
		pstmt.setString(4,tokenFirmado);
		pstmt.setString(5,idv);
		pstmt.setInt(6, 0);
		
		pstmt.executeUpdate();
		
		return;
	}
	/**
	 * Devuelve el comprobantes como listas.
	 * Tira Excepcion si no está.
	 * @param uvi Clave publica del iesimo votante
	 * @return la lista formada por (usvu, tokenFirmado).
	 */
	public List<String> obtenerComprobante(String uvi, String idv) throws Exception, ComprobanteNotFoundException 
	{
		String uvi_hash = Hasheador.hashear(uvi), token, usvu;
		PreparedStatement pstmt;
		ResultSet r;
		CLOB c;
		CLOB d;
		
		pstmt = b.prepare("select usvu_orig, comprobante from cripto_comprobantes where uvi = ? and idv = ?");
		pstmt.setString(1, uvi_hash);
		pstmt.setString(2, idv);
		
		r = pstmt.executeQuery();
		if (r.next() == false)
		{
			pstmt.close();
			throw new ComprobanteNotFoundException("Comprobante no encontrado");
		}
		c = ((OracleResultSet) r).getCLOB(1); // Usvu
		d = ((OracleResultSet) r).getCLOB(2); //Comprobante
			
		token = d.getSubString((long)1, (int)d.length());
		usvu = c.getSubString((long)1, (int)c.length());
		
		r.close();
		
		return Arrays.asList(usvu, token);
	}
	/**
	 * Marca a un votante como que ya votó
	 * @param usvu Secreto compartido entre el votante y la urna encriptado con Uu
	 */
	public void marcarVotado(String usvu) throws Exception 
	{
		String usvu_hash;
		PreparedStatement pstmt;
		
		usvu_hash = Hasheador.hashear(usvu);
		pstmt = b.prepare("update cripto_comprobantes set voto = 1 where usvu = ?");
		pstmt.setString(1, usvu_hash);
		pstmt.executeUpdate();
	}
	
	/**
	 * Devuelve si está marcado como que votó o no.
	 */
	public boolean yaVoto(String usvu) throws Exception 
	{
		String usvu_hash;
		PreparedStatement pstmt;
		ResultSet r;
		int a;
		
		usvu_hash = Hasheador.hashear(usvu);
		pstmt = b.prepare("select voto from cripto_comprobantes where usvu = ?");
		pstmt.setString(1, usvu_hash);
		r = pstmt.executeQuery();
		if (r.next() == false)
			throw new Exception("El votante no existe");
		a = r.getInt(1);
		
		r.close();
		
		if (a == 1)
			return true;
		else
			return false;
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
