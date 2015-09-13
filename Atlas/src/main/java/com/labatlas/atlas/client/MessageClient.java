package com.labatlas.atlas.client;

import com.labatlas.atlas.server.*;
import java.net.SocketAddress;
import org.apache.mina.transport.nio.NioTcpClient;

/**
 *
 * @author Hwaipy
 */
public class MessageClient {

  private final NioTcpClient client;
  private boolean started = false;
  private final SocketAddress address;

  public MessageClient(SocketAddress address) {
    client = new NioTcpClient();
    this.address = address;
  }

  public void start() {
    synchronized (this) {
      if (started) {
        throw new IllegalStateException("Server already started.");
      }
      started = true;
      client.setFilters(new MessagePackFilter());
      client.setIoHandler(new MessageClientHandler());
      client.connect(address);
    }
  }
}
