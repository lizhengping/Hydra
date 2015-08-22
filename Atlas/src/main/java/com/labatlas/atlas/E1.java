package com.labatlas.atlas;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import org.apache.mina.api.AbstractIoHandler;
import org.apache.mina.api.IoSession;
import org.apache.mina.examples.echoserver.NioEchoServer;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.nio.NioTcpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hwaipy 2015-7-1
 */
public class E1 {

  static final private Logger LOG = LoggerFactory.getLogger(NioEchoServer.class);

  public static void main(final String[] args) {
    LOG.info("starting echo server");
      final NioTcpServer acceptor = new NioTcpServer();
    // create the filter chain for this service
    acceptor.setFilters(new LoggingFilter("LoggingFilter1"));
    acceptor.setIoHandler(new AbstractIoHandler() {
      @Override
      public void sessionOpened(final IoSession session) {
        LOG.info("session opened {}", session);

        final String welcomeStr = "welcome\n";
        final ByteBuffer bf = ByteBuffer.allocate(welcomeStr.length());
        bf.put(welcomeStr.getBytes());
        bf.flip();
        session.write(bf);
      }

      @Override
      public void messageReceived(IoSession session, Object message) {
        if (message instanceof ByteBuffer) {
          LOG.info("echoing");
          session.write(message);
        }
      }

    });
    try {
      final SocketAddress address = new InetSocketAddress(9999);
      acceptor.bind(address);
      LOG.debug("Running the server for 25 sec");
      Thread.sleep(25000);
      LOG.debug("Unbinding the TCP port");
      acceptor.unbind();
    } catch (final InterruptedException e) {
      LOG.error("Interrupted exception", e);
    }
  }

}
