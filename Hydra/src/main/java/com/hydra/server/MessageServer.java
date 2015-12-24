package com.hydra.server;

import java.net.InetSocketAddress;
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
      server.setIoHandler(new MessageServerHandler());
      server.setReuseAddress(true);
      server.bind(new InetSocketAddress(port));
    }
  }
}
