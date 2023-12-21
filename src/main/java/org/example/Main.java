package org.example;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Example to test Java MIDI code to receive rotary encoder increment values and more from an Arduino based Potiboard.
 * The tested MIDI Sysex message is in 8 byte format, see class MidiInputReceiver for details.
 * Besides that there is a thread sending constantly a Sysex message to the MIDI-Potiboard to test bidirectional
 * communication.
 */
public class Main {
    private MidiDevice midiDevice;

    public static void main(String[] args) {
        new Main().start();
    }

    public void start() {
        try {
            // initialize your usb connected midi output device
            init();
            if (midiDevice != null) {
                midiDevice.open();

                // here we start the receive to get the data (potibard rotary increment counter values) from our MIDI potiboard
                // check class MidiInputReceiver
                midiDevice.getTransmitter().setReceiver(new MidiInputReceiver(midiDevice.toString()));
            }
        } catch (
                MidiUnavailableException mue) {
            mue.printStackTrace();
            System.exit(0);
        }

        // here we start to simulate sending nomenclature strings to the connected MIDI device
        simulateSendingMidiData();
    }

    /**
     * The init shows as example how to connect to a named USB Midi device.
     *
     * @throws MidiUnavailableException
     */
    public void init() throws MidiUnavailableException {
        MidiDevice.Info[] midiDevicesInfo = MidiSystem.getMidiDeviceInfo();

        for (MidiDevice.Info info : midiDevicesInfo) {
            MidiDevice md = MidiSystem.getMidiDevice(info);
            int midiDeviceMaxTransmitters = md.getMaxTransmitters();
            System.out.println("MidiDevice = " + info.getDescription() + ", maxTransmitters = " + midiDeviceMaxTransmitters + ", name = " + info.getName());

            // The correct device by name and if it has a transmitter (-1) passes the if gate
            if (midiDeviceMaxTransmitters == -1 && info.getDescription().equals("Teensy MIDI, USB MIDI, Teensy MIDI")) {
                midiDevice = md;
            }
        }
    }

    void simulateSendingMidiData() {

        SendingMidiDataTask sendingMidiDataTask01 = new SendingMidiDataTask("SendingMidiDataTask01");
        SendingMidiDataTask sendingMidiDataTask02 = new SendingMidiDataTask("SendingMidiDataTask02");

        ScheduledExecutorService executorService = Executors
                .newSingleThreadScheduledExecutor();
        ScheduledFuture<?> resultFuture = executorService.scheduleAtFixedRate(sendingMidiDataTask01, 100, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * SendingMidiTask is a test task (thread) which periodically sends out a MIDI SysexMessage transported in a Java Midi MidiMessage.
     * There are other ways to do this with Java MIDI but this solution is short, fast and easy to understand.
     *
     * @see MidiMessage
     */
    class SendingMidiDataTask implements Runnable {
        private final String taskName;

        public SendingMidiDataTask(String taskName) {
            this.taskName = taskName;
        }

        @Override
        public void run() {
            System.out.println("Sending Midi from: " + taskName);

            var deviceInfos = MidiSystem.getMidiDeviceInfo();

            try {

                Receiver receiver = MidiSystem.getReceiver();
                receiver.send(new RawMidiMessage(new byte[]{(byte) 0xF0, (byte) 'G', (byte) 'U', (byte) 'R',
                        (byte) '5', (byte) 'D', (byte) 'T', (byte) '8', (byte) 0xF7}), -1);

            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }


//            SysexMessage sysexMessage = new SysexMessage();
//            Instant instant = Instant.now();

//            @Override
//            public void run()  {
//                //System.out.println("Sending Midi from: " + taskName);
//                Receiver receiver = null;
//            try {
//                receiver = MidiSystem.getReceiver();
//            } catch (MidiUnavailableException e) {
//                System.err.println("MS: MIDI Device not available: ");
//                e.printStackTrace();
//                throw new RuntimeException(e);
//            }
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
//        }
//    }
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
