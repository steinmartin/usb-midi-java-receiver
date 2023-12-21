#  MIDI (1.0) Communication test between a potiboard (Arduino based microcontroller with optical rotary encoders) and a Java application

 The test focuses on bidirectional MIDI 1.0 communication between the Java application using the MIDI capabilities of the Java Sound API and the Arduino-based microprocessor.

 The MIDI implementation of the Java Sound API is part of nearly every Java Runtime Environment, so no extra dependencies are needed.

 USB-Midi should run on the majority of operating systems out-of-the-box, without the need of configurations or driver installations.
 
The Arduino code runs on the microprocessor Teensy 4.1. For other Arduino based microprocessor types code adaptions might be necessary.

## Requirements

- Java 17 capable PC with USB Port
- Teensy 4.1 Microprocessor, other Arduino-based Microprocessor might work
- Incremental Optical Rotary Encoder, e.g. Grayhill 61KSxx-xxx or or Bourns ENS1J-B28-xxxxxx
- Level shifter (5V - 3.3V) depending on microprocessor and encoder used

## Installation

t.b.d.

## Usage

1. Connect microcontroller with USB wire to PC
2. Start the Arduino Code in Arduino IDE (e.g 2.2)
3. Start application in Java IDE (Maven project) 

The USB-MIDI device connection must be established before the Java application starts.
This is a limitation of the MIDI capabilities of the Java Sound API.
## Related Projects

none yet

## Credits


## License
This project is licensed under the terms of the GNU Lesser General Public License v3.0

[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)
