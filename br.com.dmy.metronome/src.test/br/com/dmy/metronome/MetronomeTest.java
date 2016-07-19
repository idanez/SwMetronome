package br.com.dmy.metronome;

import static org.junit.Assert.*;

import javax.sound.midi.MidiUnavailableException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.com.dmy.metronome.model.Metronome;

public class MetronomeTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSimplyPlayAndStopMetronome() {
		Metronome metronome;
		try {
			metronome = new Metronome();
			metronome.play(60);
			assertTrue(metronome.isPlaying());
			metronome.stop();
			assertFalse(metronome.isPlaying());
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

}
