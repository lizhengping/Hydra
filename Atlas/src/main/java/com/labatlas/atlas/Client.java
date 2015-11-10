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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hwaipy
 */
public class Client {

  private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
  public static final String KEY_IDENTITY_NAME = "Name";
  private final int id;
  private String name;
  private Map identity;
  private IoSession session;
  private ConcurrentHashMap<String, Service> services = new ConcurrentHashMap<>();
//  private ConcurrentHashMap<WaitingRequestKey, Message> waitingRequests = new ConcurrentHashMap<>();

  private Client(int id, IoSession session) {
    this.id = id;
    this.session = session;
  }

  public void write(Message message) {
    LOGGER.debug("A message is about to sent to Client[{}, {}]: {}", getId(), getName(), message);
    session.write(message);
  }

  public void feed(Collection<Message> messages) {
    LOGGER.debug("{} messages received in Client[{}，{}].", messages.size(), getId(), getName());
    for (Message message : messages) {
      LOGGER.debug("Client[{}, {}] deals with message: {}", getId(), getName(), message);
      if (message.getTarget().isLocal()) {
        feedMessageToLocal(message);
      } else {
        throw new UnsupportedOperationException();
      }
    }
  }

  private void feedMessageToLocal(Message message) throws ProtocolException {
    switch (message.getType()) {
      case REQUEST:
        Command command = message.getCommand();
        if (command == null) {
          throw new ProtocolException("Command \"" + message.getCommandString() + "\" not assinged.", message);
        } else {
          LOGGER.debug("Command [{}] is found for request in Client[{}, {}]: {}", command, getId(), getName(), message);
          command.execute(message, this);
        }
        break;
      case RESPONSE:
        LOGGER.warn("The type of message is not acceptable in Client[{}, {}]: {}", getId(), getName(), message);
        break;
      default:
        LOGGER.warn("The type of message is not acceptable in Client[{}, {}]: {}", getId(), getName(), message);
        break;
    }
//      if (initialed()) {
//        Collection<Client> remoteClients = target.getRemoteClients();
//        if (remoteClients.isEmpty()) {
//          throw new ProtocolException("Target not exists.", message);
//        } else {
//          for (Client remoteClient : remoteClients) {
//            Message messageCopy = message.copy().put(Message.KEY_FROM, identity);
//            remoteClient.dealRequest(messageCopy);
//          }
//        }
//      } else {
//        throw new ProtocolException("Connection need to be initialed first using \"Connection\" command.", message);
//      }
  }

//  private void feedResponse(Message message) throws ProtocolException {
//    long responseID = message.getID();
//    Map toMap = message.get(Message.KEY_To, Map.class);
//    Object clientIDO = toMap.get(Message.KEY_CLIENT_ID);
//    if (clientIDO == null || !(clientIDO instanceof Integer)) {
//      throw new ProtocolException("The response destination need to specified correctly.", message);
//    }
//    int clientID = (int) clientIDO;
//    WaitingRequestKey key = new WaitingRequestKey(clientID, responseID);
//    Message associatedRequest = waitingRequests.get(key);
//    if (associatedRequest == null) {
//      throw new ProtocolException("Request associated to this response not exists.", message);
//    }
//    if (!message.isContinues()) {
//      waitingRequests.remove(key);
//    }
//    Client toClient = Client.getClient(clientID);
//    if (toClient != null) {
//      toClient.response(message);
//    }
//  }
//  private void dealRequest(Message message) {
//    long messageID = message.getID();
//    int sourceID = (int) message.get(Message.KEY_FROM, Map.class).get(Message.KEY_CLIENT_ID);
//    WaitingRequestKey key = new WaitingRequestKey(sourceID, messageID);
//    if (waitingRequests.containsKey(key)) {
//      throw new ProtocolException("Request ID " + message.getID() + " Duplicate.", message);
//    }
//    waitingRequests.put(key, message);
//    session.write(message);
//  }
  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  boolean initialed() {
    return name != null;
  }

  boolean init(String name) {
    if (initialed()) {
      throw new RuntimeException("Client Already Initialed.");
    }
    this.name = name;
    if (registerClient(this)) {
      LOGGER.info("Client[{}] registered as [{}].", getId(), getName());
      HashMap<String, Object> map = new HashMap<>();
      map.put(KEY_IDENTITY_NAME, name);
      map.put(Message.KEY_CLIENT_ID, id);
      identity = Collections.unmodifiableMap(map);
      return true;
    } else {
      this.name = null;
      LOGGER.warn("Client[{}] failed in register as [{}]: name duplicated.", getId(), name);
      return false;
    }
  }

  public void registerService(String serviceName) {
    Service service = ServiceManager.getDefault().registerService(serviceName, this);
    this.services.putIfAbsent(serviceName, service);
    LOGGER.info("Client[{}, {}] registered for service[{}].", getId(), getName(), serviceName);
  }

  private static AtomicInteger CLIENT_IDS = new AtomicInteger(0);

  private static final HashMap<Integer, Client> clientsByID = new HashMap<>();
  private static final HashMap<String, Client> clientsByName = new HashMap<>();

  public static Client create(IoSession session) {
    Client client = new Client(CLIENT_IDS.getAndIncrement(), session);
    LOGGER.info("Client[{}] connected. Session[{}], Address[{}].", client.getId(), session.getId(), session.getRemoteAddress());
    return client;
  }

  private static boolean registerClient(Client client) {
    synchronized (clientsByName) {
      if (clientsByName.containsKey(client.getName())) {
        return false;
      }
      clientsByID.put(client.getId(), client);
      clientsByName.put(client.getName(), client);
      LOGGER.trace("Client[{}, {}] registed. There are currently {} clients.", client.getId(), client.getName(), clientsByName.size());
      return true;
    }
  }

  private static void unregisterClient(Client client) {
    synchronized (clientsByName) {
      clientsByID.remove(client.getId());
      clientsByName.remove(client.getName());
      LOGGER.trace("Client[{}, {}] unregisted. There are currently {} clients.", client.getId(), client.getName(), clientsByName.size());
    }
  }

  public static Client getClient(String clientName) {
    return clientsByName.get(clientName);
  }

  public static Client getClient(int clientId) {
    return clientsByID.get(clientId);
  }

  public void close() {
    if (initialed()) {
      unregisterClient(this);
    }
    for (String serviceName : services.keySet()) {
      ServiceManager.getDefault().unregisterService(serviceName, this);
    }
    LOGGER.info("Client[{}, {}] disconnected. Session[{}], Address[{}].", getId(), getName(), session.getId(), session.getRemoteAddress());
  }

//  private class WaitingRequestKey {
//
//    private final int sourceID;
//    private final long messageID;
//
//    public WaitingRequestKey(int sourceID, long messageID) {
//      this.sourceID = sourceID;
//      this.messageID = messageID;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//      if (obj == null || !(obj instanceof WaitingRequestKey)) {
//        return false;
//      }
//      WaitingRequestKey inst = (WaitingRequestKey) obj;
//      return inst.sourceID == sourceID && inst.messageID == messageID;
//    }
//
//    @Override
//    public int hashCode() {
//      int hash = 5;
//      hash = 43 * hash + this.sourceID;
//      hash = 43 * hash + (int) (this.messageID ^ (this.messageID >>> 32));
//      return hash;
//    }
//  }
}
