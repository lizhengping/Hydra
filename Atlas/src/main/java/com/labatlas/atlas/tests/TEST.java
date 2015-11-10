package com.labatlas.atlas.tests;

import com.labatlas.atlas.server.MessageServer;
import java.io.IOException;

/**
 *
 * @author Hwaipy
 */
public class TEST {

  public static void main(String[] args) throws InterruptedException, IOException {
    System.setProperty("log4j.configurationFile", "./config/log4j.xml");
    int messagePort = 20001;
    int broadcastPort = 20051;

    new MessageServer(messagePort).start();
//    BroadCastServer.start(broadcastPort);

    while (System.in.read() == 'q') {
      System.exit(0);
    }
  }
}
