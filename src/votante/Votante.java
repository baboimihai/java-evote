package votante;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import criptografia.Desencriptador;
import criptografia.Encriptador;
import criptografia.Firmador;
import criptografia.Hasheador;
import criptografia.Validador;
import eleccion.FraudeException;
import eleccion.InfoServidores;
import eleccion.Padron;


/**
 * La clase Votante representa una persona que se conecta al sistema para depositar votos.
 * @author pabloggarcia
 * @version 0.1a
 */
public class Votante
{

	// Propiedades de este votante
	private String dni; // Documento Nacional de Identidad
	private String rvi; // Clave privada
	private String uvi; // Clave p�blica
	private List<List<Object>> estadoVotaciones; // Estado de sus votaciones

	// Propiedades de la votaci�n en curso para este votante
	private String idv; // ID de la votaci�n
	private String svu; // Secreto compartido entre el votante y la urna
	private String svm; // Secreto compartido entre el votante y la mesa
	private Hashtable<String, String> opcBoletas; // Opciones con sus boletas asociadas
	private String sobreHasheado; // El sobre que envia a la urna, hasheado

	// Variables de conexi�n hacia la mesa
	private Socket mesa;
	private ObjectInputStream mesaIn;
	private ObjectOutputStream mesaOut;
	private ObjectInputStream mesaInEV;
	private ObjectOutputStream mesaOutEV;

	// Variables de conexi�n hacia la urna
	private Socket urna;
	private ObjectInputStream urnaIn;
	private ObjectOutputStream urnaOut;
	private Logger logger;

