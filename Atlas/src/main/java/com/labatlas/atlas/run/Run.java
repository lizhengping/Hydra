package com.labatlas.atlas.run;

import com.labatlas.atlas.SummaryManager;
import com.labatlas.atlas.server.MessageServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Hwaipy
 */
public class Run {

  public static void main(String[] args) {
    Properties properties = new Properties();
    File propertiesFile = new File("Atlas.properties");
    try (FileInputStream propertiesIn = new FileInputStream(propertiesFile)) {
      properties.load(propertiesIn);
    } catch (FileNotFoundException ex) {
      System.out.println("Properties file not found.");
      System.exit(11);
    } catch (IOException ex) {
      System.out.println("Exception in reading properties file.");
      System.exit(12);
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
      try {
        Thread.sleep(1000l * 3600l * 24l * 365l * 10l);
      } catch (InterruptedException ex) {
        System.out.println("Process interrupted.");
        System.exit(13);
      }
    } else {
      try {
        while (System.in.read() == exitCode.charAt(0)) {
          System.exit(0);
        }
      } catch (IOException ex) {
        System.out.println("IOException in attempt to read System.in.");
        System.exit(14);
      }
    }
  }
}
