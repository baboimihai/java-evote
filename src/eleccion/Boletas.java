package eleccion;
/**
 * @author ezequiel85
 * @version 23.3.5
 * Esta clase sirve para que Ingui sea boleta
 * SALUD, Y PESETAS!!!
 */
import java.lang.Iterable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import criptografia.Hasheador;

class BoletasIterador implements Iterator<String>
{
	ResultSet r;
	Baseconn b;
	boolean last = false;

	public BoletasIterador(String idv)
	{
		PreparedStatement pstmt;

		try
		{
			b = Baseconn.getInstance();
			pstmt = b.prepare("select boleta from cripto_boletas where idv = ?");
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

		try
		{
			s = r.getString(1);
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

public class Boletas implements Iterable
{
	// Variable que contiene la única instancia de Boletas.
	private static Boletas ref;
	private String idv;
	private Baseconn b;

	// El constructor es privado para evitar que lo instancien otras clases
	private Boletas () throws ClassNotFoundException, SQLException
	{
this.b = Baseconn.getInstance();//TODO Inicializaciones necesarias.
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
	 * @return La instancia de Boletas
	 */
	public static synchronized Boletas getInstance() throws ClassNotFoundException, SQLException
	{
		if ( ref == null )
			ref = new Boletas();
		return ref;
	}

	public void setIteratorIdv(String idv)
	{
		this.idv = idv;
	}

	public Iterator<String> iterator()
	{
		return new BoletasIterador(idv);
	}

	public void insertarBoleta(String idv, String svu, String boleta) throws Exception
	{
		String svu_hash;

		svu_hash = Hasheador.hashear(svu);

		PreparedStatement pstmt;

		pstmt = b.prepare("Insert into cripto_boletas values(?,?,?)");
		pstmt.setString(1,svu_hash);
		pstmt.setString(2,idv);
		pstmt.setString(3, boleta);

		pstmt.executeUpdate();

		return;
	}

	public String getBoleta(String svu) throws Exception
	{
		String svu_hash = Hasheador.hashear(svu);
		ResultSet r;

		PreparedStatement pstmt;
		pstmt = b.prepare("select boleta from cripto_boletas where svu = ?");
		pstmt.setString(1,svu_hash);

		r = pstmt.executeQuery();
		if (r.next() == false)
		{
			r.close();
			throw new Exception("Boleta no encontrada");
		}

		r.close();

		return r.getString(1); // Comprobante;
	}

}
