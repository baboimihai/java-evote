/**
 * La clase Comprobantes contiene todos los comprobantes de la mesa, utiliza una base de datos
 * 
 */

// TODO Spock, tambien tenes que implementar la interfaz "Iterable".
// La interfaz Iterator o la implementas aca o haces otra clase que la implemente...
// En otras palabras voy a tener que poder iterar.
import java.util.*;
public class Comprobantes {
	
	/**
	 * Inserto un comprobante en la base
	 * @param usvu Secreto compartido entre el votante y la urna encriptado con Uu
	 * @param uvi Clave publica del iesimo votante
	 * @param tokenFirmado Es el comprobante en si.
	 */
	public boolean insertarComprobante(String usvu, String uvi, String tokenFirmado) throws Exception {
		return false;
	}
	/**
	 * Devuelve el comprobantes como listas.
	 * Tira Excepcion si no está.
	 * @param uvi Clave publica del iesimo votante
	 * @return la lista formada por (usvu, tokenFirmado).
	 */
	public List obtenerComprobante(String uvi, String idv) throws Exception {
		//TODO: Internamente tenes que buscar en la base todos los token que tengan 
		// el uvi, desencriptarlos (con el uvi) y fijarte si el id es el que te mando
		// yo.
		return null;
	}
	/**
	 * Marca a un votante como que ya votó
	 * @param usvu Secreto compartido entre el votante y la urna encriptado con Uu
	 */
	public void marcarVotado(String usvu) throws Exception {}
	
	/**
	 * Devuelve si está marcado como que votó o no.
	 */
	public boolean yaVoto(String usvu) throws Exception {
		return true;
	}

}
