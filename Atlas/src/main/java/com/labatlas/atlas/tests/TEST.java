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
    String messagePortS = properties.getProperty("messageserver.port", "20102");
    int messagePort;
    try {
      messagePort = Integer.parseInt(messagePortS);
    } catch (Exception e) {
      messagePort = 20102;
    }
    new MessageServer(messagePort).start();

    String exitCode = properties.getProperty("exit");
    if (exitCode == null || exitCode.isEmpty()) {
      Thread.sleep(1000 * 3600 * 24);
    } else {
      while (System.in.read() == exitCode.charAt(0)) {
        System.exit(0);
      }
    }
  }
}
