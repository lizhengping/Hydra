package com.labatlas.atlas;

import static com.labatlas.atlas.Message.KEY_TARGET;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Hwaipy
 */
public class Target {

  private final boolean local;
  private final boolean multiTarget;
  private final ArrayList< Client> clients = new ArrayList<>();

  private Target() {
    this(true, null);
  }

  private Target(Client client) {
    this(false, client);
  }

  private Target(boolean local, Client remoteClient) {
    this(local, false, remoteClient);
  }

  private Target(boolean local, boolean multiTarget, Client... remoteClients) {
    this.local = local;
    this.multiTarget = multiTarget;
    for (Client remoteClient : remoteClients) {
      if (remoteClient != null) {
        this.clients.add(remoteClient);
      }
    }
  }

  public boolean isLocal() {
    return local;
  }

  public boolean isMultiTarget() {
    return multiTarget;
  }

  public Collection<Client> getRemoteClients() {
    return Collections.unmodifiableList(clients);
  }

  static Target create(Message message) {
    Object targetO = message.get(KEY_TARGET);
    if (targetO == null) {
      return new Target();
    } else if (targetO instanceof String) {
      return create((String) targetO);
    } else if (targetO instanceof Integer) {
      return create((int) targetO);
    } else if (targetO instanceof Long) {
      return create((long) targetO);
    } else if (targetO instanceof Map) {
      return create((Map) targetO);
    } else {
      throw new ProtocolException("Target not recognized.", message);
    }
  }

  private static Target create(String target) {
    return new Target(Client.getClient(target));
  }

  private static Target create(long id) {
    throw new UnsupportedOperationException();
  }

  private static Target create(Map targetDescription) {
    throw new UnsupportedOperationException();
  }
}
