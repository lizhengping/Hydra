package com.labatlas.atlas.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.msgpack.jackson.dataformat.MessagePackFactory;

/**
 *
 * @author Hwaipy
 */
public class Message {

  public static final String KEY_REQUEST = "Request";
  public static final String KEY_RESPONSE = "Response";
  public static final String KEY_NAME = "Name";
  public static final String KEY_SERVICE_REGISTER = "ServiceRegister";
  private final HashMap attributes = new HashMap();

  public Message() {
  }

  public Message put(String key, Object value) {
    attributes.put(key, value);
    return this;
  }

  public Object get(String key) {
    return attributes.get(key);
  }

  public Object getOrDefault(String key, Object defaultValue) {
    return attributes.getOrDefault(key, defaultValue);
  }

  public <T> T get(String key, Class<T> clazz) throws MessageFormatException {
    return get(key, clazz, false);
  }

  public <T> T get(String key, Class<T> clazz, boolean nullValid) throws MessageFormatException {
    if (attributes.containsKey(key)) {
      Object value = attributes.get(key);
      if (value == null) {
        if (nullValid) {
          return null;
        } else {
          throw new MessageFormatException("Null value invalid with key \"" + key + "\".", this);
        }
      } else {
        if (clazz.isInstance(value)) {
          return (T) value;
        } else {
          throw new MessageFormatException("The value of key \"" + key + "\" can not cast to " + clazz, this);
        }
      }
    } else {
      throw new MessageFormatException("Message does not contains key \"" + key + "\".", this);
    }
  }

  public <T> List<T> getAsList(String key, Class<T> clazz) throws MessageFormatException {
    if (attributes.containsKey(key)) {
      Object value = attributes.get(key);
      ArrayList<T> list = new ArrayList<>(5);
      if (value != null) {
        if (clazz.isInstance(value)) {
          list.add((T) value);
        } else if (value instanceof List) {
          list.addAll((List) value);
        } else {
          throw new MessageFormatException("The value of key \"" + key + "\" can not cast to " + clazz, this);
        }
      }
      return list;
    } else {
      throw new MessageFormatException("Message does not contains key \"" + key + "\".", this);
    }
  }

  public boolean contains(String key) {
    return attributes.containsKey(key);
  }

  public Message response() {
    return new Message().put(KEY_RESPONSE, this.get(KEY_REQUEST, Integer.class));
  }

  /**
   * Slow implementation.
   *
   * @return
   */
  public byte[] pack() {
    ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
    try {
      return objectMapper.writeValueAsBytes(attributes);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException(ex);
    }
  }

  @Override
  public String toString() {
    return "Message " + attributes.toString();
  }

  public static Message newMessage(Map contents) {
    Message message = new Message();
    message.attributes.putAll(contents);
    return message;
  }
}
