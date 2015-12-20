package com.labatlas.atlas;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ImmutableValue;

/**
 *
 * @author Hwaipy
 */
public class MessageGenerator {

  private final ByteBuffer buffer;
  private final MessagePack messagePack = new MessagePack();

  public MessageGenerator() {
    this(1000000);
  }

  public MessageGenerator(int bufferSize) {
    buffer = ByteBuffer.wrap(new byte[bufferSize]);
    buffer.limit(0);
  }

  public void feed(ByteBuffer feed) {
    int currentPosition = buffer.position();
    if (feed.remaining() > (buffer.capacity() - buffer.limit())) {
      buffer.compact();
      currentPosition = 0;
    } else {
      buffer.position(buffer.limit());
      buffer.limit(buffer.capacity());
    }
    int feedLimit = feed.limit();
    if (feed.remaining() > buffer.remaining()) {
      feed.limit(feed.position() + buffer.remaining());
    }
    buffer.put(feed);
    feed.limit(feedLimit);
    buffer.limit(buffer.position());
    buffer.position(currentPosition);
  }

  private Message tryLoadNextValue() {
    MessageUnpacker unpacker = messagePack.newUnpacker(buffer.array(), buffer.position(), buffer.limit() - buffer.position());
    try {
      ImmutableValue value = unpacker.unpackValue();
      long unpackCursor = unpacker.getTotalReadBytes();
      Message nextMessage = new MessageConvertor(value).convert();
      buffer.position((int) (buffer.position() + unpackCursor));
      return nextMessage;
    } catch (EOFException e) {
      return null;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Message next() {
    return tryLoadNextValue();
  }
}
