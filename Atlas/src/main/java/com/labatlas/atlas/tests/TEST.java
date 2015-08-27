package com.labatlas.atlas.tests;

import com.labatlas.atlas.servers.MessageServer;
import java.io.IOException;

/**
 *
 * @author Hwaipy
 */
public class TEST {

  public static void main(String[] args) throws InterruptedException, IOException {
    MessageServer server = new MessageServer(9997);
    server.start();
    Thread.sleep(10000);
    System.exit(0);
  }
}
