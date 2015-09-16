package com.labatlas.atlas.tests;

import com.labatlas.atlas.server.BroadCastServer;
import com.labatlas.atlas.server.MessageServer;
import java.io.IOException;
import javax.swing.JFrame;

/**
 *
 * @author Hwaipy
 */
public class TEST {

  public static void main(String[] args) throws InterruptedException, IOException {
    int messagePort = 20101;
    int broadcastPort = 20151;

    new MessageServer(messagePort).start();
    BroadCastServer.start(broadcastPort);

    JFrame jFrame = new JFrame("LabAtlas Server");
    jFrame.setVisible(true);
  }

}
