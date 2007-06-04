package eleccion;
/**
 * @author ezequiel85
 * @version 10.1.2
 * Esta clase sirve para comprobar que nadie lee estos comentarios y que uno
 * los escribe mas al pedo que bocina de avion
 * SALUD!!
 */

import java.util.*;
import java.sql.*;

import oracle.sql.*;
import oracle.jdbc.*;
import criptografia.Hasheador;

class ComprobantesUrnaIterador implements Iterator<String>
{
	ResultSet r;
	Baseconn b;
	boolean last = false;

	public ComprobantesUrnaIterador(String idv)
	{
		PreparedStatement pstmt;

		try
		{
			b = Baseconn.getInstance();
			pstmt = b.prepare("select comprobante from cripto_comprobantes_urna where idv like ?");
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
public class ComprobantesUrna implements Iterable {

	// Variable que contiene la única instancia de ComprobantesUrna.
	private static ComprobantesUrna ref;
	private Baseconn b;
	private String idv;

	//Esta clase es singleton, el constructor es privado
	private ComprobantesUrna() throws ClassNotFoundException, SQLException
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
	 * @return La instancia de ComprobantesUrna
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static synchronized ComprobantesUrna getInstance() throws ClassNotFoundException, SQLException
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
	public void insertarComprobante(String svu, String idv, String tokenFirmado) throws SQLException
	{
		String svu_hash;

		svu_hash = Hasheador.hashear(svu);

		PreparedStatement pstmt;

		pstmt = b.prepare("Insert into cripto_comprobantes_urna values(?,?,?,?)");
		pstmt.setString(1,svu_hash);
		pstmt.setString(2,"no voto");
		pstmt.setString(3, tokenFirmado);
		pstmt.setString(4,idv);


		pstmt.executeUpdate();

		return;
	}

	/**
	 * Devuelve el comprobante para un svu dado.
	 * @param svu El secreto compartido entre la urna y el votante (único).
	 * @return El token firmado asociado a ese svu.
	 * @throws ComprobanteNotFoundException Si no se encontró el comprobante.
	 * @throws SQLException
	 */
	public String getComprobante(String svu) throws ComprobanteNotFoundException, SQLException
	{
		String svu_hash = Hasheador.hashear(svu), comprobante;
		CLOB c;
		ResultSet r;

		PreparedStatement pstmt;
		pstmt = b.prepare("select comprobante from cripto_comprobantes_urna where svu = ?");
		pstmt.setString(1,svu_hash);

		r = pstmt.executeQuery();
		if (r.next() == false)
		{
			r.close();
			throw new ComprobanteNotFoundException("Comprobante no encontrado");
		}

		c = ((OracleResultSet) r).getCLOB(1); // Comprobante;

		comprobante = c.getSubString((long)1, (int)c.length());

		r.close();

		return comprobante;

	}

	/**
	 * Cambia el estado de la votacion de svu.
	 * @param svu El secreto compartido entre la urna y el votante (único).
	 * @param estado Puede ser "no voto", "en proceso" o "ya voto".
	 * @throws ComprobanteNotFoundException Si no se encontró el comprobante.
	 * @throws SQLException
	 */
	public void setEstado(String svu, String estado) throws ComprobanteNotFoundException, SQLException
	{
		PreparedStatement pstmt;
		String svu_hash = Hasheador.hashear(svu);
		int cant;

		pstmt = b.prepare("update cripto_comprobantes_urna set voto = ? where svu = ?");
		pstmt.setString(1, estado);
		pstmt.setString(2, svu_hash);

		cant = pstmt.executeUpdate();
		if (cant < 1)
			throw new ComprobanteNotFoundException("Comprobante no encontrado");
		return;
	}

	/**
	 * Devuelve el estado de svu.
	 * @param svu El secreto compartido entre la urna y el votante (único).
	 * @return El estado del svu. Puede ser "no voto", "en proceso" o "ya voto".
	 * @throws ComprobanteNotFoundException Si no se encontró el comprobante.
	 * @throws SQLException
	 */
	public String getEstado(String svu) throws ComprobanteNotFoundException, SQLException
	{
		PreparedStatement pstmt;
		String svu_hash = Hasheador.hashear(svu);
		ResultSet r;

		pstmt = b.prepare("select voto from cripto_comprobantes_urna where svu = ?");
		pstmt.setString(1, svu_hash);

		r = pstmt.executeQuery();
		if (r.next() == false)
		{
			r.close();
			throw new ComprobanteNotFoundException("Comprobante no encontrado");
		}
		return r.getString(1);
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
