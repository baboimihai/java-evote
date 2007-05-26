package mesa;
/**
 * 
 * @author hrajchert
 * @version 0.2a
 * La clase MesaApp es la encargada de ofrecer los servicios de la clase Mesa
 *
 */

import java.io.IOException;
import java.net.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import org.apache.log4j.*;

import eleccion.InfoServidores;
import eleccion.Padron;

public class MesaApp {

	public static void main (String args[]) {
		// Accedo al log de la mesa
		Logger logger=Logger.getLogger("evote.mesa");
		PropertyConfigurator.configure(InfoServidores.log4jconf);
		
		// Inicializo el infoservidor
		try {
			InfoServidores.inicializarClaves();
		} catch (IOException e) {
			logger.fatal("Hubo un problema al inicializar las claves privadas: " + e.getMessage());
			System.exit(1);
		}

		try {
			Padron.getInstance().cargarPadron(InfoServidores.archVotantes, InfoServidores.archVotaciones);
		} catch (IOException e) {
			logger.fatal("Hubo un problema incializando al padron: " + e.getMessage());
			System.exit(1);
		}

		
		Selector selector = null;
		ServerSocketChannel canalVotacion = null;
		ServerSocketChannel canalVotoConsulta = null;
		try {
			// Creo el selector de canales (sirve para hacer un select entre varias conexiones).		
			selector = Selector.open();
			
			// Creo el ServerSocket encargado de escuchar los pedidos de una votación
			canalVotacion = ServerSocketChannel.open();
			canalVotacion.configureBlocking(false);
			canalVotacion.socket().bind(new InetSocketAddress(InfoServidores.puertoMesaDesdeVotante));
			
			// Creo el ServerSocket encargado de escuchar los pedidos de ver que voto.
			canalVotoConsulta = ServerSocketChannel.open();
			canalVotoConsulta.configureBlocking(false);
			canalVotoConsulta.socket().bind(new InetSocketAddress(InfoServidores.puertoMesaEV));
			
			// Registro los Canales en el selector
			canalVotacion.register(selector, SelectionKey.OP_ACCEPT);
			canalVotoConsulta.register(selector, SelectionKey.OP_ACCEPT);
		}
		catch (IOException e)
		{
			logger.fatal("Hubo un problema al inicializar la conexion: " + e.getMessage());
			System.exit(1);
		}
		// Me quedo esperando por nuevos clientes, cuando los encuentro
		// creo un thread.
		logger.info("Mesa lista para aceptar conexiones");
		while (true)
		{
			// Espero hasta que halla un pedido de conexión en alguno de los puertos.
			try {
				selector.select();
			}
			catch (IOException e)
			{
				logger.fatal("Hubo un problema con la seleccion de conexiones: " + e.getMessage());
				System.exit(1);
			}
			
			logger.debug("Recibo una conexion");
			// Itero por los canales aceptados
			for (SelectionKey aSel : selector.selectedKeys()) {
				if (aSel.isAcceptable())
				{
					// Saco al canal del set para que no sea procesado después
					selector.selectedKeys().remove(aSel);
					
					// Obtengo el ServerSocketChannel asociado a la seleccion
					ServerSocketChannel aChannel = (ServerSocketChannel) aSel.channel();
					
					if ( aChannel.equals(canalVotacion) )
					{
						try {
							logger.debug("Creo un thread de mesahandler");
							Socket s = aChannel.accept().socket();
							(new MesaHandler(s)).start();
						} catch (Exception e) {
							logger.error("Hubo un problema creando el thread de la mesa: " + e.getMessage());
						}
					}
					else{
						try {
							logger.debug("Creo un thread de EstadoVotacion");
							Socket s = aChannel.accept().socket();
							(new EstadoVotacion(s)).start();
						} catch (Exception e) {
							logger.error("Hubo un problema creando el thread de EstadoVotacion: " + e.getMessage());
						}
						 
					}
				}
			}
		}
	}
}
