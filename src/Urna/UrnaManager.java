package Urna;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.net.*;

/**
 * @author hrajchert
 * @version 0.1a
 * Esta es la clase UrnaManager, se encarga de proveer a los threads UrnaHandler
 * un socket que recibe la clase UrnaApp.
 */
public class UrnaManager {
	private Map<String, List> socketMap;
    
	// Lock del mapa y variable de condicion
	final Lock MapLock;
	final Condition MapCond;
	
	/**
	 * Constructor privado de la clase para evitar que lo creen.
	 */
	private UrnaManager() {
		socketMap = new HashMap<String, List>();
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
	
	  public void setSocket(String svu,  List l)
	  {
		  MapLock.lock();
		  try {
			  socketMap.put(svu, l);
			  MapCond.signalAll();
		  }
		  finally {
			  MapLock.unlock();
		  }
	  }
	  
	  public List getSocket(String svu) throws InterruptedException {
		  
		  List rta = null;
		  MapLock.lock();
		  Date now = new Date();
		  
		  try {
			  do {
				  MapCond.awaitUntil(new Date(now.getTime() + 31000 ));
			  } while (socketMap.containsKey(svu));
			  rta = socketMap.get(svu);
		  }
		  
		  finally {
			  MapLock.unlock();
		  }
		  return rta; 
	  }
	  
	  
}
