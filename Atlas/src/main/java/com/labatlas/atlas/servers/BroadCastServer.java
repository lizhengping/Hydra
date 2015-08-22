package com.labatlas.atlas.servers;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.mina.api.AbstractIoHandler;
import org.apache.mina.api.IoSession;
import org.apache.mina.transport.nio.NioUdpServer;

/**
 *
 * @author Hwaipy
 */
public class BroadCastServer {

  public BroadCastServer() {
    NioUdpServer server = new NioUdpServer();
    server.setIoHandler(new AbstractIoHandler() {

      @Override
      public void sessionOpened(IoSession session) {
        System.out.println("New session");
      }

      @Override
      public void messageReceived(IoSession session, Object message) {
        System.out.println("Me:" + message);
      }

    });
    final SocketAddress address = new InetSocketAddress(9999);
    server.bind(address);
    try {
      Thread.sleep(10000);
    } catch (InterruptedException ex) {
      Logger.getLogger(BroadCastServer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
