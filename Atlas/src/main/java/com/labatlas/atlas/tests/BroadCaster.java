package com.labatlas.atlas.tests;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author Hwaipy
 */
public class BroadCaster {

  public static void main(String[] args) throws InterruptedException, SocketException, UnknownHostException {
    InetAddress broadAddress = InetAddress.getByName("192.168.1.255");
    DatagramSocket sender = new DatagramSocket();
    while (true) {
      Thread.sleep(1000);
      DatagramPacket packet; // 数据包，相当于集装箱，封装信息  
      try {
        byte[] b = "BroadCast Test".getBytes();
        packet = new DatagramPacket(b, b.length, broadAddress, 9999); // 广播信息到指定端口  
        sender.send(packet);
        System.out.println("*****已发送请求*****");
      } catch (Exception e) {
        System.out.println("*****查找出错*****");
      }
    }
  }
}
