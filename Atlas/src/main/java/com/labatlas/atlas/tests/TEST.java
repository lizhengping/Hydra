package com.labatlas.atlas.tests;

import java.io.IOError;
import java.io.IOException;
<<<<<<< HEAD
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
=======
import javax.swing.JFrame;
>>>>>>> define_basic_protocol_in_LHF

/**
 *
 * @author Hwaipy
 */
public class TEST {

  public static void main(String[] args) throws InterruptedException, IOException {
<<<<<<< HEAD
    System.setProperty("log4j.configurationFile", "./config/log4j.xml");
//    int messagePort = 20001;
//    int broadcastPort = 20051;
//
//    new MessageServer(messagePort).start();
//    BroadCastServer.start(broadcastPort);
//
//    Thread.sleep(1000000);
//    System.exit(0);
    Logger logger = LoggerFactory.getLogger(TEST.class);
    while (true) {
      logger.error("this is a message");
      logger.debug("a bug dispared.!!!\n\noh no !!\n\n", new RuntimeException(new IOError(new IOException("no"))));
      logger.info("this a infomation is.");
      logger.warn("not good");
      logger.trace("steps", 1, 2, 3, 4, 5, "1231323", "wfawefef\newa\n\t,fe");
      Thread.sleep(1000);
    }
=======
    int messagePort = 20101;
    int broadcastPort = 20151;

    new MessageServer(messagePort).start();
    BroadCastServer.start(broadcastPort);

    JFrame jFrame = new JFrame("LabAtlas Server");
    jFrame.setVisible(true);
>>>>>>> define_basic_protocol_in_LHF
  }
}
