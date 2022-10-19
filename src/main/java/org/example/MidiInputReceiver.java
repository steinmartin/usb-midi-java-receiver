package org.example;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.SysexMessage;

public class MidiInputReceiver implements Receiver {
    public String name;

    public MidiInputReceiver(String name) {
        this.name = name;
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        System.out.println("midi received");
        if (message instanceof SysexMessage) {
            System.out.println("Received SysexMessage");
            SysexMessage sysexMessage = (SysexMessage) message;
//            System.out.print(Byte.toString(sysexMessage.getData()[0]));
//            System.out.print(Integer.toHexString(sysexMessage.getData()[1]));
//            System.out.print(Integer.toHexString(sysexMessage.getData()[2]));
//            System.out.print(Integer.toHexString(sysexMessage.getData()[3]));
//            System.out.print(Integer.toHexString(sysexMessage.getData()[4]));
//            System.out.print(Integer.toHexString(sysexMessage.getData()[5]));
//            System.out.println(Integer.toHexString(sysexMessage.getData()[6]));
//
            System.out.print(sysexMessage.getData()[0]);
            System.out.print(' ');
            System.out.print(sysexMessage.getData()[1]);
            System.out.print(' ');
            System.out.print(sysexMessage.getData()[2]);
            System.out.print(' ');
            System.out.print(sysexMessage.getData()[3]);
            System.out.print(' ');
            System.out.print(sysexMessage.getData()[4]);
            System.out.print(' ');
            System.out.print(sysexMessage.getData()[5]);
            System.out.print(' ');
            System.out.print(sysexMessage.getData()[6]);
            System.out.print(' ');
            System.out.print(sysexMessage.getData()[7]);
            System.out.print(' ');
            System.out.println(sysexMessage.getData()[8]);

            //System.out.print(Integer.toHexString(sysexMessage.getData()[1]));
            //System.out.println(Integer.toHexString(sysexMessage.getData()[2]));
            System.out.println("Increment: " + combine(sysexMessage.getData()[1], sysexMessage.getData()[2]));
            System.out.println("Timestamp in microseconds: " + combine(sysexMessage.getData()[3], sysexMessage.getData()[4],
                    sysexMessage.getData()[5], sysexMessage.getData()[6], sysexMessage.getData()[7], sysexMessage.getData()[8]));
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
        //res = (b1 << 35) | (b2 << 28) | (b3 << 21) | (b4 << 14) | (b5 << 7) | b6;
        res = b6 | (b5 << 7) | (b4 << 14) | (b3 << 21) | (b2 << 28) | (b1 << 35);

//        res = b6 | (b5 << 7);
//        res = b5 | (b4 << 14);
//        res = b4 | (b3 << 21);
//        res = b3 | (b2 << 28);
//        res = b2 | (b1 << 35);
        res = res << 21 >> 21;  // sign-extend as 32 bit
        return res;
    }
}
