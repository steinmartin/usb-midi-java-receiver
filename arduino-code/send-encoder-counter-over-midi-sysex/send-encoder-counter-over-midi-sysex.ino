#define ENCODER_OPTIMIZE_INTERRUPTS
#include <Encoder.h>
#include "Arduino.h"
#include "time.h"

//---------------------------------------------------------

struct LogEntry {
  uint32_t year;
  uint32_t month;
  uint32_t day;
  uint32_t hour;
  uint32_t minute;
  uint32_t second;
  uint32_t microseconds;
};

constexpr size_t CANBUFSIZE = 100;
LogEntry lbuf[CANBUFSIZE];
volatile unsigned int lbuf_head = 0;
volatile unsigned int lbuf_tail = 0;

uint64_t getTimeStamp(LogEntry* logEntry) {
  // read rtc (64bit, number of 32,768 kHz crystal periods)
  uint64_t periods;
  uint32_t hi1 = SNVS_HPRTCMR, lo1 = SNVS_HPRTCLR;
  while (true) {
    uint32_t hi2 = SNVS_HPRTCMR, lo2 = SNVS_HPRTCLR;
    if (lo1 == lo2 && hi1 == hi2) {
      periods = (uint64_t)hi2 << 32 | lo2;
      break;
    }
    hi1 = hi2;
    lo1 = lo2;
  }

  // calculate seconds and microseconds
  // keep in mind that precision is not higher than ~ 30 microseconds
  uint32_t microseconds = (1000000 * (periods % 32768)) / 32768;
  time_t sec = periods / 32768;

  tm t = *gmtime(&sec);  // calculate calendar data
  logEntry->microseconds = microseconds;
  logEntry->second = t.tm_sec;
  logEntry->minute = t.tm_min;
  logEntry->hour = t.tm_hour;
  logEntry->day = t.tm_mday;
  logEntry->month = t.tm_mon + 1;
  logEntry->year = t.tm_year + 1900;

  uint64_t microsecondsSinceMidnight = (logEntry->hour * 3600000000L) + (logEntry->minute * 60000000L) + (logEntry->second * 1000000L) + microseconds;

  // Serial.println(logEntry->hour *   3600000000L);
  // Serial.println(logEntry->minute *   60000000L);
  // Serial.println(logEntry->second *    1000000L);
  // Serial.println(microseconds);
  // Serial.println(microsecondsSinceMidnight);

  return microsecondsSinceMidnight;
}

//---------------------------------------------------------

const uint8_t SYSEX_ARRAY_SIZE = 9;

long positionA = -99999L;
long positionB = -99999L;

long positionAToDivide = -99999L;
long positionBToDivide = -99999L;

long positionADivided = -99999L;
long positionBDivided = -99999L;

long divider = 1L;
long newA = -99999L;
long newB = -99999L;

IntervalTimer timer;

Encoder encoderA(25, 26);
Encoder encoderB(39, 40);
//   avoid using pins with LEDs attached

void setup() {
  Serial.begin(9600);
  Serial.println("Up to eight knobs GSI Encoder Test:");

  // for receiving data
  usbMIDI.setHandleSystemExclusive(mySystemExclusive);
  //usbMIDI.setHandleSysEx(myReceiveSysEx);
  usbMIDI.setHandleNoteOn(OnNoteOn);
}

void loop() {

  // minimal allowed increment number
  //int increment = -8192;
  // maximal allowed increment number
  //int increment = 8191;

  // first the usual counter function, counting a number with every value change on both encoder channels
  newA = encoderA.read();
  newB = encoderB.read();

  // only watchdog, message contains no information
  uint8_t senderId = 0b01000000;
  // message contains info that first button was pressed
  // uint8_t senderId =  0b00010000;
  // message contains info that eighth encoder has new value
  // uint8_t senderId =  0b00001000;

  lbuf_head++;
  if (lbuf_head >= CANBUFSIZE) {
    lbuf_head = 0;
  }

  LogEntry* entry = &lbuf[lbuf_head];
  uint64_t microsecondsSinceMidnight = getTimeStamp(entry);

  // check if something has changed since the last time
  if ((positionAToDivide != newA) && (newA % divider == 0) && (newA != positionA)) {
    positionAToDivide = newA;
    positionADivided = newA / divider;
    senderId = 0b000000001;
    sendToUsbMidiSysex(senderId, positionADivided, microsecondsSinceMidnight);
    Serial.print(senderId);
    Serial.print(" ");
    Serial.print(positionADivided);
    Serial.print(" ");
    Serial.println(microsecondsSinceMidnight);
  }
  if ((positionBToDivide != newB) && (newB % divider == 0) && (newB != positionB)) {
    positionBToDivide = newB;
    positionBDivided = newB / divider;
    senderId = 0b000000010;
    sendToUsbMidiSysex(senderId, positionBDivided, microsecondsSinceMidnight);
    Serial.print(senderId);
    Serial.print(" ");
    Serial.print(positionBDivided);
    Serial.print(" ");
    Serial.println(microsecondsSinceMidnight);
  }
  positionA = positionADivided;
  positionB = positionBDivided;

  delay(10);

  usbMIDI.read();
}



