package br.com.dmy.metronome;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;

import org.apache.log4j.Logger;

/**
 * A metronome.
 * 
 * @author Dave Briccetti
 */
public class MetronomeDemo extends JPanel {

	/**
	 * Version 1.0
	 */
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(MetronomeDemo.class);
	private int velocity = 127;
	private Thread thread; // New thread each time the metronome is turned on
	private final Runnable runnable = createRunnable();
	private long timeBetweenBeats;
	private MidiChannel channel = null;
	private boolean keepPlaying;
	private int note;
	private Synthesizer synthesizer;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JToggleButton metronomeButton;
	private JComboBox<String> soundChooser;
	private JSlider tempoChooser;
	private JSlider sliderNote;
	private GridBagConstraints gridBagConstraints_1;
	private JLabel lblNote;
	private JLabel lblNotevalue;
	private JSlider sliderInst;
	private JSlider sliderChannel;
	private JLabel lblInst;
	private JLabel lblInstValue;
	private JLabel lblChnl;
	private JLabel lblChannelValue;
	private JLabel lblVelocity;
	private JSlider sliderVelocity;
	private JLabel lblVelocityvalue;
	private JLabel lblPressure;
	private JLabel lblPressurevalue;
	// End of variables declaration//GEN-END:variables
	private JSlider sliderPressure;
	private JLabel lblPitchbend;
	private JSlider sliderPitchBend;
	private JLabel lblPitchbendvalue;
	protected Instrument instrument;

