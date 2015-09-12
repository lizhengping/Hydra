package com.labatlas.atlas.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.labatlas.atlas.Command;
import com.labatlas.atlas.Commands;
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
  public static final String KEY_ID = "ID";
  public static final String KEY_NAME = "Name";
  public static final String KEY_CLIENT_ID = "ClientID";
  public static final String KEY_ERROR = "Error";
  public static final String KEY_ERROR_MESSAGE = "ErrorMessage";
  public static final String KEY_STATUS = "Status";
  public static final String VALUE_STATUS_OK = "Ok";
  public static final String COMMAND_CONNECTION = "Connection";
  private final HashMap attributes = new HashMap();
  private Type type;
  private String commandString = "Unknown";
  private Command command;
  private long id = -1;

  public Message() {
  }

  public Type getType() {
    return type;
  }

  public String getCommandString() {
    return commandString;
  }

  public Command getCommand() {
    return command;
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
          throw new MessageFormatException("The value of key \"" + key + "\" can not cast to " + clazz.getSimpleName(), this);
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
    return new Message().put(KEY_RESPONSE, command).put(KEY_ID, id);
  }

  public Message responseError() {
    return new Message().put(KEY_ERROR, command).put(KEY_ID, id);
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

  private void extractEssentialFields() throws IllegalArgumentException {
    Object requestCommandO = attributes.get(KEY_REQUEST);
    Object responseCommandO = attributes.get(KEY_RESPONSE);
    Object IDO = attributes.get(KEY_ID);
    Object typeCommand;
    if (requestCommandO == null) {
      if (responseCommandO == null) {
        throw new MessageFormatException("Either Request or Response should be assigned.", this);
      } else {
        typeCommand = responseCommandO;
        type = Type.RESPONSE;
      }
    } else {
      if (responseCommandO == null) {
        typeCommand = requestCommandO;
        type = Type.REQUEST;
      } else {
        throw new MessageFormatException("Message should only be either Request or Response.", this);
      }
    }
    if (typeCommand instanceof String) {
      commandString = (String) typeCommand;
      command = Commands.getCommand(commandString);
    } else {
      throw new MessageFormatException("Command should be String.", this);
    }
    if (IDO == null) {
      throw new MessageFormatException("ID should be assigned.", this);
    } else {
      if (IDO instanceof Integer) {
        id = (int) IDO;
      } else if (IDO instanceof Long) {
        id = (long) IDO;
      } else {
        throw new MessageFormatException("Command should be Integer.", this);
      }
    }
  }

  public static Message newMessage(Map contents) {
    Message message = new Message();
    message.attributes.putAll(contents);
    message.extractEssentialFields();
    return message;
  }

  public enum Type {

    REQUEST, RESPONSE, ERROR
  }
}