	/**
	 * Crea un votante asociado a un DNI con una clave privada.
	 * @param dni Documento Nacional de Identidad del votante a crear.
	 * @param clavePriv Clave privada del votante.
	 * @throws Exception Si hubo un problema en la creaci�n.
	 */
	public Votante(String dni, String clavePriv) throws Exception, IOException, VotanteInvalidoException
	{
		logger=Logger.getLogger("evote.votante");
		PropertyConfigurator.configure(InfoServidores.log4jconf);
		try
		{
			// Obtengo la clave p�blica del votante
			this.uvi = Padron.getInstance().getUvi(dni);
		}
		catch (Exception e)
		{
			// No se encontr� el DNI en el padr�n
			throw new VotanteInvalidoException("DNI o contrase�a inv�lidos.");
		}

		// Chequeo la contrase�a que me dio
		if (!esValidaContrasenia(dni, clavePriv))
			throw new VotanteInvalidoException("DNI o contrase�a inv�lidos.");

		// Guardo propiedades del votante
		this.dni = dni;
		this.rvi = clavePriv;
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
	 * @return Una tabla donde se encuentran los pares "nombre de la votaci�n" y "vot� / no vot�".
	 */
	public List<List<Object>> getEstadoVotaciones() throws Exception, IOException
	{
		// Si no lo hab�a hecho antes, creo la conexi�n al puerto de preguntas de estado de votaciones
		// de la mesa
		if (mesa == null)
			mesa = new Socket(InfoServidores.hostMesa, InfoServidores.puertoMesaEV);

		mesaOutEV = new ObjectOutputStream(mesa.getOutputStream());
		mesaInEV = new ObjectInputStream(mesa.getInputStream());

		List<List<Object>> estadoVotacionesActual;

		// Env�o a la mesa mi dni para saber mi estado de votaciones
		mesaOutEV.writeObject(dni);

		// Recibo el estado de votaciones
		estadoVotacionesActual = (List<List<Object>>) mesaInEV.readObject();

		// Chequeo que las votaciones en las que la mesa asegura que puedo votar
		// sean realmente las de p�blico conocimiento
		List<String> votacionesLocal = Padron.getInstance().getVotaciones(dni);

		// Chequeo cantidad
		if (votacionesLocal.size() != estadoVotacionesActual.size())
			throw new FraudeException("Las cantidad de votaciones disponibles enviadas por la mesa no igual a las que corresponden p�blicamente a este votante.");

		// Chequeo calidad
		for (List<Object> estado : estadoVotacionesActual)
		{
			if (!votacionesLocal.contains(estado.get(0)))
				throw new FraudeException("Las votaciones enviadas por la mesa no son exactamente las que le corresponden a este votante.");
		}

		this.estadoVotaciones = estadoVotacionesActual;

		// Cierro los streams
		mesaOutEV.close();
		mesaInEV.close();

		return this.estadoVotaciones;
	}


	/**
	 * Chequea si tiene votaciones en las que todav�a no particip�.
	 * @param estadoVotaciones
	 */
	public boolean tieneVotacionesPendientes(List<List<Object>> estadoVotaciones) throws Exception, IOException
	{
		if (this.estadoVotaciones == null)
			getEstadoVotaciones();

		for (List<Object> estado : this.estadoVotaciones)
			if ((Boolean)estado.get(1) == false)
				return true;

		return false;
	}


	/**
	 * Env�a el Paso 1 del protocolo a la mesa [Identificaci�n para una votaci�n].
	 * @throws Exception Si hubo alg�n problema en la encriptaci�n o env�o.
	 */
	public void envPaso1(String idv) throws Exception, IOException
	{
		// Creo una conexi�n a la mesa para los pasos de votaci�n
		mesa = new Socket(InfoServidores.hostMesa, InfoServidores.puertoMesaDesdeVotante);

		// Creo stream de salida con la mesa
		mesaOut = new ObjectOutputStream(mesa.getOutputStream());

		String msg; // Mensaje
		// T�rminos del mensaje:
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

		// Obtengo los t�rminos del mensaje
		// T�rmino 1
		t1 = eUrna.encriptar(svu);

		// T�rmino 2
		t2 = svm;

		// T�rmino 3
		t3 = uvi;

		//T�rmino 4
		t4_1 = dni;
		t4_2 = idv;
		t4_3_1 = t1;
		t4_3_2 = svm;
		t4_3 = Hasheador.hashear(Arrays.asList(t4_3_1, t4_3_2));
		t4 = firmador.firmar(Arrays.asList(t4_1, t4_2, t4_3));

		// Obtengo el mensaje final
		msg = eMesa.encriptar(Arrays.asList(t1, t2, t3, t4));

		// Lo env�o a la mesa
		mesaOut.writeObject(msg);

		// Defino esta votaci�n como la actual
		this.idv = idv;
	}


	/**
	 * Env�a el Paso 4 del protocolo a la urna [Mete boleta en Urna].
	 * @param opcion
	 * @throws Exception Si hubo alg�n problema en la encriptaci�n o env�o.
	 */
	public void envPaso4(String opcion) throws Exception, IOException
	{
		// Creo una conexi�n a la urna
		urna = new Socket(InfoServidores.hostUrna, InfoServidores.puertoUrnaDesdeVotante);

		// Creo stream de salida con la urna
		urnaOut = new ObjectOutputStream(urna.getOutputStream());

		String msg; // Mensaje
		// T�rminos del mensaje
		String t1, t2; // Nivel 0
		String t2_1, t2_2; // Nivel 1

		// Genero randB
		SecureRandom random = new SecureRandom();
		byte bytes[] = random.generateSeed(12);
		random.nextBytes(bytes);
		String randB = new String(bytes);

		// Obtengo las claves p�blicas de las opciones para esta votaci�n
		Enumeration<String> uOpc = Padron.getInstance().getUOpc(idv).elements();

		// Inicializo encriptadores
		Encriptador eUrna = new Encriptador(InfoServidores.publicaUrna);
		Encriptador eOpciones = new Encriptador();


		// Obtengo los t�rminos del mensaje
		// T�rmino 1
		t1 = svu;

		//T�rmino 2
		t2_1 = opcBoletas.get(opcion);
		t2_2 = randB;
logger.debug("sobreIN: opci�n: " + t2_1 + " random: " + t2_2);
		// Encripto con las claves p�blicas de las opciones de esta votaci�n
		// Con la primera clave
String aux = uOpc.nextElement();
logger.debug("encripto con la clave: " + aux);
		t2 = eOpciones.encriptar(Arrays.asList(t2_1, t2_2), aux);
		// Con el resto
		while (uOpc.hasMoreElements()) {
aux = uOpc.nextElement();
logger.debug("encripto con la clave: " + aux);
			t2 = eOpciones.encriptar(t2, aux);
		}
logger.debug("sobreOUT: " + t2);
		// Guardo el hash del sobre para chequear luego contra el ticket
		this.sobreHasheado = Hasheador.hashear(t2);

		// Obtengo el mensaje final
		msg = eUrna.encriptar(Arrays.asList(t1, t2));

		// Lo env�o a la urna
		urnaOut.writeObject(msg);
	}


	/**
	 * Recibe el Paso 3 del protocolo de la mesa [Recibe boletas con opciones posibles].
	 * @return Una enumeraci�n de las opciones posibles para la votaci�n elegida.
	 * @throws Exception Si falla la desencriptaci�n o las boletas no son v�lidas.
	 */
	public List<String> recPaso3() throws Exception, IOException, FraudeException
	{
		List<String> boletasFirmadas;
		List<String> opcionesBoletasLocal;

		// Creo stream de entrada con la mesa
		mesaIn = new ObjectInputStream(mesa.getInputStream());

		// Recibo el mensaje con las boletas
		String msg = (String) mesaIn.readObject();

		// Inicializo desencriptadores y validador
		Desencriptador desencriptador = new Desencriptador(rvi);
		Validador validador = new Validador(InfoServidores.publicaMesa);

		// Lo desencripto
		boletasFirmadas = desencriptador.desencriptar(msg);

		// Valido la firma en cada una y luego de chequear el ID de la votaci�n,
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
				throw new FraudeException("La mesa no firm� correctamente una o m�s boletas.");
			}
			opcBoleta = boleta.get(0);
			idvBoleta = boleta.get(1);
			// Chequeo que corresponda a la votaci�n que ped�
			if (!idvBoleta.equals(idv))
				throw new FraudeException("Una o m�s boletas enviadas por la mesa no corresponden a la votaci�n requerida por el votante.");
			// Almaceno las opciones con su boleta correspondiente
			opcBoletas.put(opcBoleta, boletaStr);
		}

