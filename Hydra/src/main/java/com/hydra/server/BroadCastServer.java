package com.hydra.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hwaipy
 */
public class BroadCastServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BroadCastServer.class);
  public static final String BROADCAST_REQUEST_CONNECTION = "Connection?";
  public static final String BROADCAST_RESPONSE_CONNECTION = "Connection";
  private final DatagramSocket server;
  private boolean started = false;

  private BroadCastServer(int port) throws SocketException {
    server = new DatagramSocket(port);
  }

  private void start() throws SocketException, IOException {
    synchronized (this) {
      if (started) {
        throw new IllegalStateException("Server already started.");
      }
      started = true;
    }
    server.setReuseAddress(true);
    byte[] receiveData = new byte[1024];
    while (true) {
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      server.receive(receivePacket);
      String message = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(), "UTF-8");
      String response = message(message);
      if (response != null) {
        InetAddress remoteAddress = receivePacket.getAddress();
        int remotePort = receivePacket.getPort();
        byte[] sendData = response.getBytes("UTF-8");
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, remoteAddress, remotePort);
        server.send(sendPacket);
      }
    }
  }

  private String message(String message) {
    switch (message) {
      case BROADCAST_REQUEST_CONNECTION:
        return BROADCAST_RESPONSE_CONNECTION;
    }
    return null;
  }

  public static void start(final int port) {
    Thread broadcastThread = new Thread(new Runnable() {

      @Override
      public void run() {
        while (true) {
          try {
            broadCast();
          } catch (Exception e) {
            System.out.println(e);
          }
        }
      }
      private int loop = 0;

      private void broadCast() throws Exception {
        if (loop != 0) {
          Thread.sleep(1000);
        }
        loop++;
        new BroadCastServer(port).start();
      }
    });
    broadcastThread.setName("BroadCastThread");
    broadcastThread.setDaemon(true);
    broadcastThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

      @Override
      public void uncaughtException(Thread t, Throwable e) {
        LOGGER.warn(t.getName(), e);
      }
    });
    broadcastThread.start();
  }
}
