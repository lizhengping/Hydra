package com.hwaipy.jatlas.visa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hwaipy
 */
public class Command {

  private final String head;
  private final boolean isQuery;
  private final Argument[] arguments;

  public Command(String head, boolean isQuery, Argument... arguments) {
    this.head = head;
    this.isQuery = isQuery;
    this.arguments = arguments;
  }

  public byte[] getBytes() {
    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    try {
      bo.write(head.getBytes());
      if (isQuery) {
        bo.write('?');
      }
      if (arguments != null && arguments.length > 0) {
        bo.write(' ');
        bo.write(arguments[0].getData());
      }
      for (int i = 1; i < arguments.length; i++) {
        bo.write(',');
        bo.write(arguments[i].getData());
      }
      bo.write('\r');
      bo.write('\n');
    } catch (IOException ex) {
      Logger.getLogger(Command.class.getName()).log(Level.SEVERE, null, ex);
    }
    return bo.toByteArray();
  }

  public static Command create(String head, boolean isQuery, Object[] args) {
    Argument[] arguments = new Argument[(args == null ? 0 : args.length)];
    for (int i = 0; i < arguments.length; i++) {
      Object argO = args[i];
      if (argO instanceof String) {
        arguments[i] = new Argument.StringArgument((String) argO);
      } else if (argO instanceof Integer) {
        arguments[i] = new Argument.IntegerArgument((int) argO);
      } else if (argO instanceof Long) {
        arguments[i] = new Argument.LongArgument((long) argO);
      } else if (argO instanceof float[]) {
        arguments[i] = new Argument.BlockArgument((float[]) argO);
      } else {
        throw new IllegalArgumentException("" + argO.getClass());
      }
    }
    return new Command(head, isQuery, arguments);
  }
}
