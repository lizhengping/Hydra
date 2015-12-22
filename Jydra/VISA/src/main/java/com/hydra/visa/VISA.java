package com.hydra.visa;

import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author Hwaipy
 */
public class VISA {

  private final Communicator communicator;

  private VISA(Communicator communicator) {
    this.communicator = communicator;
    communicator.start();
  }

  public void write(Command command) {
    communicator.sendLater(command.getBytes());
  }

  public void write(String head, boolean isQuery, Object... arguments) {
    communicator.sendLater(Command.create(head, isQuery, arguments).getBytes());
  }

  public Object query(Command command) throws InterruptedException {
    write(command);
    return read(2000);
  }

  public Object query(String head, Object... arguments) throws InterruptedException {
    write(head, true, arguments);
    return read(2000);
  }

  private Object read(long timeout) throws InterruptedException {
    byte[] data = communicator.read(timeout);
//    System.out.println(new String(data));
    if (data == null || data.length == 0) {
      return null;
    }
    if (data[0] == '"' && data[data.length - 1] == '"') {
      return new String(data, 1, data.length - 2);
    } else if (data[0] == '#') {
//      System.out.println("###");
      return null;
    } else {
      String d = new String(data);
      try {
        return Integer.parseInt(d);
      } catch (Exception e) {
      }
      try {
        return Float.parseFloat(d);
      } catch (Exception e) {
      }
//      throw new IllegalArgumentException();
      return null;
    }
  }

  public static void test(Runnable run) {
    run.run();
  }

  public static final VISA openSocketResource(String host, int port) throws IOException {
    Socket socket = new Socket(host, port);
    return new VISA(new Communicator(socket.getInputStream(), socket.getOutputStream()));
  }

//  public static void main(String[] args) {
//    System.out.println(ClassLoader.getSystemClassLoader().);
//  }
}