	public static void main(final String[] args) {
		JFrame f = new JFrame("DBSchools Metronome");
		final JPanel met = new MetronomeDemo();
		met.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		f.getContentPane().add(met, BorderLayout.CENTER);
		f.pack();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	/** Creates new form Metronome */
	public MetronomeDemo() {
		try {
			synthesizer = MidiSystem.getSynthesizer();
			synthesizer.open();
			channel = synthesizer.getChannels()[9];
			instrument = synthesizer.getLoadedInstruments()[9];
		} catch (MidiUnavailableException ex) {
			log.error(ex);
		}
		initComponents();
		setTempo(120);
		setNoteFromChoice();
		note = 37;
		metronomeButton.requestFocus();
	}

	/**
	 * Sets the tempo. May be called while the metronome is on.
	 * 
	 * @param beatsPerMinute
	 *            the tempo, in beats per minute
	 */
	public void setTempo(final int beatsPerMinute) {
		processTempoChange(beatsPerMinute);
		tempoChooser.setValue(beatsPerMinute);
	}

	/**
	 * Sets the MIDI note, in the percussion channel, to use for the metronome sound. See
	 * http://en.wikipedia.org/wiki/General_MIDI.
	 * 
	 * @param note
	 *            the MIDI note to use
	 */
	public void setNote(final int note) {
		this.note = note;
	}

	/**
	 * Stops the metronome.
	 */
	public void stop() {
		keepPlaying = false;
		if (thread != null) {
			thread.interrupt(); // Interrupt the sleep
		}
	}

	// private Runnable createRunnable() {
	// return new Runnable() {
	//
	// @Override
	// public void run() {
	// final long startTime = System.currentTimeMillis();
	// long wokeLateBy = 0;
	//
	// while (keepPlaying) {
	// // Someone could change note while we sleep. Make sure we
	// // turn on and off the same note.
	// final int noteForThisBeat = note;
	//
	// if (wokeLateBy > 10) {
	// log.debug("Woke late by " + wokeLateBy);
	// } else {
	// channel.noteOn(noteForThisBeat, velocity);
	// }
	// final long currentTimeBeforeSleep = System.currentTimeMillis();
	// final long currentLag = (currentTimeBeforeSleep - startTime) % timeBetweenBeats;
	// final long sleepTime = timeBetweenBeats - currentLag;
	// final long expectedWakeTime = currentTimeBeforeSleep + sleepTime;
	// try {
	// Thread.sleep(sleepTime);
	// } catch (InterruptedException ex) {
	// log.debug("Interrupted");
	// }
	// wokeLateBy = System.currentTimeMillis() - expectedWakeTime;
	// channel.noteOff(noteForThisBeat);
	// }
	// log.debug("Thread ending");
	// }
	// };
	// }

	private Runnable createRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				long wokeLateOrEarlyBy = 0;

				while (keepPlaying) {
					// Someone could change note while we sleep. Make sure we
					// turn on and off the same note.
					final int noteForThisBeat = note;

					// System.out.println ("late(+)/early(-): " + wokeLateOrEarlyBy);
					// System.out.println("Note: " + noteForThisBeat + " Velo: " + velocity);
					channel.noteOn(noteForThisBeat, velocity);

					final long currentTimeBeforeSleep = System.currentTimeMillis();
					// correct time to sleep by previous error, to keep the overall tempo
					final long sleepTime = timeBetweenBeats - wokeLateOrEarlyBy;
					final long expectedWakeTime = currentTimeBeforeSleep + sleepTime;
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException ex) {
						// log.debug("Interrupted");
					}
					wokeLateOrEarlyBy = System.currentTimeMillis() - expectedWakeTime;

					channel.noteOff(noteForThisBeat);
				}
				// log.debug("Thread ending");
			}

		};
	}

	/*
	 * Possivel Correção do metodo run:
	 * public void run() {
		long wokeLateOrEarlyBy = 0;
	
	while (keepPlaying) {
	// Someone could change note while we sleep. Make sure we 
	// turn on and off the same note.
	final int noteForThisBeat = note; 
	
	// System.out.println ("late(+)/early(-): " + wokeLateOrEarlyBy);
	
	channel.noteOn(noteForThisBeat, velocity);
	
	final long currentTimeBeforeSleep = System.currentTimeMillis();
	//correct time to sleep by previous error, to keep the overall tempo
	final long sleepTime = timeBetweenBeats - wokeLateOrEarlyBy;
	final long expectedWakeTime = currentTimeBeforeSleep + sleepTime;
	try {
	Thread.sleep(sleepTime);
	} catch (InterruptedException ex) {
	// log.debug("Interrupted");
	}
	wokeLateOrEarlyBy = System.currentTimeMillis() - expectedWakeTime;
	
	channel.noteOff(noteForThisBeat);
	}
	// log.debug("Thread ending");
	} 
	
	 */

	private void processTempoChange(final int beatsPerMinute) {
		setMetronomeButtonText(beatsPerMinute);
		timeBetweenBeats = 1000 * 60 / beatsPerMinute;
		restartAtEndOfBeatIfRunning();
	}

	private void restartAtEndOfBeatIfRunning() {
		if (keepPlaying) {
			keepPlaying = false;
			try {
				thread.join();
			} catch (InterruptedException ex) {
				log.debug(ex);
			}
			startThread();
		}
	}

	private void setMetronomeButtonText(final int beatsPerMinute) {
		metronomeButton.setText(Integer.toString(beatsPerMinute));
	}

	private void startThread() {
		if (channel != null) {
			keepPlaying = true;
			thread = new Thread(runnable, "Metronome");
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}
	}

	void setNoteFromChoice() {
		setNote(((PercussionSound) soundChooser.getSelectedItem()).getMidiNote());
	}

	static private class PercussionSound {
		private final String name;
		private final int midiNote;

		public PercussionSound(final String name, final int midiNote) {
			this.name = name;
			this.midiNote = midiNote;
		}

		public int getMidiNote() {
			return midiNote;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	private PercussionSound[] getSounds() {
		return new PercussionSound[] { new PercussionSound("Claves", 75), new PercussionSound("Cow Bell", 56),
				new PercussionSound("High Bongo", 60), new PercussionSound("Low Bongo", 61),
				new PercussionSound("High Wood Block", 76), new PercussionSound("Low Wood Block", 77), };
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
	 * content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		GridBagConstraints gridBagConstraints;
		soundChooser = new JComboBox<>();
		tempoChooser = new JSlider();

		setBorder(javax.swing.BorderFactory.createTitledBorder("Metronome"));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 10 };
		setLayout(gridBagLayout);

		soundChooser.setModel(new DefaultComboBoxModel(getSounds()));
		soundChooser.setToolTipText("Select the sound to use");
		soundChooser.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				soundChooserActionPerformed(evt);
			}
		});
		gridBagConstraints_1 = new java.awt.GridBagConstraints();
		gridBagConstraints_1.gridwidth = 2;
		gridBagConstraints_1.insets = new Insets(0, 0, 5, 5);
		gridBagConstraints_1.gridx = 1;
		gridBagConstraints_1.gridy = 0;
		add(soundChooser, gridBagConstraints_1);

		tempoChooser.setMaximum(208);
		tempoChooser.setMinimum(40);
		tempoChooser.setValue(120);
		tempoChooser.addChangeListener(new javax.swing.event.ChangeListener() {
			@Override
			public void stateChanged(final javax.swing.event.ChangeEvent evt) {
				tempoChooserStateChanged(evt);
			}
		});

		metronomeButton = new JToggleButton();

		metronomeButton.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
		metronomeButton.setText("Beat");
		metronomeButton.setToolTipText("Start and stop the metronome");
		metronomeButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				metronomeButtonActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.insets = new Insets(0, 4, 5, 5);
		add(metronomeButton, gridBagConstraints);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(4, 0, 5, 5);
		add(tempoChooser, gridBagConstraints);

		sliderNote = new JSlider();
		sliderNote.setMinimum(27);
		sliderNote.setMaximum(87);
		sliderNote.setValue(27);
		sliderNote.addChangeListener(new javax.swing.event.ChangeListener() {
			@Override
			public void stateChanged(final javax.swing.event.ChangeEvent evt) {
				final int note = sliderNote.getValue();
				setNote(note);
				lblNotevalue.setText(String.valueOf(note));
			}
		});

		lblNote = new JLabel("Note:");
		GridBagConstraints gbc_lblNote = new GridBagConstraints();
		gbc_lblNote.insets = new Insets(0, 0, 5, 5);
		gbc_lblNote.gridx = 0;
		gbc_lblNote.gridy = 2;
		add(lblNote, gbc_lblNote);
		GridBagConstraints gbc_sliderNote = new GridBagConstraints();
		gbc_sliderNote.insets = new Insets(0, 0, 5, 5);
		gbc_sliderNote.fill = GridBagConstraints.HORIZONTAL;
		gbc_sliderNote.gridwidth = 2;
		gbc_sliderNote.gridx = 1;
		gbc_sliderNote.gridy = 2;
		add(sliderNote, gbc_sliderNote);

		lblNotevalue = new JLabel("NoteValue");
		lblNotevalue.setText("0");
		GridBagConstraints gbc_lblNotevalue = new GridBagConstraints();
		gbc_lblNotevalue.insets = new Insets(0, 0, 5, 0);
		gbc_lblNotevalue.gridx = 3;
		gbc_lblNotevalue.gridy = 2;
		add(lblNotevalue, gbc_lblNotevalue);

		lblInst = new JLabel("Inst.:");
		GridBagConstraints gbc_lblInst = new GridBagConstraints();
		gbc_lblInst.insets = new Insets(0, 0, 5, 5);
		gbc_lblInst.gridx = 0;
		gbc_lblInst.gridy = 3;
		add(lblInst, gbc_lblInst);

		sliderInst = new JSlider();
		sliderInst.setMinimum(0);
		sliderInst.setMaximum(127);
		sliderInst.setValue(0);
		sliderInst.setEnabled(false);
		sliderInst.addChangeListener(new javax.swing.event.ChangeListener() {
			@Override
			public void stateChanged(final javax.swing.event.ChangeEvent evt) {
				final int inst = sliderInst.getValue();
				lblInstValue.setText(String.valueOf(inst));
				instrument = synthesizer.getLoadedInstruments()[inst];
				channel.programChange(instrument.getPatch().getProgram());
			}
		});
		GridBagConstraints gbc_sliderInst = new GridBagConstraints();
		gbc_sliderInst.fill = GridBagConstraints.BOTH;
		gbc_sliderInst.insets = new Insets(0, 0, 5, 5);
		gbc_sliderInst.gridwidth = 2;
		gbc_sliderInst.gridx = 1;
		gbc_sliderInst.gridy = 3;
		add(sliderInst, gbc_sliderInst);

		lblInstValue = new JLabel("InstValue");
		lblInstValue.setText("0");
		GridBagConstraints gbc_lblInstValue = new GridBagConstraints();
		gbc_lblInstValue.fill = GridBagConstraints.BOTH;
		gbc_lblInstValue.insets = new Insets(0, 0, 5, 15);
		gbc_lblInstValue.gridx = 3;
		gbc_lblInstValue.gridy = 3;
		add(lblInstValue, gbc_lblInstValue);

		lblChnl = new JLabel("lblChnl:");
		GridBagConstraints gbc_lblChnl = new GridBagConstraints();
		gbc_lblChnl.insets = new Insets(0, 0, 5, 5);
		gbc_lblChnl.gridx = 0;
		gbc_lblChnl.gridy = 4;
		add(lblChnl, gbc_lblChnl);

		sliderChannel = new JSlider();
		sliderChannel.setMaximum(1);
		sliderChannel.setMinimum(0);
		sliderChannel.setValue(1);
		sliderChannel.addChangeListener(new javax.swing.event.ChangeListener() {
			@Override
			public void stateChanged(final javax.swing.event.ChangeEvent evt) {
				int channelNumber = sliderChannel.getValue();
				channelNumber = channelNumber == 1 ? 9 : 0;
				int max = 127;
				int min = 0;
				if (channelNumber == 9) {
					max = 87;
					min = 27;
				}
				sliderNote.setMaximum(max);
				sliderNote.setMinimum(min);
				sliderNote.setValue(min);
				sliderInst.setEnabled(channelNumber != 9 && channelNumber != 1);
				lblChannelValue.setText(String.valueOf(channelNumber));
				channel = synthesizer.getChannels()[channelNumber];
			}
		});

		GridBagConstraints gbc_sliderChannel = new GridBagConstraints();
		gbc_sliderChannel.insets = new Insets(0, 0, 5, 5);
		gbc_sliderChannel.gridwidth = 2;
		gbc_sliderChannel.gridx = 1;
		gbc_sliderChannel.gridy = 4;
		add(sliderChannel, gbc_sliderChannel);

		lblChannelValue = new JLabel("lblChannelValue");
		lblChannelValue.setText("1");
		GridBagConstraints gbc_lblChannelValue = new GridBagConstraints();
		gbc_lblChannelValue.insets = new Insets(0, 0, 5, 0);
		gbc_lblChannelValue.gridx = 3;
		gbc_lblChannelValue.gridy = 4;
		add(lblChannelValue, gbc_lblChannelValue);

		lblVelocity = new JLabel("Velocity:");
		GridBagConstraints gbc_lblVelocity = new GridBagConstraints();
		gbc_lblVelocity.insets = new Insets(0, 0, 5, 5);
		gbc_lblVelocity.gridx = 0;
		gbc_lblVelocity.gridy = 5;
		add(lblVelocity, gbc_lblVelocity);

		sliderVelocity = new JSlider();
		sliderVelocity.setMaximum(127);
		sliderVelocity.setMinimum(0);
		sliderVelocity.setValue(127);
		sliderVelocity.addChangeListener(new javax.swing.event.ChangeListener() {
			@Override
			public void stateChanged(final javax.swing.event.ChangeEvent evt) {
				int valocityValue = sliderVelocity.getValue();
				velocity = valocityValue;
				lblVelocityvalue.setText(String.valueOf(velocity));
			}
		});
		GridBagConstraints gbc_slider = new GridBagConstraints();
		gbc_slider.gridwidth = 2;
		gbc_slider.insets = new Insets(0, 0, 5, 5);
		gbc_slider.gridx = 1;
		gbc_slider.gridy = 5;
		add(sliderVelocity, gbc_slider);

		lblVelocityvalue = new JLabel("127");
		GridBagConstraints gbc_lblVelocityvalue = new GridBagConstraints();
		gbc_lblVelocityvalue.insets = new Insets(0, 0, 5, 0);
		gbc_lblVelocityvalue.gridx = 3;
		gbc_lblVelocityvalue.gridy = 5;
		add(lblVelocityvalue, gbc_lblVelocityvalue);

		lblPressure = new JLabel("Pressure:");
		GridBagConstraints gbc_lblPressure = new GridBagConstraints();
		gbc_lblPressure.insets = new Insets(0, 0, 5, 5);
		gbc_lblPressure.gridx = 0;
		gbc_lblPressure.gridy = 6;
		add(lblPressure, gbc_lblPressure);

		sliderPressure = new JSlider();
		sliderPressure.setMinimum(0);
		sliderPressure.setMaximum(127);
		sliderPressure.setValue(127);
		sliderPressure.addChangeListener(new javax.swing.event.ChangeListener() {
			@Override
			public void stateChanged(final javax.swing.event.ChangeEvent evt) {
				int pressure = sliderPressure.getValue();
				channel.setChannelPressure(pressure);
				lblPressurevalue.setText(String.valueOf(pressure));
			}
		});
		GridBagConstraints gbc_sliderPressure = new GridBagConstraints();
		gbc_sliderPressure.gridwidth = 2;
		gbc_sliderPressure.insets = new Insets(0, 0, 5, 5);
		gbc_sliderPressure.gridx = 1;
		gbc_sliderPressure.gridy = 6;
		add(sliderPressure, gbc_sliderPressure);

		lblPressurevalue = new JLabel("127");
		GridBagConstraints gbc_lblPressurevalue = new GridBagConstraints();
		gbc_lblPressurevalue.insets = new Insets(0, 0, 5, 0);
		gbc_lblPressurevalue.gridx = 3;
		gbc_lblPressurevalue.gridy = 6;
		add(lblPressurevalue, gbc_lblPressurevalue);

		lblPitchbend = new JLabel("PitchBend:");
		GridBagConstraints gbc_lblPitchbend = new GridBagConstraints();
		gbc_lblPitchbend.insets = new Insets(0, 0, 0, 5);
		gbc_lblPitchbend.gridx = 0;
		gbc_lblPitchbend.gridy = 7;
		add(lblPitchbend, gbc_lblPitchbend);

		sliderPitchBend = new JSlider();
		sliderPitchBend.setMinimum(0);
		sliderPitchBend.setMaximum(16383);
		sliderPitchBend.setValue(8192);
		sliderPitchBend.addChangeListener(new javax.swing.event.ChangeListener() {
			@Override
			public void stateChanged(final javax.swing.event.ChangeEvent evt) {
				int pitch = sliderPitchBend.getValue();
				channel.setPitchBend(pitch);
				lblPitchbendvalue.setText(String.valueOf(pitch));
			}
		});
		GridBagConstraints gbc_sliderPB = new GridBagConstraints();
		gbc_sliderPB.gridwidth = 2;
		gbc_sliderPB.insets = new Insets(0, 0, 0, 5);
		gbc_sliderPB.gridx = 1;
		gbc_sliderPB.gridy = 7;
		add(sliderPitchBend, gbc_sliderPB);

		lblPitchbendvalue = new JLabel("0");
		GridBagConstraints gbc_lblPitchbendvalue = new GridBagConstraints();
		gbc_lblPitchbendvalue.gridx = 3;
		gbc_lblPitchbendvalue.gridy = 7;
		add(lblPitchbendvalue, gbc_lblPitchbendvalue);
	}// </editor-fold>//GEN-END:initComponents

	private void metronomeButtonActionPerformed(final ActionEvent evt) {// GEN-FIRST:event_metronomeButtonActionPerformed

		if (metronomeButton.isSelected()) {
			startThread();
		} else {
			stop();
		}

	}// GEN-LAST:event_metronomeButtonActionPerformed

	private void soundChooserActionPerformed(final ActionEvent evt) {// GEN-FIRST:event_soundChooserActionPerformed
		setNoteFromChoice();
	}// GEN-LAST:event_soundChooserActionPerformed

	private void tempoChooserStateChanged(final ChangeEvent evt) {// GEN-FIRST:event_tempoChooserStateChanged
		final int tempo = tempoChooser.getValue();
		if (((JSlider) evt.getSource()).getValueIsAdjusting()) {
			setMetronomeButtonText(tempo);
		} else {
			processTempoChange(tempo);
		}
	}// GEN-LAST:event_tempoChooserStateChanged
}