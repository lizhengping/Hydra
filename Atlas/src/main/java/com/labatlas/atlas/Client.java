package com.labatlas.atlas;

import com.labatlas.atlas.services.Service;
import com.labatlas.atlas.services.ServiceManager;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mina.api.IoSession;

/**
 *
 * @author Hwaipy
 */
public class Client {

  public static final String KEY_IDENTITY_NAME = "Name";
  private final int id;
  private String name;
  private Map identity;
  private IoSession session;
  private ConcurrentHashMap<String, Service> services = new ConcurrentHashMap<>();
  private ConcurrentHashMap<WaitingRequestKey, Message> waitingRequests = new ConcurrentHashMap<>();

  private Client(int id) {
    this.id = id;
  }

  public void setSession(IoSession session) {
    this.session = session;
  }

  public void response(Message message) {
    session.write(message);
  }

  public void feedRequest(Message message) throws MessageFormatException {
    Target target = message.getTarget();
    if (target.isLocal()) {
      Command command = message.getCommand();
      if (command == null) {
        throw new MessageFormatException("Command \"" + message.getCommandString() + "\" not assinged.", message);
      } else {
        command.execute(message, this);
      }
    } else {
      if (initialed()) {
        Collection<Client> remoteClients = target.getRemoteClients();
        if (remoteClients.isEmpty()) {
          throw new MessageFormatException("Target not exists.", message);
        } else {
          for (Client remoteClient : remoteClients) {
            Message messageCopy = message.copy().put(Message.KEY_FROM, identity);
            remoteClient.dealRequest(messageCopy);
          }
        }
      } else {
        throw new MessageFormatException("Connection need to be initialed first using \"Connection\" command.", message);
      }
    }
  }

  public void feedResponse(Message message) throws MessageFormatException {
    long responseID = message.getID();
    Map toMap = message.get(Message.KEY_To, Map.class);
    Object clientIDO = toMap.get(Message.KEY_CLIENT_ID);
    if (clientIDO == null || !(clientIDO instanceof Integer)) {
      throw new MessageFormatException("The response destination need to specified correctly.", message);
    }
    int clientID = (int) clientIDO;
    WaitingRequestKey key = new WaitingRequestKey(clientID, responseID);
    Message associatedRequest = waitingRequests.get(key);
    if (associatedRequest == null) {
      throw new MessageFormatException("Request associated to this response not exists.", message);
    }
    if (!message.isContinues()) {
      waitingRequests.remove(key);
    }
    Client toClient = Client.getClient(clientID);
    if (toClient != null) {
      toClient.response(message);
    }
  }

  public void feed(Collection<Message> messages) {
    for (Message message : messages) {
      try {
        switch (message.getType()) {
          case REQUEST:
            feedRequest(message);
            break;
          case RESPONSE:
            feedResponse(message);
            break;
          default:
            throw new RuntimeException();
        }
      } catch (MessageFormatException ex) {
        session.write(message.responseError().put(Message.KEY_ERROR_MESSAGE, ex.getMessage()));
      }
    }
  }

  private void dealRequest(Message message) {
    long messageID = message.getID();
    int sourceID = (int) message.get(Message.KEY_FROM, Map.class).get(Message.KEY_CLIENT_ID);
    WaitingRequestKey key = new WaitingRequestKey(sourceID, messageID);
    if (waitingRequests.containsKey(key)) {
      throw new MessageFormatException("Request ID " + message.getID() + " Duplicate.", message);
    }
    waitingRequests.put(key, message);
    session.write(message);
  }

  public int getId() {
    return id;
  }

  private String getName() {
    return name;
  }

  boolean initialed() {
    return name != null;
  }

  void init(String name) {
    if (initialed()) {
      throw new RuntimeException("Client Already Initialed.");
    }
    this.name = name;
    HashMap<String, Object> map = new HashMap<>();
    map.put(KEY_IDENTITY_NAME, name);
    map.put(Message.KEY_CLIENT_ID, id);
    identity = Collections.unmodifiableMap(map);
    registerClient(this);
  }

  public void registerService(String serviceName) {
    Service service = ServiceManager.getDefault().registerService(serviceName, this);
    this.services.putIfAbsent(serviceName, service);
  }

  private static AtomicInteger CLIENT_IDS = new AtomicInteger(0);

  private static ConcurrentHashMap<Integer, Client> clientsByID = new ConcurrentHashMap<>();
  private static ConcurrentHashMap<String, Client> clientsByName = new ConcurrentHashMap<>();

  public static Client create() {
    Client client = new Client(CLIENT_IDS.getAndIncrement());
    return client;
  }

  private static void registerClient(Client client) {
    clientsByID.put(client.getId(), client);
    clientsByName.put(client.getName(), client);
  }

  public static Client getClient(String clientName) {
    return clientsByName.get(clientName);
  }

  public static Client getClient(int clientId) {
    return clientsByID.get(clientId);
  }

  private class WaitingRequestKey {

    private final int sourceID;
    private final long messageID;

    public WaitingRequestKey(int sourceID, long messageID) {
      this.sourceID = sourceID;
      this.messageID = messageID;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof WaitingRequestKey)) {
        return false;
      }
      WaitingRequestKey inst = (WaitingRequestKey) obj;
      return inst.sourceID == sourceID && inst.messageID == messageID;
    }

    @Override
    public int hashCode() {
      int hash = 5;
      hash = 43 * hash + this.sourceID;
      hash = 43 * hash + (int) (this.messageID ^ (this.messageID >>> 32));
      return hash;
    }
  }
}
