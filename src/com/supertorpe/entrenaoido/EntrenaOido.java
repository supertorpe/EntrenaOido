package com.supertorpe.entrenaoido;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.sound.midi.*;

public class EntrenaOido {
	
	private static final List<Character> NOTAS = Arrays.asList( 'C', 'D', 'E', 'F', 'G', 'A', 'B' );
	private static final int TONOS_OCTAVA = 12;
	private static final int NOTA_C0 = 24;

	private static final String PARAM_NOTAS = "notas";
	private static final String PARAM_MODO = "modo";
	private static final String PARAM_INSTRUMENTO = "instrumento";
	private static final String PARAM_DURACION = "duracion";
	private static final String PARAM_VOLUMEN = "volumen";
	private static final String PARAM_MOSTRAR_NOTA = "mostrarNota";
	
	// Parámetros de ejecución
	protected static int instrumento;
	protected static boolean secuencial;
	protected static int duracion;
	protected static int volumen;
	protected static boolean mostrarNota;
	private static List<String> notasCandidatas = new ArrayList<String>();
	private static List<Integer> valoresNotasCandidatas = new ArrayList<Integer>();

	private static int currentIndex = 0;
	private static Random rnd = new Random();
	
	private static final int calcularValorNota(char nota, int octava) {
		return NOTA_C0 + octava * TONOS_OCTAVA + NOTAS.indexOf(nota);
	}

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
	
	private static int siguienteNota() {
		int idx;
		if (secuencial) {
			currentIndex++;
			if (currentIndex >= valoresNotasCandidatas.size())
				currentIndex = 0;
			idx = currentIndex;
		} else {
			idx = rnd.nextInt(valoresNotasCandidatas.size());
		}
		if (mostrarNota)
			System.out.println(notasCandidatas.get(idx));
		return valoresNotasCandidatas.get(idx);
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
		String sInstrumento = leerParametro(PARAM_INSTRUMENTO, prop, true);
		instrumento = Integer.parseInt(sInstrumento);
		String sDuracion = leerParametro(PARAM_DURACION, prop, true);
		duracion = Integer.parseInt(sDuracion);
		String sVolumen = leerParametro(PARAM_VOLUMEN, prop, true);
		volumen = Integer.parseInt(sVolumen);
		String sMostrarNota = leerParametro(PARAM_MOSTRAR_NOTA, prop, true);
		mostrarNota = Boolean.parseBoolean(sMostrarNota);
		String[] notas = leerParametro(PARAM_NOTAS, prop, true).split(",");
		for (String nota : notas) {
			char nombreNota = nota.charAt(0);
			int octavaNota = Integer.parseInt(nota.substring(1));
			int valorNota = calcularValorNota(nombreNota, octavaNota);
			notasCandidatas.add(nota);
			valoresNotasCandidatas.add(valorNota);
		}
	}
	
	private static String leerStdin(String mensaje) throws IOException {
		System.out.print(mensaje);
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    String linea = br.readLine();
	    System.out.println();
	    return linea;
	}
	
	private static void ejecutar() throws Exception {
		MidiDevice.Info[] devices = MidiSystem.getMidiDeviceInfo();
		if (devices.length == 0)
			throw new Exception("No se han encontrado dispositivos MIDI");
		Synthesizer synth = MidiSystem.getSynthesizer();
		synth.open();
		try {		
			Instrument[] instr = synth.getDefaultSoundbank().getInstruments();
			synth.loadInstrument(instr[instrumento]);
			MidiChannel[] mc = synth.getChannels();
			mc[0].programChange(instrumento);
			boolean finalizado = false;
			boolean repetir = false;
			int nota = siguienteNota();
			String opcion;
			while (!finalizado) {
				mc[0].noteOn(nota, volumen);
				Thread.currentThread().sleep(duracion);
				mc[0].allNotesOff();
				opcion = leerStdin("Pulse [R] para repetir, [S] para siguiente u otra tecla para salir\n");
				if ("S".equalsIgnoreCase(opcion))
					nota = siguienteNota();
				else if (!"R".equalsIgnoreCase(opcion))
					break;
			}
		} finally {
			synth.close();
		}
	}

}