package com.labatlas.atlas.tests;

import com.labatlas.atlas.server.BroadCastServer;
import com.labatlas.atlas.server.MessageServer;
import java.io.IOException;

/**
 *
 * @author Hwaipy
 */
public class TEST {

  public static void main(String[] args) throws InterruptedException, IOException {
    int messagePort = 50001;
    int broadcastPort = 50051;

    new MessageServer(messagePort).start();
    BroadCastServer.start(broadcastPort);

    Thread.sleep(1000000);
    System.exit(0);
  }
}
