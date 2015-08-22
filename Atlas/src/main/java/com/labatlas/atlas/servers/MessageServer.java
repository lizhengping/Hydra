package com.labatlas.atlas.servers;

import java.net.InetSocketAddress;
import org.apache.mina.api.AbstractIoHandler;
import org.apache.mina.api.IoSession;
import org.apache.mina.transport.nio.NioTcpServer;

/**
 *
 * @author Hwaipy
 */
public class MessageServer {

  private final int port;
  private final NioTcpServer server;
  private boolean started = false;

  public MessageServer(int port) {
    this.port = port;
    server = new NioTcpServer();
  }

  public void start() {
    synchronized (this) {
      if (started) {
        throw new IllegalStateException("Server already started.");
      }
      started = true;
      server.setFilters(new MessagePackFilter());
      server.setIoHandler(new AbstractIoHandler() {

        @Override
        public void sessionOpened(IoSession session) {
          System.out.println("New session");
        }

        @Override
        public void messageReceived(IoSession session, Object message) {
          System.out.println("Message:" + message);
        }

      });
      server.setReuseAddress(true);
      server.bind(new InetSocketAddress(port));
    }
  }
}
