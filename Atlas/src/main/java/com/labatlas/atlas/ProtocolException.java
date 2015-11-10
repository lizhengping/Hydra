package com.labatlas.atlas;

/**
 *
 * @author Hwaipy
 */
public class ProtocolException extends RuntimeException {

  private final Message message;

  public ProtocolException(Message message) {
    this.message = message;
  }

  public ProtocolException(String discription, Message message) {
    super(discription);
    this.message = message;
  }

  public ProtocolException(String discription, Throwable cause, Message message) {
    super(discription, cause);
    this.message = message;
  }

  public Message getMessageObject() {
    return message;
  }
}
