package com.labatlas.atlas.server;

import com.labatlas.atlas.Message;
import com.labatlas.atlas.MessageGenerator;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import org.apache.mina.api.IdleStatus;
import org.apache.mina.api.IoFilter;
import org.apache.mina.api.IoSession;
import org.apache.mina.filterchain.ReadFilterChainController;
import org.apache.mina.filterchain.WriteFilterChainController;
import org.apache.mina.session.AttributeKey;
import org.apache.mina.session.WriteRequest;

/**
 *
 * @author Hwaipy
 */
public class MessagePackFilter implements IoFilter {

  private static final AttributeKey MESSAGE_GENERATOR = new AttributeKey(MessageGenerator.class, "MessageGenerator");

  public MessagePackFilter() {
  }

  @Override
  public void sessionOpened(IoSession is) {
    MessageGenerator messageGenerator = new MessageGenerator();
    is.setAttribute(MESSAGE_GENERATOR, messageGenerator);
  }

  @Override
  public void sessionClosed(IoSession is) {
  }

  @Override
  public void sessionIdle(IoSession is, IdleStatus is1) {
  }

  @Override
  public void messageReceived(IoSession is, Object o, ReadFilterChainController rfcc) {
    if (o instanceof ByteBuffer) {
      ByteBuffer buffer = (ByteBuffer) o;
      MessageGenerator generator = (MessageGenerator) is.getAttribute(MESSAGE_GENERATOR);
      LinkedList<Message> messageList = new LinkedList<>();
      while (buffer.hasRemaining()) {
        int remaining = buffer.remaining();
        generator.feed(buffer);
        if (remaining == buffer.remaining()) {
          throw new IllegalArgumentException("Message too long.");
        }
        while (true) {
          Message next = generator.next();
          if (next == null) {
            break;
          }
          messageList.add(next);
        }
      }
      if (!messageList.isEmpty()) {
        rfcc.callReadNextFilter(messageList);
      }
    } else {
      rfcc.callReadNextFilter(o);
    }
  }

  @Override
  public void messageWriting(IoSession is, WriteRequest wr, WriteFilterChainController wfcc) {
    Object messageObject = wr.getMessage();
    if (messageObject instanceof Message) {
      wr.setMessage(ByteBuffer.wrap(((Message) messageObject).pack()));
    }
    wfcc.callWriteNextFilter(wr);
  }

  @Override
  public void messageSent(IoSession is, Object o) {
  }
}
