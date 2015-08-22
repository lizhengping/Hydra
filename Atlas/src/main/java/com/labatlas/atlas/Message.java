package com.labatlas.atlas;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Hwaipy
 */
@org.msgpack.annotation.Message
public class Message {

  private Map attributes = new HashMap();

  public Message() {
    attributes.put("name", "new message");
    attributes.put("index", 123);
    attributes.put("send", true);
    attributes.put("content", new int[]{1, 2, 3, 45, -100, 2121312421});
  }

}
