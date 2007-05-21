package eleccion;
/**
 * 
 * @author ezequiel85
 * @version 1.0
 * La clase Padron hace que Ingui se la coma mas rapido
 *
 */
import java.io.*;
import java.util.*;

public class Padron
{	
	private BufferedReader votantes, votaciones;
	private String line;
	private String group = null;
	private Hashtable<String, Votante> votante_list;
	private Hashtable<String, Votacion> votacion_list;
	private static Padron ref;
	
	/**
	 * Constructor privado del padron para evitar que lo creen.
	 */
	private Padron()
	{
		votante_list = new Hashtable<String, Votante>();
		votacion_list = new Hashtable<String, Votacion>();
	}
			
	/**
	 * "Constructor" para el Singleton
	 * @return Instancia de la clase
	 */
	public static synchronized Padron getInstance() 
	{
		if ( ref == null )
			ref = new Padron();
		return ref;
	}
	
	/** Metodo privado para cargar votantes (solo para ordenar un poco cargarPadron)
	 * @param void
	 * @throws IOException
	 */
	private void cargarVotaciones() throws IOException
	{
		String nombre = null;
		while ((line = this.votaciones.readLine()) != null)
		{
			if (line.charAt(0) == '0')
			{
				nombre = (line.split(","))[1];
				votacion_list.put(nombre, new Votacion(nombre));
				continue;
			}
			String[] splitted;
			Votacion v;
			splitted = line.split(",");
			
			if (line.charAt(0) == '1') //Nueva lista
			{
				v = votacion_list.get(nombre);
				v.addOpcion(splitted[1], splitted[2]);
				votacion_list.put(nombre, v);
			}
			else // Nuevo grupo que participa en la votacion
			{
				v = votacion_list.get(nombre);
				v.addGrupo(splitted[1]);
				votacion_list.put(nombre, v);
			}
		}		
	}
	
	/** Metodo privado para cargar votantes (solo para ordenar un poco cargarPadron)
	 * @param void
	 * @throws IOException
	 */
	private void cargarVotantes() throws IOException
	{
		while ((line = this.votantes.readLine()) != null)
		{
			if (line.charAt(0) == '0')
			{
				group = (line.split(","))[1];
				continue;
			}
			String[] splitted;
			splitted = line.split(",");
			
			if (votante_list.containsKey(splitted[2]))
			//Si el hash ya contiene a este DNI, es porque esta en mas de un grupo
			{
				Votante v = votante_list.get(splitted[2]);
				v.addGrupo(group);
				votante_list.put(splitted[2], v);
			}
			else // Si no, insertamos un nuevo votante
			{
				votante_list.put(splitted[2], new Votante(splitted[2],splitted[1], splitted[3], group));
			}
		}		
	}

	/**
	 * Carga en memoria el padron
	 * @param votantes Path al archivo de votantes
	 * @param votaciones Path al archivo de votaciones
	 * @throws IOException
	 */
	public void cargarPadron(String votantes, String votaciones) throws IOException
	{
		this.votantes = new BufferedReader(new FileReader(votantes));
		this.votaciones = new BufferedReader(new FileReader(votaciones));

		this.cargarVotantes();
		this.cargarVotaciones();
	}

	/**
	 *  Esta clase es singleton y no se puede clonar. 
	 */
	  public Object clone()	throws CloneNotSupportedException 
	  {
	    throw new CloneNotSupportedException(); 
	  }
	
	/**
	 * Dado un DNI y un ID de votación consulta si el votante puede votar en dicha eleccion
	 * @param dni String con el DNI
	 * @param idv ID de la votacion
	 * @throws Exception Si no se encontro el par (dni, idv)
	 */
	public boolean puedeVotar(String dni, String idv) throws Exception
	{
		Votante v;
		Votacion vo;
		Iterator i;
		String grupo;
		
		v = votante_list.get(dni);
		if (v == null)
			throw new Exception("El DNI no corresponde a ningun votante");
		
		vo = votacion_list.get(idv);
		if (vo == null)
			throw new Exception("La votacion solicitada no existe");
		
		i = vo.grupos.iterator();
		while (i.hasNext())
		{
			grupo = (String)i.next();
			if (v.grupo.contains(grupo))
				return true;
		}
		return false;
	}
	
