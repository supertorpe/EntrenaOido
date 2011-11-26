package com.supertorpe.entrenaoido;

import java.util.List;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;

public class Sintetizador {
	
	private Synthesizer synth;
	private MidiChannel channel;
	
	public Sintetizador(int instrumento) throws Exception {
		MidiDevice.Info[] devices = MidiSystem.getMidiDeviceInfo();
		if (devices.length == 0)
			throw new Exception("No se han encontrado dispositivos MIDI");
		synth = MidiSystem.getSynthesizer();
		synth.open();
		Instrument[] instr = synth.getDefaultSoundbank().getInstruments();
		synth.loadInstrument(instr[instrumento]);
		channel = synth.getChannels()[0];
		channel.programChange(instrumento);
	}

	public void close() throws Exception {
		synth.close();
	}
	
	public void sonar(List<Nota> notas, int duracion, int volumen) throws Exception {
		for (Nota nota : notas)
			channel.noteOn(nota.getTono(), volumen);
		Thread.sleep(duracion);
		channel.allNotesOff();
	}

	public void sonar(List<Nota> notas, int duracion, int volumen, int intervalo) throws Exception {
		for (Nota nota : notas)
			channel.noteOn(nota.getTono() + intervalo, volumen);
		Thread.sleep(duracion);
		channel.allNotesOff();
	}

	public void sonar(Nota nota, int duracion, int volumen) throws Exception {
		channel.noteOn(nota.getTono(), volumen);
		Thread.sleep(duracion);
		channel.allNotesOff();
	}

	public void sonar(Nota nota, int duracion, int volumen, int intervalo) throws Exception {
		channel.noteOn(nota.getTono() + intervalo, volumen);
		Thread.sleep(duracion);
		channel.allNotesOff();
	}

}
