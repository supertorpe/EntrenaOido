package com.supertorpe.entrenaoido;

import java.util.Arrays;
import java.util.List;

public class Nota {
	
	private static final List<String> NOTAS = Arrays.asList( "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" );

	private static final int TONOS_OCTAVA = NOTAS.size();
	private static final int TONO_C0 = 12;
	
	private static final int calcularTono(String nota, int octava) {
		return TONO_C0 + octava * TONOS_OCTAVA + NOTAS.indexOf(nota);
	}

	private String nombre;
	private int octava;
	
	public Nota(String nombreCompleto) {
		nombre = nombreCompleto.substring(0, nombreCompleto.length() - 1);
		octava = Integer.parseInt(nombreCompleto.substring(nombreCompleto.length() - 1));
	}
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public int getOctava() {
		return octava;
	}
	public void setOctava(int octava) {
		this.octava = octava;
	}
	public int getTono() {
		return calcularTono(nombre, octava);
	}
	public String toString() {
		return nombre + octava;
		
	}

}
