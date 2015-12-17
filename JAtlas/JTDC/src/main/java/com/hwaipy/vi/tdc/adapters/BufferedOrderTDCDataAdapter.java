package com.hwaipy.vi.tdc.adapters;

import com.hwaipy.vi.tdc.TDCDataAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Hwaipy
 */
public class BufferedOrderTDCDataAdapter implements TDCDataAdapter {

  private ArrayList<Long> buffer = new ArrayList<>(100000);
  private ArrayList<Long> doubleBuffer = new ArrayList<>(100000);
  private final int preservedEvent = 100;
  private long previousTime = 0;
  private int sortOuttedCount = 0;

  @Override
  public Object offer(Object data) {
    return order(data);
  }

  @Override
  public Object flush(Object data) {
    return order(data);
  }

  private Object order(Object data) {
    if (data != null) {
      if (data instanceof List) {
        buffer.addAll((List<? extends Long>) data);
      } else {
        throw new RuntimeException("Input data of BufferedOrderTDCDataAdapter should be List<Long>.");
      }
    }
    buffer.sort(null);
    int startPotision = 0;
    for (Long timeEvent : buffer) {
      if (timeEvent > previousTime) {
        break;
      }
      startPotision++;
    }
    List<Long> result;
    sortOuttedCount += startPotision;
    if (buffer.size() - startPotision > preservedEvent) {
      result = buffer.subList(startPotision, buffer.size() - preservedEvent);
      ArrayList<Long> mid = buffer;
      buffer = doubleBuffer;
      doubleBuffer = mid;
    } else {
      result = new ArrayList<>(buffer);
    }
    buffer.clear();
    if (result.size() > 0) {
      previousTime = result.get(result.size() - 1);
    }
    return result;
  }

  public int getSortOuttedCount() {
    return sortOuttedCount;
  }
}
