package eleccion;

/**
 * Esta clase sirve para parsear mi existencia
 */

import java.io.*;

import criptografia.GenKeys;
import eleccion.InfoServidores;

public class GenPadron
{

	private BufferedReader r_votante;
	private BufferedReader r_votacion;
	private BufferedWriter w_votante;
	private BufferedWriter w_votacion;
//	long dni = 30240000;

	/**
	 *
	 * @param args, primer parametro archivo de votantes, segundo de votaciones
	 * NO VALIDO NADA, asi que si esta mal, AJO Y AGUA
	 */
	public static void main(String[] args)
	{
		@SuppressWarnings("unused")
		GenPadron p;

		try
		{
			p = new GenPadron(args[0], args[1]);
			System.out.println("fin generación padrón");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public GenPadron(String votante, String votacion) throws Exception
	{
		this.r_votante = new BufferedReader(new FileReader(votante));
		this.r_votacion = new BufferedReader(new FileReader(votacion));
		this.w_votante = new BufferedWriter(new FileWriter(InfoServidores.resources + "votantes/votante_parsed.txt"));
		this.w_votacion = new BufferedWriter(new FileWriter(InfoServidores.resources + "votacion/votacion_parsed.txt"));

		parse_votante(this.r_votante, this.w_votante);
		parse_votacion(this.r_votacion, this.w_votacion);
	}
	void parse_votante(BufferedReader votante, BufferedWriter out) throws Exception
	{
		String line;
		String res;
		String[] splitted;
//		String dni_string;
		String path = InfoServidores.resources + "votantes/";

		//String val;
		//Hashtable<String, String> tablita;

		//tablita = new Hashtable<String, String>();

		while((line = votante.readLine()) != null)
		{
			splitted = line.split(",");
			if (line.charAt(0) == '1')
			{
				//val = tablita.get(splitted[1]);
				//if (val == null)
				//{
				//	dni_string = (new Long(dni)).toString();
				//	dni++;
				//	tablita.put(splitted[1], dni_string);
				//}
				//else
				//{
				//	dni_string = val;
				//}
				System.out.println("Creando claves para: " + splitted[1]);
				GenKeys.generarClaves(0,path+splitted[1].trim()+"_privada.key", path+splitted[1].trim()+"_publica.key");
				res = splitted[0].trim()+","+splitted[1].trim();
			}
			else
				res = splitted[0].trim()+","+splitted[1].trim();
			out.write(res);
			out.newLine();
		}
		out.close();
	}

	void parse_votacion(BufferedReader votacion, BufferedWriter out) throws Exception
	{
		String line;
		String res;
		String[] splitted;
		String path = InfoServidores.resources + "votacion/";
		//String path = "";
		int vot_num = 0, opc_num = 0;
		String idv = null;


		while((line = votacion.readLine()) != null)
		{
			splitted = line.split(",");
			if (line.charAt(0) == '0')
			{
				vot_num++;
				opc_num = 0;
				idv = splitted[1].trim().replace(' ', '_').replace('¿', '_').
										 replace('/', '_').replace('?', '_');
			}
			else if (line.charAt(0) == '1')
				opc_num++;

			if (line.charAt(0) == '1')
			{
				//vot_num_s = (new Long(vot_num)).toString();
				//opc_num_s = (new Long(opc_num)).toString();
				String keyName = idv + "_" + splitted[1].trim().replace(' ', '_').replace('¿','_').
																replace('/', '_').replace('?', '_');
				System.out.println("Creando claves para: " + keyName);
				GenKeys.generarClaves(0, path + keyName + "_privada.key",
										 path + keyName + "_publica.key");
				res = splitted[0].trim() + "," + splitted[1].trim() + "," + keyName;
			}
			else
				res = splitted[0].trim() + "," + splitted[1].trim();
			out.write(res);
			out.newLine();
		}
		out.close();
	}
}