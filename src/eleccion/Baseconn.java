package eleccion;
import java.sql.*;

public class Baseconn
{
	private static Baseconn ref;
	private Connection conn;
	
	/**
	 * Constructor privado del padron para evitar que lo creen.
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	private Baseconn() throws ClassNotFoundException, SQLException
	{	
		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		conn = DriverManager.getConnection("jdbc:odbc:oracle", "mbesio", "NaN");
	}
	
	/**
	 * "Constructor" para el Singleton
	 * @return Instancia de la clase
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static synchronized Baseconn getInstance() throws ClassNotFoundException, SQLException 
	{
		if ( ref == null )
			ref = new Baseconn();
		return ref;
	}
	
	/**
	 *  Esta clase es singleton y no se puede clonar. 
	 */
	  public Object clone()	throws CloneNotSupportedException 
	  {
	    throw new CloneNotSupportedException(); 
	  }
	  
	  public ResultSet doQuery(String query) throws SQLException
	  {
		  Statement s;
		  ResultSet r;
		  s = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		  r = s.executeQuery(query);
		  
		  return r;
	  }
}