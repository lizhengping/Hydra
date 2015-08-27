package com.labatlas.atlas.message;

/**
 *
 * @author Hwaipy
 */
public class MessageFormatException extends RuntimeException {

  private final Message message;

  public MessageFormatException(Message message) {
    this.message = message;
  }

  public MessageFormatException(String discription, Message message) {
    super(discription);
    this.message = message;
  }

  public MessageFormatException(String discription, Throwable cause, Message message) {
    super(discription, cause);
    this.message = message;
  }

}
