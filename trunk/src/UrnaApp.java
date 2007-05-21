/**
 * 
 * @author hrajchert
 * @version 0.1a
 * La clase UrnaApp es la encargada de ofrecer los servicios de la clase Urna
 *
 */

import java.net.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.io.*;
import java.security.InvalidKeyException;
import java.util.*;

public class UrnaApp {
	public static final int PUERTO1 = 4080;
	public static final int PUERTO2 = 4081;
	public static void main (String args[]) {
		
		// Me creo la instancia de desencriptador encargada de resolver los pedidos del votante.
		
		Desencriptador decrypt = null;
		try {
			decrypt = new Desencriptador("pepe");
		} catch (InvalidKeyException e1) {
			e1.printStackTrace();
			System.exit(1);
		} 
	
		Selector selector = null;
		ServerSocketChannel canalMesa = null;
		ServerSocketChannel canalVotante = null;
		try {
			// Creo el selector de canales (sirve para hacer un select entre varias conexiones).		
			selector = Selector.open();
			
			// Creo el ServerSocket encargado de escuchar los pedidos de la Mesa.
			canalMesa = ServerSocketChannel.open();
			canalMesa.configureBlocking(false);
			canalMesa.socket().bind(new InetSocketAddress(PUERTO1));
			
			// Creo el ServerSocket encargado de escuchar los pedidos del Votante.
			canalVotante = ServerSocketChannel.open();
			canalVotante.configureBlocking(false);
			canalVotante.socket().bind(new InetSocketAddress(PUERTO2));
			
			// Registro los Canales en el selector
			canalMesa.register(selector, SelectionKey.OP_ACCEPT);
			canalVotante.register(selector, SelectionKey.OP_ACCEPT);
		}
		catch (IOException e)
		{
			System.err.println("Hubo un problema al inicializar la conexion: " + e.getMessage());
			System.exit(1);
		}

		// Me quedo esperando por nuevos clientes
		while (true)
		{
			// Espero hasta que halla un pedido de conexión en alguno de los puertos.
			try {
				selector.select();
			}
			catch (IOException e)
			{
				System.err.println("Hubo un problema con la seleccion de conexiones: " + e.getMessage());
				System.exit(1);
			}
			
			// Itero por los canales aceptados
			for (SelectionKey aSel : selector.selectedKeys()) {
				if (aSel.isAcceptable())
				{
					// Saco al canal del set para que no sea procesado después
					selector.selectedKeys().remove(aSel);
					
					// Obtengo el ServerSocketChannel asociado a la seleccion
					ServerSocketChannel aChannel = (ServerSocketChannel) aSel.channel();
					
					if ( aChannel.equals(canalMesa) )
					{
						// Creo un thread para el votante
						try {
							Socket s = aChannel.accept().socket();
							(new UrnaHandler(s)).start();
						} catch (Exception e) {
							System.err.println("Hubo un problema creando el thread de la urna: " + e.getMessage());
						}
					}
					else{
						 // Le aviso al UrnaManager la conexion del votante para que el thread ya creado lo pueda retomar.
						try {
							// Abro el socket para obtener el svu
							Socket s = aChannel.accept().socket();
						    ObjectInputStream votanteIn;
							votanteIn = new ObjectInputStream(s.getInputStream());

							// Obtengo el string encriptado
							String paso2_enc = (String) votanteIn.readObject();

							// Lo desencripto en la lista, obtengo Svu y el sobre
							List<String> paso2 = decrypt.desencriptar(paso2_enc);
							if ( paso2.size() != 2 )
								throw new Exception("Mensaje mal formado");
														
							String svu = paso2.get(0);
							// Le aviso al UrnaManager que hay una nueva conexion con la lista formada
							// por el socket y el sobre (porque ya lo lei)
							UrnaManager.getInstance().setSocket(svu, Arrays.asList(s,paso2.get(1)));
						}
						catch (Exception e)
						{
							System.err.println("Hubo un problema con el mensaje del votante: " + e.getMessage());
						}
					}
				}
			}
		}
	}
}
