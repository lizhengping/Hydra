package com.labatlas.atlas.tests;

import com.labatlas.atlas.Message;
import java.io.IOException;
import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;

/**
 *
 * @author Hwaipy
 */
public class TEST {

  public static void main(String[] args) throws InterruptedException, IOException {
//    new BroadCastServer();
//    MessageServer server = new MessageServer(9997);
//    server.start();
//    MessagePackSender.main(args);
//    Thread.sleep(10000);
//    System.exit(0);
    ////
    MessagePack messagePack = new MessagePack();
//
    BufferPacker packer = messagePack.createBufferPacker();
    packer.write(new Message().attributes);
    byte[] bs = packer.toByteArray();
    for (byte b : bs) {
      System.out.print((int) b + ", ");
    }
    System.out.println();
//    int position = 1;
//    ByteBuffer buffer1 = ByteBuffer.wrap(Arrays.copyOfRange(bs, 0, position));
//    BufferUnpacker unpacker = messagePack.createBufferUnpacker();
//    UnpackerIterator iterator = unpacker.iterator();
//    unpacker.feed(buffer1);
//    System.out.println(iterator.hasNext());
//
//    ByteBuffer buffer2 = ByteBuffer.wrap(Arrays.copyOfRange(bs, position, bs.length));
//    unpacker.feed(buffer2);
//    System.out.println(iterator.hasNext());
  }
}
