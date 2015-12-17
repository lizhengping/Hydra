package com.hwaipy.vi.tdc.serialize;

import java.nio.ByteBuffer;
import java.util.List;

/**
 *
 * @author Hwaipy
 */
public class Serializer {

  private final long resolution;
  private final int channelDivision;
  private final int channelBit;
  private byte previousByte = 0;
  private byte previousByteRemaining = 8;

  public Serializer(int channel, long resolution) {
    this.resolution = resolution;
    int cd = 1;
    int cb = 0;
    while (channel > 0) {
      channel >>= 1;
      cd <<= 1;
      cb++;
    }
    this.channelDivision = cd;
    this.channelBit = cb;
  }

  public byte[] serialize(List<Long> timeEvents) {
    long startTime = timeEvents.get(0) / channelDivision;
    long endTime = timeEvents.get(timeEvents.size() - 1) / channelDivision;
    int timeBit = optimizeTimeBit();
    int timeEventBit = timeBit + channelBit;
    long timeEventMask = 1 << timeEventBit;
    long carryUnit = ((long) 1) << timeBit;
    long dataBitL = timeEvents.size() * (channelBit + timeBit + 1) + ((endTime - startTime) / carryUnit) + 192;
    if (dataBitL > Integer.MAX_VALUE) {
      throw new RuntimeException("Data rate not acceptable.");
    }
    int dataBit = (int) dataBitL;
    int dataByte = ((dataBit + 7) / 8);
    byte[] bytes = new byte[dataByte];
    ByteBuffer data = ByteBuffer.wrap(bytes);
    data.putInt(dataByte);
    data.putShort((short) timeBit);
    data.putShort((short) channelBit);
    data.putLong(startTime);
    data.putLong(resolution);
    long previousCarry = 0;
    for (Long timeEvent : timeEvents) {
      int channel = (int) (timeEvent % channelDivision);
      long fullTime = (timeEvent / channelDivision - startTime) / resolution;
      long carry = fullTime / carryUnit;
      long time = fullTime % carryUnit;
      long sTimeEvent = ((time << channelBit) + channel) | timeEventMask;
      int carrys = (int) (carry - previousCarry);
      while (carrys >= previousByteRemaining) {
        data.put(previousByte);
        carrys -= previousByteRemaining;
        previousByte = 0;
        previousByteRemaining = 8;
      }
      previousByteRemaining -= carrys;
      int timeEventRemaining = timeEventBit;
      while (timeEventRemaining >= previousByteRemaining) {
        byte t = (byte) ((sTimeEvent & ((1 << timeEventRemaining) - 1)) >> (timeEventRemaining - previousByteRemaining));
        previousByte |= t;
        data.put(previousByte);
        timeEventRemaining -= previousByteRemaining;
        previousByte = 0;
        previousByteRemaining = 8;
      }
      byte t = (byte) ((sTimeEvent & ((1 << timeEventRemaining) - 1)) << (previousByteRemaining - timeEventRemaining));
      previousByte |= t;
      previousByteRemaining -= timeEventRemaining;
      previousCarry = carry;
    }
    if (previousByteRemaining < 8) {
      data.put(previousByte);
    }
    return bytes;
  }

  private int optimizeTimeBit() {
    return 26;
  }
}
