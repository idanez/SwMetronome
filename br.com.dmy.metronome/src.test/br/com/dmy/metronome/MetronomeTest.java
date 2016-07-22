package br.com.dmy.metronome;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

	private Metronome getNewMetronome() {
		Metronome metronome = null;
		try {
			metronome = new Metronome();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		return metronome;
	}

	@Test
	public void testSimplyPlayAndStopMetronome() {
		Metronome metronome = getNewMetronome();
		metronome.play();
		assertTrue(metronome.isPlaying());
		metronome.stop();
		assertFalse(metronome.isPlaying());
	}

	@Test
	public void testDefineBpmAndPlay() {
		Metronome metronome = getNewMetronome();
		metronome.setBeatsPerMinute(60);
		metronome.play();
		assertTrue(metronome.isPlaying());
		metronome.stop();
	}

	@Test
	public void testDefineBpmAndChangeItWhilePlaying() {
		Metronome metronome = getNewMetronome();
		metronome.setBeatsPerMinute(60);
		metronome.play();
		assertTrue(metronome.isPlaying());
		metronome.setBeatsPerMinute(120);
		assertTrue(metronome.isPlaying());
		metronome.stop();
		assertFalse(metronome.isPlaying());
	}

	@Test
	public void testBpmWithReallyHighValue() {
		Metronome metronome = getNewMetronome();
		metronome.setBeatsPerMinute(6000);
		metronome.play();
		assertTrue(metronome.isPlaying());
		metronome.stop();
		assertFalse(metronome.isPlaying());
	}

	@Test
	public void testSetVolumeValue() {
		Metronome metronome = getNewMetronome();
		metronome.setLowBeatVolume(60);
		metronome.setHighBeatVolume(70);
		metronome.play();
		assertTrue(metronome.isPlaying());
		metronome.stop();
		assertFalse(metronome.isPlaying());
	}

}
