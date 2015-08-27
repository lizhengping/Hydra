package com.labatlas.atlas;

import com.labatlas.atlas.message.Message;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mina.api.IoSession;

/**
 *
 * @author Hwaipy
 */
public class Client {

  private final int id;
  private String name = null;
  private IoSession session;

  private Client(int id) {
    this.id = id;
  }

  public void setSession(IoSession session) {
    this.session = session;
  }

  public void feed(Message message) {
    if (name == null) {
      message0(message);
    } else {
      if (message.contains(Message.KEY_SERVICE_REGISTER)) {
        List<String> list = message.getAsList(Message.KEY_SERVICE_REGISTER, String.class);
        System.out.println(list);
      }
    }
  }

  public void feed(Collection<Message> messages) {
    for (Message message : messages) {
      feed(message);
    }
  }

  public int getId() {
    return id;
  }

  private void message0(Message message) {
    int requestId = message.get(Message.KEY_REQUEST, Integer.class);
    name = message.get(Message.KEY_NAME, String.class);
    Message response = message.response();
    session.write(response);
  }

  private static AtomicInteger CLIENT_IDS = new AtomicInteger(0);

  public static Client create() {
    return new Client(CLIENT_IDS.getAndIncrement());
  }

}
