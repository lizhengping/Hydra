package com.hydra.visa;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Hwaipy
 */
public interface Argument {

  public byte[] getData();

  public static class StringArgument implements Argument {

    private final String content;

    public StringArgument(String content) {
      this.content = content;
    }

    @Override
    public byte[] getData() {
      return ("\"" + content + "\"").getBytes();
    }
  }

  public static class IntegerArgument implements Argument {

    private final int content;

    public IntegerArgument(int content) {
      this.content = content;
    }

    @Override
    public byte[] getData() {
      return ("" + content).getBytes();
    }
  }

  public static class LongArgument implements Argument {

    private final long content;

    public LongArgument(long content) {
      this.content = content;
    }

    @Override
    public byte[] getData() {
      return ("" + content).getBytes();
    }
  }

  public static class BlockArgument implements Argument {

    private final float[] data;

    public BlockArgument(float[] data) {
      this.data = data;
    }

    @Override
    public byte[] getData() {
      int dataSize = data.length * 4;
      String head = "#" + ("" + dataSize).length() + ("" + dataSize);
      ByteBuffer buffer = ByteBuffer.wrap(new byte[head.length() + dataSize]);
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      buffer.put(head.getBytes());
      for (float d : data) {
        buffer.putFloat(d);
      }
      return buffer.array();
    }
  }
}
