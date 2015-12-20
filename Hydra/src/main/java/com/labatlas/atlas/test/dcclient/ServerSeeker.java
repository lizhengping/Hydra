package com.labatlas.atlas.test.dcclient;

import com.labatlas.atlas.server.BroadCastServer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 *
 * @author Hwaipy
 */
public class ServerSeeker {

  private final DatagramSocket udpSocket;
  private final SocketAddress broadcastAddress;

  public ServerSeeker(SocketAddress broadcastAddress) throws SocketException {
    this.broadcastAddress = broadcastAddress;
    this.udpSocket = new DatagramSocket();
  }

  public InetAddress seek() throws IOException, InterruptedException {
    byte[] receiveData = new byte[1024];
    String request = BroadCastServer.BROADCAST_REQUEST_CONNECTION;
    while (true) {
      byte[] sendData = request.getBytes("UTF-8");
      DatagramPacket sendPacket = new DatagramPacket(sendData, 0, sendData.length, broadcastAddress);
      udpSocket.send(sendPacket);
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      udpSocket.receive(receivePacket);
      String response = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(), "UTF-8");
      if (BroadCastServer.BROADCAST_RESPONSE_CONNECTION.endsWith(response)) {
        return receivePacket.getAddress();
      }
      Thread.sleep(300);
    }
  }
}
