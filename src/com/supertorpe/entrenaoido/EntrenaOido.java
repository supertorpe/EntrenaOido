package com.supertorpe.entrenaoido;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class EntrenaOido {
	
	private static final String PARAM_NOTAS = "notas";
	private static final String PARAM_MODO = "modo";
	private static final String PARAM_EJERCICIO = "ejercicio";
	private static final String PARAM_INTERVALO = "intervalo.tipo";
	private static final String PARAM_INSTRUMENTO = "instrumento";
	private static final String PARAM_DURACION = "duracion";
	private static final String PARAM_VOLUMEN = "volumen";
	private static final String PARAM_MOSTRAR_NOTA = "mostrarNota";
	
	// Parámetros de ejecución
	protected static int instrumento;
	protected static boolean secuencial;
	protected static String ejercicio;
	protected static int intervalo;
	protected static int duracion;
	protected static int volumen;
	protected static boolean mostrarNota;
	private static List<List<Nota>> notasCandidatas = new ArrayList<List<Nota>>();

	private static int currentIndex = -1;
	private static Random rnd = new Random();
	
	private static Sintetizador sintetizador;
	
	private static String leerParametro(String paramName,
		Properties properties, boolean required) throws Exception {
		String result = System.getProperty(paramName);
		if (esCadenaVacia(result))
			result = properties.getProperty(paramName);
		if (esCadenaVacia(result) && required)
			throw new Exception("No está configurado el parámetro " + paramName);
		return result;
	}
	
	private static boolean esCadenaVacia(String cadena) {
		return (cadena == null || cadena.trim().length() == 0);
	}
	
	private static List<Nota> siguiente() {
		int idx;
		if (secuencial) {
			currentIndex++;
			if (currentIndex >= notasCandidatas.size())
				currentIndex = 0;
			idx = currentIndex;
		} else {
			do {
				idx = rnd.nextInt(notasCandidatas.size());
			} while (idx == currentIndex);
			currentIndex = idx;
		}
		if (mostrarNota)
			System.out.println(notasCandidatas.get(idx));
		return notasCandidatas.get(idx);
	}

	public static void main(String[] args) throws Exception {
		try {
			cargarConfiguracion(args);
			ejecutar();
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
	}
	
	protected static void cargarConfiguracion(String[] args) throws Exception {
		String cfgFile = "EntrenaOido.properties";
		// Si no se encuentra el fichero, buscarlo en el classpath
		if (!FileUtil.esFicheroLectura(cfgFile)) {
			String filename = FileUtil.searchFile(cfgFile);
			if (filename == null || !FileUtil.esFicheroLectura(cfgFile))
				throw new Exception("No se puede leer el fichero " + cfgFile);
			cfgFile = filename;
		}
		Properties prop = new Properties();
		try {
			prop.load(new BufferedInputStream(new FileInputStream(cfgFile)));
		} catch (Exception ex) {
			throw new Exception("No se ha podido cargar el fichero de propiedades " + cfgFile);
		}
		// Leer la configuración, dando prioridad a los parámetros de la JVM
		String sModo = leerParametro(PARAM_MODO, prop, true);
		secuencial = "SECUENCIAL".equals(sModo);
		ejercicio = leerParametro(PARAM_EJERCICIO, prop, true);
		if ("INTERVALO".equals(ejercicio)) {
			String sIntervalo = leerParametro(PARAM_INTERVALO, prop, true);
			intervalo = Integer.parseInt(sIntervalo);
		}
		String sInstrumento = leerParametro(PARAM_INSTRUMENTO, prop, true);
		instrumento = Integer.parseInt(sInstrumento);
		String sDuracion = leerParametro(PARAM_DURACION, prop, true);
		duracion = Integer.parseInt(sDuracion);
		String sVolumen = leerParametro(PARAM_VOLUMEN, prop, true);
		volumen = Integer.parseInt(sVolumen);
		String sMostrarNota = leerParametro(PARAM_MOSTRAR_NOTA, prop, true);
		mostrarNota = Boolean.parseBoolean(sMostrarNota);
		String[] grupos = leerParametro(PARAM_NOTAS, prop, true).split(" ");
		for (String grupo : grupos) {
			List<Nota> lista = new ArrayList<Nota>();
			notasCandidatas.add(lista);
			String[] notas = grupo.split(",");
			for (String nota : notas)
				lista.add(new Nota(nota));
		}
	}
	
	private static String leerStdin(String mensaje) throws IOException {
		System.out.print(mensaje);
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    String linea = br.readLine();
	    System.out.println();
	    return linea;
	}
	
	private static String descripcionTiempo(long millis) {
		return String.format("%d min, %d seg", 
			    TimeUnit.MILLISECONDS.toMinutes(millis),
			    TimeUnit.MILLISECONDS.toSeconds(millis) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
	}
	
	private static void ejecutar() throws Exception {
		sintetizador = new Sintetizador(instrumento);
		try {
			boolean finalizado = false;
			List<Nota> grupo = siguiente();
			String opcion;
			long numEjecuciones = 1, numRepeticiones = 0;
			long tiempo = System.currentTimeMillis();
			long tMax = 0, tMin = 0;
			boolean nuevoIntervalo = true;
			int intvlo = 0;
			while (!finalizado) {
				long t = System.currentTimeMillis();
				sintetizador.sonar(grupo, duracion, volumen, intvlo);
				opcion = leerStdin("Pulse [R] para repetir, [S] para siguiente u otra tecla para salir\n");
				if (!"R".equalsIgnoreCase(opcion)) {
					t = System.currentTimeMillis() - t;
					if (tMax == 0 || tMax < t)
						tMax = t;
					if (tMin == 0 || tMin > t)
						tMin = t;
				}
				if ("S".equalsIgnoreCase(opcion)) {
					if ("INTERVALO".equals(ejercicio) && nuevoIntervalo) {
						intvlo = intervalo;
						nuevoIntervalo = false;
					} else {
						intvlo = 0;
						nuevoIntervalo = true;
						grupo = siguiente();
						System.out.println("Nº ejecuciones: " + ++numEjecuciones + "; tiempo: " + descripcionTiempo(System.currentTimeMillis() - tiempo));
					}
				} else if ("R".equalsIgnoreCase(opcion)) {
					numRepeticiones++;
				} else {
					break;
				}
			}
			tiempo = System.currentTimeMillis() - tiempo;
			System.out.println("Resultados:");
			System.out.println("  - Nº Ejecuciones: " + numEjecuciones);
			System.out.println("  - Nº Repeticiones: " + numRepeticiones);
			System.out.println("  - Tiempo total: " + descripcionTiempo(tiempo));
			System.out.println("  - Tiempo medio: " + descripcionTiempo(tiempo / numEjecuciones));
			System.out.println("  - Tiempo máximo: " + descripcionTiempo(tMax));
			System.out.println("  - Tiempo mínimo: " + descripcionTiempo(tMin));
		} finally {
			sintetizador.close();
		}
	}

}
