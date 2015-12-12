package com.hwaipy.vi.tdc.adapters;

import com.hwaipy.vi.tdc.TDCDataAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Hwaipy
 */
public class ChannelMappingTDCDataAdapter implements TDCDataAdapter {

  private final int[] mapping;
  private int[] mappedEventCounts;
  private ArrayList<Integer> unmappedEventCount = new ArrayList<>();

  public ChannelMappingTDCDataAdapter(int[] mapping) {
    this.mapping = mapping;
    mappedEventCounts = new int[mapping.length];
  }

  @Override
  public Object offer(Object data) {
    if (data == null) {
      return null;
    }
    if (!(data instanceof List)) {
      throw new IllegalArgumentException("The input data of ChannelMappingTDCDataAdapter should be List.");
    }
    List timeEventSet = (List) data;
    ArrayList<List> preMappingTimeEventSet = new ArrayList<>(timeEventSet.size());
    for (Object timeEventsO : timeEventSet) {
      if (!(timeEventsO instanceof List)) {
        throw new IllegalArgumentException("The item of input List data of ChannelMappingTDCDataAdapter should be List.");
      }
      preMappingTimeEventSet.add((List) timeEventsO);
    }
    ArrayList<List> mappedTimeEventSet = new ArrayList<>(mapping.length);
    for (int mappingItem : mapping) {
      List timeEvents = preMappingTimeEventSet.get(mappingItem);
      mappedEventCounts[mappedTimeEventSet.size()] += timeEvents.size();
      mappedTimeEventSet.add(timeEvents);
      preMappingTimeEventSet.set(mappingItem, null);
    }
    while (preMappingTimeEventSet.size() > unmappedEventCount.size()) {
      unmappedEventCount.add(0);
    }
    for (int i = 0; i < preMappingTimeEventSet.size(); i++) {
      List preList = preMappingTimeEventSet.get(i);
      if (preList != null) {
        unmappedEventCount.set(i, unmappedEventCount.get(i) + preList.size());
      }
    }
    return mappedTimeEventSet;
  }

  @Override
  public Object flush(Object data) {
    return offer(data);
  }

  public int[] getMappedEventCounts() {
    return Arrays.copyOf(mappedEventCounts, mappedEventCounts.length);
  }

  public int[] getUnmappedEventCount() {
    int[] result = new int[unmappedEventCount.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = unmappedEventCount.get(i);
    }
    return result;
  }

}
