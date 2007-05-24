package urna;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import eleccion.ComprobantesUrna;

/**
 * @author hrajchert
 * @version 0.1a
 * Esta es la clase UrnaManager, se encarga de proveer a los threads UrnaHandler
 * un socket que recibe la clase UrnaApp.
 */
public class UrnaManager {
	private Map<String, List> votanteMap;
    
	// Lock del mapa y variable de condicion
	final Lock MapLock;
	final Condition MapCond;
	
	/**
	 * Constructor privado de la clase para evitar que lo creen.
	 */
	private UrnaManager() {
		votanteMap = new HashMap<String, List>();
		MapLock = new ReentrantLock();
		MapCond = MapLock.newCondition();
	}
	
	
	private static UrnaManager ref;
	/**
	 * "Constructor" para el Singleton
	 * @return Instancia de la clase
	 */
	public static synchronized UrnaManager getInstance() 
	{
		if ( ref == null )
			ref = new UrnaManager();
		return ref;
	}
	/**
	 *  Esta clase es singleton y no se puede clonar. 
	 */
	  public Object clone()	throws CloneNotSupportedException 
	  {
	    throw new CloneNotSupportedException(); 
	  }
	
	  /**
	   * Crea un votante y lo pone en el mapa de votantes 
	   * @param svu El secreto compartido entre el votante y la urna.
	   * @param l Lista formada por socket y sobre.
	   * @throws Exception Si el svu es invalido
	   */
	  public void setVotante(String svu,  List l) throws Exception
	  {
		  MapLock.lock();
		  try {
			  // Me fijo que el svu sea valido.
			  if ( !ComprobantesUrna.getInstance().getEstado(svu).equals("no voto")) throw new Exception();
			  // Marco que está en proceso de votación
			  ComprobantesUrna.getInstance().setEstado(svu, new String("en proceso"));
			  
			  votanteMap.put(svu, l);
			  MapCond.signalAll();
		  }
		  catch (Exception e) {
			  // Tiro excepcion si no se encontró el comprobante o si ya votó.
			  throw new Exception("El svu es invalido");
		  }
		  finally {
			  MapLock.unlock();
		  }
	  }
	  
	  public List getVotante(String svu) throws InterruptedException {
		  
		  List rta = null;
		  MapLock.lock();
		  Date now = new Date();
		  
		  try {
			  do {
				  MapCond.awaitUntil(new Date(now.getTime() + 31000 ));
			  } while (votanteMap.containsKey(svu));
			  rta = votanteMap.get(svu);
		  }
		  
		  finally {
			  MapLock.unlock();
		  }
		  return rta; 
	  }
	  
	  
}
