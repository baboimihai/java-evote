package eleccion;
//
// TODO: Falta definir pero tengo noni
//
import java.lang.Iterable;
import java.util.Iterator;

class BoletasIterador implements Iterator<String> {
	public BoletasIterador(String idv) {
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

public class Boletas implements Iterable{
	
	private String idv;
	// El constructor es privado para evitar que lo instancien otras clases
	private Boletas() {
		//TODO Inicializaciones necesarias.
	}

	// Variable que contiene la única instancia de Boletas.
	private static Boletas ref;

	/**
	 *  Esta clase es singleton y no se puede clonar. 
	 */
	  public Object clone()	throws CloneNotSupportedException {
	    throw new CloneNotSupportedException(); 
	  }
	
	/**
	 * Devuelve la instancia a la clase.
	 * @return La instancia de Boletas
	 */
	public static synchronized Boletas getInstance()
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
		
	}
	
	public String getBoleta(String svu) throws Exception
	{
		return null;
	}
	
}
