package mesa;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.*;

import eleccion.ComprobanteNotFoundException;
import eleccion.ComprobantesMesa;
import eleccion.InfoServidores;
import eleccion.Padron;

public class EstadoVotacion extends Thread {
	// Variables de conexión hacia el votante
	private Socket votante;
	private ObjectInputStream votanteIn;
	private ObjectOutputStream votanteOut;
	
	// Clase de log
	private Logger logger;
	
	public EstadoVotacion(Socket aVotante) throws IOException {
		// Accedo al log de  mesa estado
		logger=Logger.getLogger("evote.mesa.estado");
		PropertyConfigurator.configure(InfoServidores.log4jconf);
		
		votante = aVotante;
		votanteOut = new ObjectOutputStream(votante.getOutputStream());
		votanteIn = new ObjectInputStream(votante.getInputStream());
}
/**
 * El run sirve para correr el thread.
 */
public void run() {
		//
		// TODO: Responder al votante los errores etc.
		//
		List<List<Object>> rta = new Vector<List<Object>>();
		try {
			// Leo el dni desde el votante
			String dni = (String) votanteIn.readObject();
			
			logger.info("Me llego el dni = " + dni);
			// Cargo la lista de votaciones en las que está habilitado el votante.
			List<String> votacionesPosibles = Padron.getInstance().getVotaciones(dni);
			String uvi = Padron.getInstance().getUvi(dni);
			// Itero por los idv, me fijo si no está, si esta lo saco
			for (String idv : votacionesPosibles) {
				boolean yaVoto;
				try {
					ComprobantesMesa.getInstance().obtenerComprobante(uvi, idv);
					// Si esta 
					yaVoto = true;
					
				} catch (ComprobanteNotFoundException e){
					// Si no esta
					yaVoto = false;
				}
				// Agrego la tupla
				List<Object> aux = new Vector<Object>();
				aux.add((Object)idv);
				aux.add(yaVoto);
				rta.add(aux);
			}
			logger.info("Devuelvo rta = " + rta);
			votanteOut.writeObject(rta);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Hubo un error atendiendo el pedido" ,e);
		}
		
	}
}