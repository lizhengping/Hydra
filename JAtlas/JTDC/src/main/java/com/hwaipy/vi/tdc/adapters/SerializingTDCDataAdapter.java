//package com.hwaipy.vi.tdc.adapters;
//
//import com.hwaipy.vi.tdc.TDCDataAdapter;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
///**
// *
// * @author Hwaipy
// */
//public class SerializingTDCDataAdapter implements TDCDataAdapter {
//
//  private final MultiChannelBuffer multiChannelBuffer;
//
//  public SerializingTDCDataAdapter(int channel, long resolution) {
//    multiChannelBuffer = new MultiChannelBuffer(channel);
//  }
//
//  @Override
//  public byte[] offer(Object data) {
//    return translate(data, false);
//  }
//
//  @Override
//  public byte[] flush(Object data) {
//    return translate(data, true);
//  }
//
//  private byte[] translate(Object data, boolean force) {
//    if (data == null) {
//      return null;
//    }
//    if (!(data instanceof List)) {
//      throw new IllegalArgumentException("The input data of SerializingTDCDataAdapter should be List.");
//    }
//    List timeEventSet = (List) data;
//    if (timeEventSet.size() > multiChannelBuffer.channel) {
//      throw new IllegalArgumentException("The input data of SerializingTDCDataAdapter should only contains " + multiChannelBuffer.channel + " channels.");
//    }
//    for (int i = 0; i < timeEventSet.size(); i++) {
//      Object timeEventsO = timeEventSet.get(i);
//      if (!(timeEventsO instanceof List)) {
//        throw new IllegalArgumentException("The item of input List data of ChannelMappingTDCDataAdapter should be List.");
//      }
//      List timeEvents = (List) timeEventsO;
//      multiChannelBuffer.addAll(i, timeEvents);
//    }
//    if (force) {
//      doTranslate();
//    } else if (multiChannelBuffer.getRemaining() >= 100000) {
//      doTranslate();
//    }
//    return null;
//  }
//
//  private void doTranslate() {
//    //do serialization
//    multiChannelBuffer.clear();
//  }
//}
