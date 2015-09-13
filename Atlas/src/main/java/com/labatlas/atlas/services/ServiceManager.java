package com.labatlas.atlas.services;

import com.labatlas.atlas.Client;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Hwaipy
 */
public class ServiceManager {

  private final ConcurrentHashMap<String, Service> serviceMap = new ConcurrentHashMap<>();

  private ServiceManager() {
  }

  public Service registerService(String serviceName, Client client) {
    Service service = getService(serviceName, true);

    return service;
  }

  private static final ServiceManager INSTANCE = new ServiceManager();

  public static ServiceManager getDefault() {
    return INSTANCE;
  }

  private Service getService(String name, boolean create) {
    Service service = serviceMap.get(name);
    if (service == null && create) {
      service = new Service(name);
      Service previous = serviceMap.putIfAbsent(name, service);
      if (previous != null) {
        service = previous;
      }
    }
    return service;
  }

}