		// Chequeo que la lista de opciones que me envi� la mesa sean
		// las de conocimiento p�blico para esta votaci�n
		opcionesBoletasLocal = Padron.getInstance().getOpciones(idv);


		if (opcionesBoletasLocal.size() != opcBoletas.size() ||
				!opcionesBoletasLocal.containsAll(opcBoletas.keySet()))
			throw new FraudeException("Las boletas enviadas por la mesa no son exactamente las que le corresponden a este votante.");

		// Cierro los streams con la mesa
		mesaOut.close();
		mesaIn.close();

		// Devuelvo las IDs de votaci�n
		return opcionesBoletasLocal;
	}


	/**
	 * Recibe el Paso 7 del protocolo de la urna [Recibe el ticket de la votaci�n].
	 * @return El ticket de votaci�n.
	 * @throws Exception
	 */
	public String recPaso7() throws Exception, IOException, FraudeException
	{
		// Creo stream de entrada con la urna
		urnaIn = new ObjectInputStream(urna.getInputStream());

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
			throw new FraudeException("La urna no firm� correctamente el ticket.");
		}

		if (!ticketSinFirmar.equals(sobreHasheado))
			throw new FraudeException("La urna hashe� un sobre distinto al que se le entreg�.");

		// Cierro los stream con la urna
		urnaOut.close();
		urnaIn.close();

		return ticket;
	}

}
