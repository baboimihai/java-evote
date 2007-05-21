package votante;


import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

import criptografia.*;

import eleccion.*;



/**
 * 
 * @author pagarcia
 * @version 0.1a
 * La clase Votante representa una persona que se conecta al sistema para depositar votos.
 *
 */
public class Votante
{
	// Propiedades de este votante
	private String dni; // Documento Nacional de Identidad
	private String rvi; // Clave privada
	private String uvi; // Clave pública
	
	// Propiedades de la votación en curso para este votante
	private String idv; // ID de la votación
	private String svu; // Secreto compartido entre el votante y la urna
	private String svm; // Secreto compartido entre el votante y la mesa
	private Hashtable<String, String> opcBoletas; // Opciones con sus boletas asociadas
	private String sobreHasheado; // El sobre que envia a la urna, hasheado
	
	// Variables de conexión hacia la mesa
	private Socket mesa;
	private ObjectInputStream mesaIn;
	private ObjectOutputStream mesaOut;
	
	// Variables de conexión hacia la urna
	private Socket urna;
	private ObjectInputStream urnaIn;
	private ObjectOutputStream urnaOut;
	
	/**
	 * Crea un votante asociado a un DNI con una clave privada.
	 * @param dni Documento Nacional de Identidad del votante a crear.
	 * @param contrasenia Contraseña del votante. En particular, su clave privada.
	 * @throws Exception Si hubo un problema en la creación.
	 */
	public Votante(String dni, String contrasenia) throws Exception, IOException, VotanteInvalidoException
	{
		try
		{
			// Obtengo la clave publica del votante
			this.uvi = Padron.getInstance().getUvi(dni);
		}
		catch (Exception e)
		{
			// No se encontro el DNI en el padron
			throw new VotanteInvalidoException("DNI o contraseña inválidos.");
		}
		
		// Chequeo la contraseña que me dio
		if (!esValidaContrasenia(dni, contrasenia))
			throw new VotanteInvalidoException("DNI o contraseña inválidos.");
		
		// Guardo propiedades del votante
		this.dni = dni;
		this.rvi = contrasenia;
		
		// Creo una conexión a la mesa
		mesa = new Socket(InfoServidores.hostMesa, InfoServidores.puertoMesaDesdeVotante);
		mesaIn = new ObjectInputStream(mesa.getInputStream());
		mesaOut = new ObjectOutputStream(mesa.getOutputStream());
		
		// Creo una conexión a la urna
		urna = new Socket(InfoServidores.hostUrna, InfoServidores.puertoUrnaDesdeVotante);
		urnaIn = new ObjectInputStream(urna.getInputStream());
		urnaOut = new ObjectOutputStream(urna.getOutputStream());
	}
	
	/**
	 * Chequea si una contrasenia es valida para un DNI dado.
	 * @param dni El DNI del votante.
	 * @param contrasenia La contrasena ingresada, que debiera ser su clave privada.
	 * @return Indica si es valida o no.
	 * @throws Exception Si hubo un problema en la encriptacion o desencriptacion.
	 */
	private boolean esValidaContrasenia(String dni, String contrasenia) throws Exception
	{
		// Creo el encriptador y desencriptador para chequear la validez
		Encriptador encriptador = new Encriptador(uvi);
		Desencriptador desencriptador = new Desencriptador(contrasenia);
		
		// Encripto su DNI con su clave publica
		// Si logro obtenerlo nuevamente desencriptandolo con su contrasenia,
		// es porque era su clave privada
		if (desencriptador.desencriptarString(encriptador.encriptar(dni)).equals(dni))
			return true;
		else
			return false;
	}

	/**
	 * Pide a la mesa el estado de las votaciones en que puede participar.
	 * @param dni El DNI del votante.
	 * @return Una tabla donde se encuentran los pares "nombre de la votación" y "votó / no votó".
	 */
	public Hashtable getEstadoVotaciones(String dni) throws Exception, IOException
	{
		Hashtable estadoVot;

		// Envío a la mesa mi dni para saber mi estado de votaciones
		mesaOut.writeObject(dni);
		
		// Recibo el estado de votaciones
		estadoVot = (Hashtable) mesaIn.readObject();
		
		// Chequeo que las votaciones en las que la mesa asegura que puedo votar
		// sean realmente las de público conocimiento
		List<String> votacionesLocal = Padron.getInstance().getVotaciones(dni);
		if (votacionesLocal.size() != estadoVot.size() ||
				!votacionesLocal.containsAll(estadoVot.keySet()))
			throw new FraudeException("Las votaciones enviadas por la mesa no son exactamente las que le corresponden a este votante.");
				
		return estadoVot;
	}
	
	/**
	 * Envía el Paso 1 del protocolo [Identificación para una votación].
	 * @throws Exception Si hubo algún problema en la encriptación o envío.
	 */
	public void envPaso1(String idv) throws Exception, IOException
	{
		String msg; // Mensaje
		// Términos del mensaje:
		String t1, t2, t3, t4; // Nivel 0
		String t4_1, t4_2, t4_3; // Nivel 1
		String t4_3_1, t4_3_2; // Nivel 2
		
		// Genero SVU y SVM
		SecureRandom random = new SecureRandom();
		byte bytes[] = random.generateSeed(12);
		random.nextBytes(bytes);
		this.svu = new String(bytes);
		random.nextBytes(bytes);
		this.svm = new String(bytes);
		
		// Inicializo encriptadores y firmador
		Encriptador eMesa = new Encriptador(InfoServidores.publicaMesa);
		Encriptador eUrna = new Encriptador(InfoServidores.publicaUrna);
		Firmador firmador = new Firmador(rvi);
		
		// Obtengo los términos del mensaje
		// Término 1
		t1 = eUrna.encriptar(svu);
		
		// Término 2
		t2 = svm;
		
		// Término 3
		t3 = uvi;
		
		//Término 4
		t4_1 = dni;
		t4_2 = idv;
		t4_3_1 = eUrna.encriptar(svu);
		t4_3_2 = svm;
		t4_3 = Hasheador.hashear(Arrays.asList(t4_3_1, t4_3_2));
		t4 = firmador.firmar(Arrays.asList(t4_1, t4_2, t4_3));
		
		// Obtengo el mensaje final
		msg = eMesa.encriptar(Arrays.asList(t1, t2, t3, t4));
		
		// Lo envío a la mesa
		mesaOut.writeObject(msg);
				
		// Defino esta votación como la actual
		this.idv = idv;
	}
	
