package com.hwaipy.vi.tdc.serialize;

import java.nio.ByteBuffer;

/**
 *
 * @author Hwaipy
 */
public class Deserializer {

  private byte previousByte = 0;
  private byte previousByteRemaining = 8;

  public Deserializer() {
  }

  public long[] serialize(byte[] dataBlock) {
    ByteBuffer data = ByteBuffer.wrap(dataBlock);
    int dataByte = data.getInt();
    int timeBit = data.getShort();
    int channelBit = data.getShort();
    long startTime = data.getLong();
    long resolution = data.getLong();
    long carryUnit = ((long) 1) << timeBit;
    long previousCarry = 0;
    if (dataByte != dataBlock.length) {
      throw new IllegalArgumentException("Data block size " + dataBlock.length + " not match the in-data value " + dataByte + ".");
    }
    for (int i = 6; i < dataBlock.length; i++) {
      byte b = dataBlock[i];
    }

//    for (Long timeEvent : timeEvents) {
//      int channel = (int) (timeEvent % channelDivision);
//      long fullTime = (timeEvent / channelDivision - startTime) / resolution;
//      long carry = fullTime / carryUnit;
//      long time = fullTime % carryUnit;
//      long sTimeEvent = (time << channelBit) + channel;
//      int carrys = (int) (carry - previousCarry);
//      while (carrys >= previousByteRemaining) {
//        data.put(previousByte);
//        carrys -= previousByteRemaining;
//        previousByte = 0;
//        previousByteRemaining = 8;
//      }
//      previousByteRemaining -= carrys;
//      int timeEventRemaining = timeEventBit;
//      while (timeEventRemaining >= previousByteRemaining) {
//        byte t = (byte) ((sTimeEvent & ((1 << timeEventRemaining) - 1)) >> (timeEventRemaining - previousByteRemaining));
//        previousByte |= t;
//        data.put(previousByte);
//        timeEventRemaining -= previousByteRemaining;
//        previousByte = 0;
//        previousByteRemaining = 8;
//      }
//      byte t = (byte) ((sTimeEvent & ((1 << timeEventRemaining) - 1)) << (previousByteRemaining - timeEventRemaining));
//      previousByte |= t;
//      previousByteRemaining -= timeEventRemaining;
//      previousCarry = carry;
//    }
//    if (previousByteRemaining < 8) {
//      data.put(previousByte);
//    }
//    return bytes;
    return null;
  }

  private int optimizeTimeBit() {
    return 26;
  }
}
