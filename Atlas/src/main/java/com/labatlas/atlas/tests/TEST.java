package com.labatlas.atlas.tests;

import com.labatlas.atlas.server.MessageServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Hwaipy
 */
public class TEST {

  public static void main(String[] args) throws InterruptedException, IOException {
    Properties properties = new Properties();
    File propertiesFile = new File("Atlas.properties");
    try (FileInputStream propertiesIn = new FileInputStream(propertiesFile)) {
      properties.load(propertiesIn);
      propertiesIn.close();
    }
    System.setProperty("log4j.configurationFile",
            properties.getProperty("log4j.configurationFile", "./config/log4j.xml"));

    int messagePort = 20001;
    new MessageServer(messagePort).start();

//    while (System.in.read() == 'q') {
//      System.exit(0);
//    }
    Thread.sleep(1000 * 3600 * 365);
  }
}
