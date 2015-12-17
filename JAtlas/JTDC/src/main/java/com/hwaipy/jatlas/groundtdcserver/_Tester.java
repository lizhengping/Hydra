package com.hwaipy.jatlas.groundtdcserver;

import com.hwaipy.vi.tdc.TDCDataProcessor;
import com.hwaipy.vi.tdc.TDCParser;
import com.hwaipy.vi.tdc.adapters.BufferedOrderTDCDataAdapter;
import com.hwaipy.vi.tdc.adapters.DeserializingTDCDataAdapter;
import com.hwaipy.vi.tdc.adapters.GroundTDCDataAdapter;
import com.hwaipy.vi.tdc.adapters.SerializingTDCDataAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hwaipy
 */
public class _Tester {

  public void testParseTime() throws FileNotFoundException, IOException {
    GroundTDCDataAdapter groundTDCAdapter = new com.hwaipy.vi.tdc.adapters.GroundTDCDataAdapter(new int[]{0, 2, 3});
    BufferedOrderTDCDataAdapter bufferedOrderTDCDataAdapter = new com.hwaipy.vi.tdc.adapters.BufferedOrderTDCDataAdapter();
    SerializingTDCDataAdapter serializingTDCDataAdapter = new com.hwaipy.vi.tdc.adapters.SerializingTDCDataAdapter(3, 100);
    DeserializingTDCDataAdapter deserializingTDCDataAdapter = new com.hwaipy.vi.tdc.adapters.DeserializingTDCDataAdapter();
    DataProcessor processor = new DataProcessor();
    TDCParser parser = new com.hwaipy.vi.tdc.TDCParser(processor, groundTDCAdapter, bufferedOrderTDCDataAdapter, serializingTDCDataAdapter, deserializingTDCDataAdapter);
    File file = new File("/users/hwaipy/documents/data/samples/20151129114403-帧错误示例.dat");
//    File file = new File("/users/hwaipy/documents/data/samples/Ground_TDC_1.dat");
    int fileLength = (int) file.length();
    RandomAccessFile raf = new RandomAccessFile(file, "r");
    byte[] data = new byte[fileLength];
    if (raf.read(data) != fileLength) {
      throw new RuntimeException();
    }
    System.out.println("Data size: " + data.length);
    ArrayList<byte[]> dataSection = new ArrayList<>();
    int randomSeed = 198917;
    int position = 0;
    while (position < data.length) {
      int nextPosition = Math.min(data.length, position + randomSeed);
      randomSeed = (randomSeed * 2) % 4097 + 7;
      dataSection.add(Arrays.copyOfRange(data, position, nextPosition));
      position = nextPosition;
    }
    long startTime = System.nanoTime();
    for (byte[] section : dataSection) {
      parser.offer(section);
    }
    long endTime = System.nanoTime();
    System.out.println((endTime - startTime) / 1e9);
    System.out.println("----In GroundTDCDataAdapter----");
    System.out.println("Frame readed: " + groundTDCAdapter.getFrameCount());
    System.out.println("Frame valid: " + groundTDCAdapter.getValidFrameCount());
    System.out.println("Skipped in seeking head: " + groundTDCAdapter.getSkippedInSeekingHead());
    System.out.println("Unknown channel events: " + groundTDCAdapter.getUnknownChannelEventCount());
    System.out.println("Valid events: " + sum(groundTDCAdapter.getValidEventCount()) + " " + Arrays.toString(groundTDCAdapter.getValidEventCount()));
    System.out.println("Remaining: " + groundTDCAdapter.getDataRemaining());
    System.out.println("Addressed bytes: " + (groundTDCAdapter.getFrameCount() * 2048 + groundTDCAdapter.getSkippedInSeekingHead() + groundTDCAdapter.getDataRemaining()));
    System.out.println("----In bufferedOrderTDCDataAdapter----");
    System.out.println("SortOuttedCount: " + bufferedOrderTDCDataAdapter.getSortOuttedCount());
    System.out.println("----In SerializingTDCDataAdapter----");
  }

  private int sum(int... items) {
    int sum = 0;
    for (int item : items) {
      sum += item;
    }
    return sum;
  }

  private class DataProcessor implements TDCDataProcessor {

    @Override
    public void process(Object data) {
      if (data == null) {
        return;
      }
      if (!(data instanceof byte[])) {
        throw new RuntimeException();
      }
      byte[] dataB = (byte[]) data;
      try (FileOutputStream fos = new FileOutputStream("/users/hwaipy/documents/data/Outputs/out.dat", true)) {
        fos.write(dataB);
      } catch (Exception ex) {
        Logger.getLogger(_Tester.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
}