	/**
	 * Dado un DNI devuelve una lista con las votaciones en que puede participar el votante
	 * @param dni String con el DNI
	 * @throws Exception Si no se encontro el dni
	 */
	public List<String> getVotaciones(String dni) throws Exception
	{
		Votacion vo;
		Votante v;
		Iterator i;
		List<String> a;
		Enumeration<Votacion> e;
		
		a = new ArrayList<String>();
		
		v = votante_list.get(dni);
		if (v == null)
			throw new Exception("El DNI no corresponde a ningun votante");
		
		i = v.grupo.iterator();
		
		while(i.hasNext())
		{
			String rec;
			
			e = votacion_list.elements();
			rec = (String) i.next();
			while(e.hasMoreElements())
			{	
				vo = e.nextElement();
				for(String grupoparticipante:vo.grupos)
				{
					if (grupoparticipante.equals(rec) && !a.contains(vo.nombre))
					{
						a.add(vo.nombre);
					}
				}		
			}
		}
		return a;
	}
	
	/**
	 * Devuelve las opciones para un idv
	 * @param idv El id de la votacion
	 * @return La lista de Strings con las opciones
	 * @throws Exception Si no existia la votacion
	*/
	public List<String> getOpciones(String idv) throws Exception
	{
		Votacion vo;
		List<String> l;
		Enumeration<String> e;
		
		l = new ArrayList<String>();
		vo = votacion_list.get(idv);
		if (vo == null)
			throw new Exception("La votacion solicitada no existe");
		e = getUOpc(vo.nombre).keys();
		while(e.hasMoreElements())
		{
			l.add(e.nextElement());
		}
		
		return l;
	}
	
	/**
	 * Dada una clave publica devuelve el DNI correspondiente
	 * @param uvi String con la clave publica
	 * @throws Exception Si no se encontro el uvi
	 * 
	 */
	public String getDNI(String uvi) throws Exception
	{
		Votante v;
		Enumeration<Votante> e;
		
		e = votante_list.elements();
		while(e.hasMoreElements())
		{
			v = e.nextElement();
			if (v.getUvi().equals(uvi))
				return v.getDNI();
		}
		throw new Exception("El votante indicado no existe");
		//Si llego aca es que la uvi indicada no existe
	}

	/**
	 * Dado un DNI devuelve la clave publica asociada
	 * @param dni String con el DNI
	 * @throws Exception Si no se encontro el dni
	 */
	public String getUvi(String dni) throws Exception
	{
		Votante v;
		
		v = votante_list.get(dni);
		if (v == null)
			throw new Exception("El votante indicado no existe");
		
		return v.getUvi();
	}
	
	/**
	 * Dado un ID de votacion retorna un hash con los pares (opcion, clave pública)
	 * @param idv
	 * @throws Exception Si no se encontro la votacion
	 */
	public Hashtable<String,String> getUOpc(String idv) throws Exception
	{
		Votacion vo;
		
		vo = votacion_list.get(idv);
		if (vo == null)
			throw new Exception("La votacion solicitada no existe");
		return vo.getListas();
	}
	
	private class Votacion
	{
		private String nombre;
		private Hashtable<String,String> listas;
		private ArrayList<String> grupos;
		
		public Votacion(String nombre)
		{
			this.nombre = nombre;
			this.listas = new Hashtable<String,String>();
			this.grupos = new ArrayList<String>();
		}
		
		public void addOpcion(String candidato, String uvi)
		{
			this.listas.put(candidato, uvi);
		}
		
		public void addGrupo(String grupo)
		{
			this.grupos.add(grupo);
		}
		
		public Hashtable<String, String> getListas()
		{
			return this.listas;
		}
	}
	
	private class Votante
	{
		@SuppressWarnings("unused")
		private String dni;
		@SuppressWarnings("unused")
		private String nombre;
		@SuppressWarnings("unused")
		private String uvi;
		private ArrayList<String> grupo;
		
		public Votante(String dni, String nombre, String uvi, String grupo)
		{
			this.grupo = new ArrayList<String>();
			this.dni = dni;
			this.nombre = nombre;
			this.uvi = uvi;
			this.grupo.add(grupo);
		}
		
		public void addGrupo(String grupo)
		{
			this.grupo.add(grupo);
		}
		
		public String getDNI()
		{
			return this.dni;
		}
		
		public String getUvi()
		{
			return this.uvi;
		}
	}
}