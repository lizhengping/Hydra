package com.hwaipy.vi.tdc.adapters;

import com.hwaipy.vi.tdc.TDCDataAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Hwaipy
 */
public class OrderFilterTDCDataAdapter implements TDCDataAdapter {

  private long[] previousTimes = new long[0];
  private int[] validEventCounts = new int[0];
  private int[] skippedEventCounts = new int[0];

  @Override
  public Object offer(Object data) {
    if (data == null) {
      return null;
    }
    if (!(data instanceof List)) {
      throw new IllegalArgumentException("The input data of OrderFilterTDCDataAdapter should be List.");
    }
    List timeEventSet = (List) data;
    if (previousTimes.length < timeEventSet.size()) {
      previousTimes = Arrays.copyOf(previousTimes, timeEventSet.size());
      validEventCounts = Arrays.copyOf(validEventCounts, timeEventSet.size());
      skippedEventCounts = Arrays.copyOf(skippedEventCounts, timeEventSet.size());
    }
    ArrayList<List> filteredTimeEventSet = new ArrayList<>(timeEventSet.size());
    for (int i = 0; i < timeEventSet.size(); i++) {
      Object preFilteredListO = timeEventSet.get(i);
      if (!(preFilteredListO instanceof List)) {
        throw new IllegalArgumentException("The item of input List data of OrderFilterTDCDataAdapter should be List.");
      }
      List preFilteredList = (List) preFilteredListO;
      filteredTimeEventSet.add(orderFiltering(preFilteredList, i));
    }
    return filteredTimeEventSet;
  }

  @Override
  public Object flush(Object data) {
    return offer(data);
  }

  private ArrayList<Long> orderFiltering(List<Long> timeEvents, int channel) {
    long previousTime = previousTimes[channel];
    ArrayList<Long> filteredTimeEvents = new ArrayList<>(timeEvents.size());
    for (long timeEvent : timeEvents) {
      if (timeEvent > previousTime) {
        filteredTimeEvents.add(timeEvent);
        previousTime = timeEvent;
      } else {
        skippedEventCounts[channel]++;
      }
    }
    previousTimes[channel] = previousTime;
    validEventCounts[channel] += filteredTimeEvents.size();
    return filteredTimeEvents;
  }

  public int[] getValidEventCounts() {
    return Arrays.copyOf(validEventCounts, validEventCounts.length);
  }

  public int[] getSkippedEventCounts() {
    return Arrays.copyOf(skippedEventCounts, skippedEventCounts.length);
  }
}