void OnNoteOn(byte channel, byte note, byte velocity) {
  //digitalWrite(ledPin, HIGH); // Any Note-On turns on LED
  Serial.print(channel);
  Serial.print(note);
  Serial.println(velocity);
}

void mySystemExclusive(byte* data, unsigned int length) {
  Serial.print("SysEx Message: ");
  //printBytes(data, length);
  printChars(data, length);
  Serial.println();
}


void myReceiveSysEx(const uint8_t *buffer, uint16_t lenght, boolean flag) {

    static int count = 0;
    static boolean F0status = false;
    static boolean F7status = false;

    int SysExBuffer[348];

    Serial.print("lenght: ");
    Serial.println(lenght);

    for (uint16_t i = 0; i < lenght; i++) {

        SysExBuffer[count] = buffer[i];
        count++;

        if (buffer[i] == 0xF0) {
            F0status = true;
            Serial.println("F0 status");
        }

        if (buffer[i] == 0xF7) {
            F7status = true;
            Serial.println("F7 status");
            Serial.print("receive data lenght: ");
            Serial.println(count);
            count = 0;
        }
    }
}

void printBytes(const byte* data, unsigned int size) {
  while (size > 0) {
    byte b = *data++;
    if (b < 16) Serial.print('0');
    Serial.print(b, HEX);
    if (size > 1) Serial.print(' ');
    size = size - 1;
  }
}

void printChars(const byte* data, unsigned int size) {
  *data++;
  for (unsigned int i = 0; i < size - 2; i++) {
    byte b = *data++;

    //if (b < 16) Serial.print('0');
    Serial.print((char)b);
    //if (size > 2) Serial.print(' ');
  }
}

void sendToUsbMidiSysex(uint8_t senderId, int counter, uint64_t microsecondsSM) {
  uint8_t valueInArrayOfBytes[2];
  uint8_t timestampInArrayOfBytes[6];
  convertIntTo2Bytes(counter, valueInArrayOfBytes);
  convertUInt64to6Bytes(microsecondsSM, timestampInArrayOfBytes);
  const uint8_t* arrayOfBytes =
    createSysexArray(senderId, timestampInArrayOfBytes[0], timestampInArrayOfBytes[1], timestampInArrayOfBytes[2],
                     timestampInArrayOfBytes[3], timestampInArrayOfBytes[4], timestampInArrayOfBytes[5], valueInArrayOfBytes[0], valueInArrayOfBytes[1]);

  usbMIDI.sendSysEx(SYSEX_ARRAY_SIZE, arrayOfBytes);
  //  for(int i = 0; i < SYSEX_ARRAY_SIZE; ++i) {
  //    Serial.print(arrayOfBytes[i]);
  //    Serial.print(" ");
  //  }
  //  Serial.print("\n");
}

const void convertIntTo2Bytes(int value, uint8_t* convertedInt) {
  convertedInt[0] = (value >> 7) & 0x7F;
  convertedInt[1] = value & 0x7F;
}

const void convertUInt64to6Bytes(uint64_t value, uint8_t* convertedInt) {
  convertedInt[0] = (value >> 35) & 0x7F;
  convertedInt[1] = (value >> 28) & 0x7F;
  convertedInt[2] = (value >> 21) & 0x7F;
  convertedInt[3] = (value >> 14) & 0x7F;
  convertedInt[4] = (value >> 7) & 0x7F;
  convertedInt[5] = value & 0x7F;
}

const uint8_t* createSysexArray(uint8_t sender_id, uint8_t microsecs_timestamp_byte1,
                                uint8_t microsecs_timestamp_byte2, uint8_t microsecs_timestamp_byte3, uint8_t microsecs_timestamp_byte4,
                                uint8_t microsecs_timestamp_byte5, uint8_t microsecs_timestamp_byte6, uint8_t encoder01_counter_high, uint8_t encoder01_counter_low) {

  static uint8_t data[SYSEX_ARRAY_SIZE] = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
  data[0] = sender_id;
  data[1] = microsecs_timestamp_byte1;
  data[2] = microsecs_timestamp_byte2;
  data[3] = microsecs_timestamp_byte3;
  data[4] = microsecs_timestamp_byte4;
  data[5] = microsecs_timestamp_byte5;
  data[6] = microsecs_timestamp_byte6;
  data[7] = encoder01_counter_high;
  data[8] = encoder01_counter_low;
  return data;
}
