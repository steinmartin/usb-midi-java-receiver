package org.example;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

public class Main {
    private MidiDevice midiOutput;

    public static void main(String[] args) {
        new Main().start();
    }
    public void start() {
        // initialize your usb connected midi output device
        try {
          init();
          if (midiOutput != null) {
                midiOutput.open();
                MidiSystem.getTransmitter().setReceiver(new MidiInputReceiver(midiOutput.toString()));
          }
        } catch (MidiUnavailableException mue) {
            mue.printStackTrace();
            System.exit(0);
        }
    }

    public void init() throws MidiUnavailableException {
        MidiDevice.Info[] midiDevicesInfo = MidiSystem.getMidiDeviceInfo();

        for (MidiDevice.Info info : midiDevicesInfo) {
            MidiDevice midiDevice = MidiSystem.getMidiDevice(info);
            int midiDeviceMaxTransmitters = midiDevice.getMaxTransmitters();
            System.out.println("MidiDevice = " + info.getDescription() + ", maxTransmitters = " + midiDeviceMaxTransmitters);

            // The needed device if it has a transmitter (-1 or 1..n) passes the if gate
            if (midiDeviceMaxTransmitters != 0 && info.getDescription().equals("Teensy MIDI, USB MIDI, Teensy MIDI")) {
                midiOutput = midiDevice;
            }
        }
    }
}
