package mesa;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

import eleccion.ComprobanteNotFoundException;
import eleccion.ComprobantesMesa;
import eleccion.Padron;

public class EstadoVotacion extends Thread {
	// Variables de conexión hacia el votante
	private Socket votante;
	private ObjectInputStream votanteIn;
	private ObjectOutputStream votanteOut;
	
	public EstadoVotacion(Socket aVotante) throws IOException {
	votante = aVotante;
	votanteIn = new ObjectInputStream(votante.getInputStream());
	votanteOut = new ObjectOutputStream(votante.getOutputStream());
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
			
			// Cargo la lista de votaciones en las que está habilitado el votante.
			List<String> votacionesPosibles = Padron.getInstance().getVotaciones(dni);
			String uvi = Padron.getInstance().getUvi(dni);
			// Itero por los idv, me fijo si no está, si esta lo saco
			for (String idv : votacionesPosibles) {
				boolean puedeVotar;
				try {
					ComprobantesMesa.getInstance().obtenerComprobante(uvi, idv);
					// Si esta 
					puedeVotar = false;
					
				} catch (ComprobanteNotFoundException e){
					// Si no esta
					puedeVotar = true;
				}
				// Agrego la tupla
				List<Object> aux = new Vector<Object>();
				aux.add((Object)idv);
				aux.add(puedeVotar);
				rta.add(aux);
			}
			votanteOut.writeObject(rta);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}