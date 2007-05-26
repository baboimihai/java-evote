package eleccion;
/**
 * Esta clase sirve para parsear mi existencia
 */

import java.io.*;
import java.util.Hashtable;

import criptografia.GenKeys;


public class GenPadron
{
	private BufferedReader r_votante;
	private BufferedReader r_votacion;
	private BufferedWriter w_votante;
	private BufferedWriter w_votacion;
	long dni = 30240000;

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
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public GenPadron(String votante, String votacion) throws Exception
	{
		this.r_votante = new BufferedReader(new FileReader(votante));
		this.r_votacion = new BufferedReader(new FileReader(votacion));
		this.w_votante = new BufferedWriter(new FileWriter("votante_parsed.txt"));
		this.w_votacion = new BufferedWriter(new FileWriter("votacion_parsed.txt"));

		parse_votante(this.r_votante, this.w_votante);
		parse_votacion(this.r_votacion, this.w_votacion);
	}
	void parse_votante(BufferedReader votante, BufferedWriter out) throws Exception
	{
		String line;
		String res;
		String[] splitted;
		String dni_string;
		String path = InfoServidores.readKey("resources") + "votantes/";
		//String path = "";
		String val;
		Hashtable<String, String> tablita;

		tablita = new Hashtable<String, String>();

		while((line = votante.readLine()) != null)
		{
			splitted = line.split(",");
			if (line.charAt(0) == '1')
			{
				val = tablita.get(splitted[1]);
				if (val == null)
				{
					dni_string = (new Long(dni)).toString();
					dni++;
					tablita.put(splitted[1], dni_string);
				}
				else
				{
					dni_string = val;
				}
				GenKeys.generarClaves(0,path+dni_string+"_privada.key", path+dni_string+"_publica.key");
				res = splitted[0].trim()+","+splitted[1].trim()+","+dni_string;
			}
			else
				res = splitted[0].trim()+","+splitted[1].trim();
			out.write(res);
			out.newLine();
		}
		out.close();
	}

	void parse_votacion(BufferedReader votante, BufferedWriter out) throws Exception
	{
		String line;
		String res;
		String[] splitted;
		String path = InfoServidores.readKey("resources") + "votacion/";
		//String path = "";
		int vot_num = 0, opc_num = 0;
		String vot_num_s, opc_num_s;


		while((line = votante.readLine()) != null)
		{
			splitted = line.split(",");
			if (line.charAt(0) == '0')
			{
				vot_num++;
				opc_num = 0;
			}
			else if (line.charAt(0) == '1')
				opc_num++;

			if (line.charAt(0) == '1')
			{
				vot_num_s = (new Long(vot_num)).toString();
				opc_num_s = (new Long(opc_num)).toString();
				GenKeys.generarClaves(0,path+ vot_num_s +"_"+ opc_num_s +"_privada.key", path+vot_num_s +"_"+ opc_num_s +"_publica.key");
				res = splitted[0].trim()+","+splitted[1].trim()+"," + vot_num_s + "_" + opc_num_s;
			}
			else
				res = splitted[0].trim()+","+splitted[1].trim();
			out.write(res);
			out.newLine();
		}
		out.close();
	}
}