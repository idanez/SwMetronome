package br.com.dmy.metronome.model;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class Metronome {

	private Synthesizer synthesizer;
	private MidiChannel channel = null;
	private int beatsPerMinute;
	private int note;
	private boolean keepPlaying;
	private final int velocity = 127;
	private long timeBetweenBeats;
	private Thread thread;
	private final Runnable runnable;

	/*
	 * Functionalities:
	 * 1 - Play -> tested
	 * 2 - Stop -> tested
	 * 3 - Define the instrument and be able to change it while playing
	 * 4 - Define the velocity and be able to change it while playing
	 * 5 - Define the tempo and be able to change it while playing
	 * 6 - Define the bpm and be able to change it while playing
	 * 7 - Check if it is playing
	 * 8 - Send on message each pulse
	 * 9 - Set a timer to automatically stop the metronome
	 */

	public Metronome() throws MidiUnavailableException {
		runnable = createRunnable();
		synthesizer = MidiSystem.getSynthesizer();
		synthesizer.open();
		channel = synthesizer.getChannels()[0]; // Get only the channel 0
	}

	public void play(int beatsPerMinute) {
		this.beatsPerMinute = beatsPerMinute;			
		startMetronome();
	}
	
	public boolean isPlaying() {		
		return keepPlaying;
	}

	public void stop() {
		keepPlaying = false;
		if (thread != null) {
			thread.interrupt(); // Interrupt the sleep
		}
	}

	private void startMetronome() {
		if (channel != null) {
			calculateTimeBetweetBeats();
			restartAtEndOfBeatIfRunning();
			keepPlaying = true;
			thread = new Thread(runnable, "Metronome");
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}
	}
	
	private void calculateTimeBetweetBeats() {
		timeBetweenBeats = 1000 * 60 / beatsPerMinute;
	}

	private void restartAtEndOfBeatIfRunning() {
		if (keepPlaying) {
			keepPlaying = false;
			try {
				thread.join();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	private Runnable createRunnable() {
		return new Runnable() {
			long wokeLateOrEarlyBy = 0;
			@Override
			public void run() {				
				while (keepPlaying) {
					// Someone could change note while we sleep. Make sure we
					// turn on and off the same note.
					final int noteForThisBeat = note;
					channel.noteOn(noteForThisBeat, velocity);
					final long sleepTime = calculateSleepTime(wokeLateOrEarlyBy);
					final long expectedWakeTime = calculateExpectedWakeTime(sleepTime);
					try {
						Thread.sleep(calculateSleepTime(wokeLateOrEarlyBy));
					} catch (InterruptedException ex) {
						// log.debug("Interrupted");
					}
					wokeLateOrEarlyBy = System.currentTimeMillis() - expectedWakeTime;
					channel.noteOff(noteForThisBeat);
				}
			}

			private long calculateSleepTime(long wokeLateOrEarlyBy) {
				return timeBetweenBeats - wokeLateOrEarlyBy;
			}

			private long calculateExpectedWakeTime(long sleepTime) {
				final long currentTimeBeforeSleep = System.currentTimeMillis();
				// correct time to sleep by previous error, to keep the overall tempo				
				final long expectedWakeTime = currentTimeBeforeSleep + sleepTime;
				return expectedWakeTime;
			}

		};
	}


}
