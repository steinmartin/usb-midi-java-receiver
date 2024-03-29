package org.example;

import javax.sound.midi.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Example to test Java MIDI code to receive rotary encoder increment values and more from an Arduino based Potiboard.
 * The tested MIDI Sysex message is in 8 byte format, see class MidiInputReceiver for details.
 * Besides that there is a thread sending constantly a Sysex message to the MIDI-Potiboard to test bidirectional
 * communication.
 */
public class Main {
    private MidiDevice midiDeviceIn;
    private MidiDevice midiDeviceOut;

    public static void main(String[] args) {
        new Main().start();
    }

    public void start() {
        try {
            // initialize your usb connected midi output device
            init();
            if (midiDeviceIn != null) {
                midiDeviceIn.open();

                // here we start to receive to get the data (potibard rotary increment counter values) from our MIDI potiboard
                // check class MidiInputReceiver
                midiDeviceIn.getTransmitter().setReceiver(new MidiInputReceiver(midiDeviceIn.toString()));
            }
            if (midiDeviceOut != null) {
                midiDeviceOut.open();
            }
        } catch (MidiUnavailableException mue) {
            mue.printStackTrace();
            System.exit(0);
        }

        // here we start to simulate sending data  to the connected MIDI device
        simulateSendingMidiData();
    }

    /**
     * The init shows as example how to connect to a named USB Midi device.
     *
     * @throws MidiUnavailableException to halt on MIDI connection problems
     */
    public void init() throws MidiUnavailableException {
        MidiDevice.Info[] midiDevicesInfo = MidiSystem.getMidiDeviceInfo();

        for (MidiDevice.Info info : midiDevicesInfo) {
            MidiDevice md = MidiSystem.getMidiDevice(info);
            int midiDeviceMaxTransmitters = md.getMaxTransmitters();
            System.out.println("MidiDevice = " + info.getDescription() + ", maxTransmitters = " + midiDeviceMaxTransmitters + ", name = " + info.getName());

            if (midiDeviceMaxTransmitters == -1 && info.getDescription().equals("Teensy MIDI, USB MIDI, Teensy MIDI")) {
                System.out.print("MidiDevice = " + info.getDescription() + ", maxTransmitters = " + midiDeviceMaxTransmitters + ", name = " + info.getName());
                System.out.println(" for MIDI INPUT detected and initialized");
                midiDeviceIn= md;
            }

            if (midiDeviceMaxTransmitters == 0 && info.getDescription().equals("Teensy MIDI, USB MIDI, Teensy MIDI")) {
                System.out.print("MidiDevice = " + info.getDescription() + ", maxTransmitters = " + midiDeviceMaxTransmitters + ", name = " + info.getName());
                System.out.println("  for MIDI OUTPUT detected and initialized");
                midiDeviceOut= md;
            }

            // Windows 10 Midi name
//           if (midiDeviceMaxTransmitters == -1 && info.getName().equals("Teensy MIDI")) {
//                System.out.print("MidiDevice = " + info.getDescription() + ", maxTransmitters = " + midiDeviceMaxTransmitters + ", name = " + info.getName());
//                System.out.println(" for MIDI INPUT detected and initialized");
//                midiDeviceIn= md;
//            }
//
//            if (midiDeviceMaxTransmitters == 0 && info.getName().equals("Teensy MIDI")) {
//                System.out.print("MidiDevice = " + info.getDescription() + ", maxTransmitters = " + midiDeviceMaxTransmitters + ", name = " + info.getName());
//                System.out.println("  for MIDI OUTPUT detected and initialized");
//                midiDeviceOut= md;
//            }
        }
    }
    void simulateSendingMidiData() {

        SendingMidiDataTask sendingMidiDataTask01 = new SendingMidiDataTask("SendingMidiDataTask01");
        SendingMidiDataTask sendingMidiDataTask02 = new SendingMidiDataTask("SendingMidiDataTask02");

        ScheduledExecutorService  executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(sendingMidiDataTask01, 100, 500, TimeUnit.MILLISECONDS);
        executorService.scheduleAtFixedRate(sendingMidiDataTask02, 100, 600, TimeUnit.MILLISECONDS);
    }

    /**
     * SendingMidiTask is a test task (thread) which periodically sends out a MIDI SysexMessage transported in a Java Midi MidiMessage.
     * There are other ways to do this with Java MIDI but this solution is short, fast and easy to understand.
     *
     * @see MidiMessage
     */
    class SendingMidiDataTask implements Runnable {
        private final String taskName;
        private final Receiver receiver;

        public SendingMidiDataTask(String taskName) {
            this.taskName = taskName;
            try {
                //receiver = midiDeviceOut.getReceiver();

                receiver = MidiSystem.getReceiver();
                System.out.println("Receiver Name: " + receiver);
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            System.out.println("Sending Midi from: " + taskName);

            //   Here we send a SysEx message for testing
            receiver.send(new RawMidiMessage(new byte[]{(byte) 0xF0, (byte) 'G', (byte) 'U', (byte) 'R',
                    (byte) '5', (byte) 'D', (byte) 'T', (byte) '8', (byte) 0xF7}), -1);

            //           Here we send a note for testing
//            ShortMessage msg = new ShortMessage();
//            try {
//                msg.setMessage(ShortMessage.NOTE_ON, 1, 60, 93);
//            } catch (InvalidMidiDataException e) {
//                System.err.println("MS: MIDI Data not correct: ");
//                e.printStackTrace();
//                throw new RuntimeException(e);
//            }
//            long timeStamp = -1;
//            receiver.send(msg, timeStamp);
        }
    }

    public static class RawMidiMessage extends MidiMessage {
        public RawMidiMessage(byte[] data) {
            super(data);
        }

        @Override
        public Object clone() {
            return new RawMidiMessage(this.getMessage());
        }
    }
}
