package org.example;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.SysexMessage;

public class MidiInputReceiver implements Receiver {
    public String name;
    protected long lastTimestampInMicroseconds = Long.MIN_VALUE;

    public MidiInputReceiver(String name) {
        this.name = name;
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        if (message instanceof SysexMessage) {
            //System.out.println("Received SysexMessage:");
            SysexMessage sysexMessage = (SysexMessage) message;

//            System.out.print(sysexMessage.getData()[0] + " ");
//            System.out.print(sysexMessage.getData()[1] + " ");
//            System.out.print(sysexMessage.getData()[2] + " ");
//            System.out.print(sysexMessage.getData()[3] + " ");
//            System.out.print(sysexMessage.getData()[4] + " ");
//            System.out.print(sysexMessage.getData()[5] + " ");
//            System.out.print(sysexMessage.getData()[6] + " ");
//            System.out.print(sysexMessage.getData()[7] + " ");
//            System.out.println(sysexMessage.getData()[8]);

            byte encoder = sysexMessage.getData()[0];
            long microsecondsSinceMidnight = combine(sysexMessage.getData()[1], sysexMessage.getData()[2],
                    sysexMessage.getData()[3], sysexMessage.getData()[4], sysexMessage.getData()[5], sysexMessage.getData()[6]);

            // binary representation of the 6 timestamp bytes
//            binaerDarstellenVonByte ("", sysexMessage.getData()[1]);
//            System.out.print(" ");
//            binaerDarstellenVonByte ("", sysexMessage.getData()[2]);
//            System.out.print(" ");
//            binaerDarstellenVonByte ("", sysexMessage.getData()[3]);
//            System.out.print(" ");
//            binaerDarstellenVonByte ("", sysexMessage.getData()[4]);
//            System.out.print(" ");
//            binaerDarstellenVonByte ("", sysexMessage.getData()[5]);
//            System.out.print(" ");
//            binaerDarstellenVonByte ("", sysexMessage.getData()[6]);
//            System.out.println();


            int counterA = combine(sysexMessage.getData()[7], sysexMessage.getData()[8]);
            //System.out.println("Increment: " + counterA);
            System.out.print(encoder);
            System.out.print(' ');
            System.out.print(counterA);
            System.out.print(' ');
            //binaerDarstellenVonLong ("", microsecondsSinceMidnight);
            //System.out.println("Timestamp in microseconds: " + microsecondsSinceMidnight);
            System.out.print(microsecondsSinceMidnight);
            System.out.print(' ');
            System.out.println(microsecondsSinceMidnight - lastTimestampInMicroseconds);
            lastTimestampInMicroseconds = microsecondsSinceMidnight;
        }
    }

    @Override
    public void close() {
        // Nothing
    }

    // assumes hi and lo have top bit clear
    int combine(byte hi, byte lo) {
        int res = (hi << 7) | lo;  // combine the 7 bit chunks to 14 bits in the int
        res = res << 18 >> 18;  // sign-extend as 32 bit
        return res;
    }

    long combine(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6) {
        long res = 0;
        res = (long) b1 << 35;
        res = (res | (long) b2 << 28);
        res = (res | (long) b3 << 21);
        res = (res | (long) b4 << 14);
        res = (res | (long) b5 << 7);
        res = (res | (long) b6);
        return res;
    }

    public static void binaerDarstellenVonByte (String text, byte zahl) {

        byte maske = 0b00000001;
        char[] bitfolge = new char[8];

        for (int i = 0; i < 8; i++)
        {
            bitfolge[7 - i] = (zahl & maske) == 0 ? '0' : '1';
            maske = (byte) (maske << 1);
        }

        System.out.print(text);
        System.out.print(bitfolge);
    }


    public static void binaerDarstellenVonLong (String text, long zahl) {

        long maske = 0b0000000000000000000000000000000000000000000000000000000000000001;
        char[] bitfolge = new char[64];

        for (int i = 0; i < 64; i++)
        {
            bitfolge[63 - i] = (zahl & maske) == 0 ? '0' : '1';
            maske = maske << 1;
        }

        System.out.print(text);
        System.out.println(bitfolge);
    }
    public static void binaerDarstellenVonInt (String text, int zahl) {

        int maske = 0b00000000000000000000000000000001;
        char[] bitfolge = new char[32];

        for (int i = 0; i < 32; i++)
        {
            bitfolge[31 - i] = (zahl & maske) == 0 ? '0' : '1';
            maske = maske << 1;
        }

        System.out.print(text);
        System.out.println(bitfolge);
    }
}