	/**
	 * Envía el Paso 4 del protocolo [Mete boleta en Urna].
	 * @param opcion
	 * @throws Exception Si hubo algún problema en la encriptación o envío.
	 */
	public void envPaso4(String opcion) throws Exception, IOException
	{
		String msg; // Mensaje
		// Términos del mensaje
		String t1, t2; // Nivel 0
		String t2_1, t2_2; // Nivel 1
		
		// Genero randB
		SecureRandom random = new SecureRandom();
		byte bytes[] = random.generateSeed(12);
		random.nextBytes(bytes);
		String randB = new String(bytes);
		
		// Obtengo las claves públicas de las opciones para esta votación
		Enumeration<String> uOpc = Padron.getInstance().getUOpc(idv).elements();

		// Inicializo encriptadores
		Encriptador eUrna = new Encriptador(InfoServidores.publicaUrna);
		Encriptador eOpciones = new Encriptador();
		
		
		// Obtengo los términos del mensaje
		// Término 1
		t1 = svu;
		
		//Término 2
		t2_1 = opcBoletas.get(opcion);
		t2_2 = randB;
		// Encripto con las claves públicas de las opciones de esta votación
		// Con la primera clave
		t2 = eOpciones.encriptar(Arrays.asList(t2_1, t2_2), uOpc.nextElement());
		// Con el resto
		while (uOpc.hasMoreElements())
				t2 = eOpciones.encriptar(t2, uOpc.nextElement());
		
		// Guardo el hash del sobre para chequear luego contra el ticket
		this.sobreHasheado = Hasheador.hashear(t2);
		
		// Obtengo el mensaje final
		msg = eUrna.encriptar(Arrays.asList(t1, t2));

		// Lo envío a la urna
		urnaOut.writeObject(msg);
	}
	
	/**
	 * Recibe el Paso 3 del protocolo [Recibe boletas con opciones posibles].
	 * @return Una enumeración de las opciones posibles para la votación elegida.
	 * @throws Exception Si falla la desencriptación o las boletas no son válidas.
	 */
	public List<String> recPaso3() throws Exception, IOException, FraudeException
	{
		List<String> boletasFirmadas;
		List<String> opcionesBoletasLocal;
		
		// Recibo el mensaje con las boletas
		String msg = (String) mesaIn.readObject();
		
		// Inicializo desencriptadores y validador
		Desencriptador desencriptador = new Desencriptador(rvi);
		Validador validador = new Validador(InfoServidores.publicaMesa);
		
		// Lo desencripto
		boletasFirmadas = desencriptador.desencriptar(msg);
		
		// Valido la firma en cada una y luego de chequear el ID de la votación,
		// guardo un hash indexado por las opciones junto a sus respectivas boletas
		
		this.opcBoletas = new Hashtable<String, String>();
		
		List<String> boleta;
		String opcBoleta;
		String idvBoleta;
		
		for (String boletaStr : boletasFirmadas)
		{
			try
			{
				// Valido la boleta
				boleta = validador.validar(boletaStr);
			}
			catch (Exception e)
			{
				throw new FraudeException("La mesa no firmó correctamente una o más boletas.");
			}
			opcBoleta = boleta.get(0);
			idvBoleta = boleta.get(1);
			// Chequeo que corresponda a la votación que pedí
			if (!idvBoleta.equals(idv))
				throw new FraudeException("Una o más boletas enviadas por la mesa no corresponden a la votación requerida por el votante.");
			// Almaceno las opciones con su boleta correspondiente
			opcBoletas.put(opcBoleta, boletaStr);
		}
		
		// Chequeo que la lista de opciones que me envió la mesa sean
		// las de conocimiento público para esta votación
		opcionesBoletasLocal = Padron.getInstance().getOpciones(idv);
		
		
		if (opcionesBoletasLocal.size() != opcBoletas.size() ||
				!opcionesBoletasLocal.containsAll(opcBoletas.keySet()))
			throw new FraudeException("Las boletas enviadas por la mesa no son exactamente las que le corresponden a este votante.");
		
		// Devuelvo las IDs de votación
		return opcionesBoletasLocal;
	}
	
	/**
	 * Recibe el Paso 7 del protocolo [Recibe el ticket de la votación].
	 * @return El ticket de votación.
	 * @throws Exception
	 */
	public String recPaso7() throws Exception, IOException, FraudeException
	{
		// Recibo el ticket
		String ticket = (String) urnaIn.readObject();
		
		// Inicializo validador
		Validador validador = new Validador(InfoServidores.publicaUrna);
		
		// Chequeo firma de la urna
		String ticketSinFirmar;
		try
		{
			ticketSinFirmar = validador.validarString(ticket);			
		}
		catch (Exception e)
		{
			throw new FraudeException("La urna no firmó correctamente el ticket.");
		}
		
		if (!Hasheador.hashear(ticketSinFirmar).equals(sobreHasheado))
			throw new FraudeException("La urna hasheó un sobre distinto al que se le entregó.");
		
		return ticket;
	}
	
}
