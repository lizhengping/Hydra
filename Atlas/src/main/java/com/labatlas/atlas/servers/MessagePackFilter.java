package com.labatlas.atlas.servers;

import java.nio.ByteBuffer;
import org.apache.mina.api.IdleStatus;
import org.apache.mina.api.IoFilter;
import org.apache.mina.api.IoSession;
import org.apache.mina.filterchain.ReadFilterChainController;
import org.apache.mina.filterchain.WriteFilterChainController;
import org.apache.mina.session.AttributeKey;
import org.apache.mina.session.WriteRequest;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;
import org.msgpack.unpacker.BufferUnpacker;
import org.msgpack.unpacker.Converter;
import org.msgpack.unpacker.UnpackerIterator;

/**
 *
 * @author Hwaipy
 */
public class MessagePackFilter implements IoFilter {

  private static final AttributeKey BUFFER_UNPACKER_KEY = new AttributeKey(BufferUnpacker.class, "BufferUnpacker");
  private static final AttributeKey UNPACKER_ITERATOR_KEY = new AttributeKey(UnpackerIterator.class, "BufferUnpackerIterator");
  private final MessagePack messagePack;

  public MessagePackFilter() {
    messagePack = new MessagePack();
  }

  @Override
  public void sessionOpened(IoSession is) {
    BufferUnpacker bufferUnpacker = messagePack.createBufferUnpacker();
    is.setAttribute(BUFFER_UNPACKER_KEY, bufferUnpacker);
    UnpackerIterator iterator = bufferUnpacker.iterator();
    is.setAttribute(UNPACKER_ITERATOR_KEY, iterator);
  }

  @Override
  public void sessionClosed(IoSession is) {
  }

  @Override
  public void sessionIdle(IoSession is, IdleStatus is1) {
  }

  @Override
  public void messageReceived(IoSession is, Object o, ReadFilterChainController rfcc) {
    try {
      if (o instanceof ByteBuffer) {
        ByteBuffer buffer = (ByteBuffer) o;
        BufferUnpacker bufferUnpacker = (BufferUnpacker) is.getAttribute(BUFFER_UNPACKER_KEY);
        UnpackerIterator iterator = (UnpackerIterator) is.getAttribute(UNPACKER_ITERATOR_KEY);
        bufferUnpacker.feed(buffer);
        while (iterator.hasNext()) {
          Value value = iterator.next();
          System.out.println(value);
        }
      } else {
        rfcc.callReadNextFilter(o);
      }
    } catch (Exception e) {
      e.printStackTrace();
      is.close(true);
    }
  }

  @Override
  public void messageWriting(IoSession is, WriteRequest wr, WriteFilterChainController wfcc) {
  }

  @Override
  public void messageSent(IoSession is, Object o) {
  }

}
