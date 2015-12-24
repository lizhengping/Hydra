package com.hydra.core;

import com.hydra.services.Service;
import com.hydra.services.ServiceManager;
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
  public static final String KEY_CONNECTION_TIME = "ConnectionTime";
  private final int id;
  private String name;
  private Map identity;
  private IoSession session;
  private ConcurrentHashMap<String, Service> services = new ConcurrentHashMap<>();
  private long connectionTime;
  private boolean closed = false;

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
        feedMessageToRemote(message);
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
  }

  private void feedMessageToRemote(Message message) throws ProtocolException {
    Target target = message.getTarget();
    Collection<Client> remoteClients = target.getRemoteClients();
    message.put(Message.KEY_FROM, getName());
    if (!target.isMultiTarget() && remoteClients.isEmpty()) {
      Message responseError = message.responseError().put(Message.KEY_ERROR_MESSAGE, "Target no exists.");
      LOGGER.debug("The target of message does not exist in Client[{}, {}]: {}", getId(), getName(), message);
      write(responseError);
    } else {
      for (Client remoteClient : remoteClients) {
        remoteClient.write(message);
      }
    }
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public boolean isInitialed() {
    return name != null;
  }

  public boolean isClosed() {
    return closed;
  }

  public boolean init(String name) {
    if (isInitialed()) {
      throw new RuntimeException("Client Already Initialed.");
    }
    this.name = name;
    connectionTime = System.currentTimeMillis();
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
  }

  private static AtomicInteger CLIENT_IDS = new AtomicInteger(0);

  private static final HashMap<Integer, Client> CLIENTS_BY_ID = new HashMap<>();
  private static final HashMap<String, Client> CLIENTS_BY_NAME = new HashMap<>();

  public static Client create(IoSession session) {
    Client client = new Client(CLIENT_IDS.getAndIncrement(), session);
    LOGGER.info("Client[{}] connected. Session[{}], Address[{}].", client.getId(), session.getId(), session.getRemoteAddress());
    return client;
  }

  private static boolean registerClient(Client client) {
    synchronized (CLIENTS_BY_NAME) {
      if (CLIENTS_BY_NAME.containsKey(client.getName())) {
        return false;
      }
      CLIENTS_BY_ID.put(client.getId(), client);
      CLIENTS_BY_NAME.put(client.getName(), client);
      LOGGER.trace("Client[{}, {}] registed. There are currently {} clients.", client.getId(), client.getName(), CLIENTS_BY_NAME.size());
      return true;
    }
  }

  private static void unregisterClient(Client client) {
    synchronized (CLIENTS_BY_NAME) {
      CLIENTS_BY_ID.remove(client.getId());
      CLIENTS_BY_NAME.remove(client.getName());
      LOGGER.trace("Client[{}, {}] unregisted. There are currently {} clients.", client.getId(), client.getName(), CLIENTS_BY_NAME.size());
    }
  }

  public static Client getClient(String clientName) {
    return CLIENTS_BY_NAME.get(clientName);
  }

  public static Client getClient(int clientId) {
    return CLIENTS_BY_ID.get(clientId);
  }

  public static Collection<Client> getClients() {
    return Collections.unmodifiableCollection(CLIENTS_BY_ID.values());
  }

  public Map getSummaryInfomation() {
    Map map = new HashMap();
    map.put(Message.KEY_CLIENT_ID, id);
    map.put(KEY_IDENTITY_NAME, name);
    map.put(KEY_CONNECTION_TIME, connectionTime);
    return map;
  }

  public void close() {
    if (isInitialed()) {
      unregisterClient(this);
    }
    for (String serviceName : services.keySet()) {
      ServiceManager.getDefault().unregisterService(serviceName, this);
    }
    SummaryManager.getDefault().unregisterSummaryListener(this);
    closed = true;
    LOGGER.info("Client[{}, {}] disconnected. Session[{}], Address[{}].", getId(), getName(), session.getId(), session.getRemoteAddress());
  }
}
