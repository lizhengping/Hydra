package com.labatlas.atlas;

import com.labatlas.atlas.message.Message;
import com.labatlas.atlas.message.MessageFormatException;
import com.labatlas.atlas.services.Service;
import com.labatlas.atlas.services.ServiceManager;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
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
  private ConcurrentHashMap<String, Service> services = new ConcurrentHashMap<>();

  private Client(int id) {
    this.id = id;
  }

  public void setSession(IoSession session) {
    this.session = session;
  }

  public void response(Message message) {
    message.put(Message.KEY_CLIENT_ID, id);
    session.write(message);
  }

  public void feed(Message message) {
    try {
      Command command = message.getCommand();
      if (command == null) {
        throw new MessageFormatException("Command \"" + message.getCommandString() + "\" not assinged.", message);
      } else {
        command.execute(message, this);
      }
    } catch (MessageFormatException ex) {
      session.write(message.responseError().put(Message.KEY_ERROR_MESSAGE, ex.getMessage()));
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

  boolean initialed() {
    return name != null;
  }

  void init(String name) {
    if (initialed()) {
      throw new RuntimeException("Client Already Initialed.");
    }
    this.name = name;
  }

  public void registerService(String serviceName) {
    Service service = ServiceManager.getDefault().registerService(serviceName, this);
    this.services.putIfAbsent(serviceName, service);
  }

  private static AtomicInteger CLIENT_IDS = new AtomicInteger(0);

  public static Client create() {
    return new Client(CLIENT_IDS.getAndIncrement());
  }

}
