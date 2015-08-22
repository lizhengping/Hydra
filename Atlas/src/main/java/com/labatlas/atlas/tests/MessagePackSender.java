package com.labatlas.atlas.tests;

import com.labatlas.atlas.Message;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;

/**
 *
 * @author Hwaipy
 */
public class MessagePackSender {

  public static void main(String[] args) {
    MessagePack messagePack = new MessagePack();
    while (true) {
      try {
        Thread.sleep(3000);
      } catch (InterruptedException ex) {
        Logger.getLogger(MessagePackSender.class.getName()).log(Level.SEVERE, null, ex);
      }
      try {
        final Socket socket = new Socket("localhost", 9997);
        new Thread(new Runnable() {

          @Override
          public void run() {
            try {
              OutputStream outputStream = socket.getOutputStream();
              MessagePack messagePack = new MessagePack();
              Packer packer = messagePack.createPacker(outputStream);
              while (true) {
                Thread.sleep(1000);
                packer.write(new Message());
                packer.write("1341fr");
              }
            } catch (Exception ex) {
              Logger.getLogger(MessagePackSender.class.getName()).log(Level.SEVERE, null, ex);
            }
          }
        }).start();
      } catch (IOException ex) {
        Logger.getLogger(MessagePackSender.class.getName()).log(Level.SEVERE, null, ex);
      }
      return;
    }
  }

}
