package com.hwaipy.vi.tdc;

import java.util.Timer;
import java.util.TimerTask;

/**
 * The refresh time is used to trigger write events. In some application, the
 * tdc datawriter tends to wait for enough data before write out. So the offer
 * method is invoked in a fixed frequency with arguments null to remaind the
 * adapters to write out the translated data.
 *
 * @author Hwaipy
 */
public class TDCParser {

  private final TDCDataProcessor processor;
  private long flushTime;
  private final TDCDataAdapter[] adapters;

  public TDCParser(TDCDataProcessor processor, long flushTime, TDCDataAdapter... adapters) {
    this.processor = processor;
    this.flushTime = flushTime;
    this.adapters = adapters;
    Timer timer = new Timer("TDCParser refresh timer", true);
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        flush();
      }
    }, flushTime, flushTime);
  }

  public TDCParser(TDCDataProcessor writer, TDCDataAdapter... adapters) {
    this(writer, 100, adapters);
  }

  public void offer(byte[] data) {
    Object d = data;
    for (TDCDataAdapter adapter : adapters) {
      synchronized (adapter) {
        d = adapter.offer(d);
      }
    }
    processor.process(d);
  }

  public void flush() {
    Object d = null;
    for (TDCDataAdapter adapter : adapters) {
      synchronized (adapter) {
        d = adapter.flush(d);
      }
    }
    processor.process(d);
  }
}
