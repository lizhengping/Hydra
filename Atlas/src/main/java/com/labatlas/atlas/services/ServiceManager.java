package com.labatlas.atlas.services;

import com.labatlas.atlas.Client;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hwaipy
 */
public class ServiceManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceManager.class);
  private final ConcurrentHashMap<String, Service> serviceMap = new ConcurrentHashMap<>();

  private ServiceManager() {
  }

  public Service registerService(String serviceName, Client client) {
    Service service = getService(serviceName, true);

    //do something
    LOGGER.info("Client[{}, {}] registered for service[{}]. Now service[{}] has {} instances.",
            client.getId(), client.getName(), serviceName, serviceName, -1);

    return service;
  }

  public Service unregisterService(String serviceName, Client client) {
    Service service = getService(serviceName, true);

    //do something
    LOGGER.info("Client[{}, {}] unregistered from service[{}]. Now service[{}] has {} instances.",
            client.getId(), client.getName(), serviceName, serviceName, -1);

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
