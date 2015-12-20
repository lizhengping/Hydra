package com.labatlas.atlas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hwaipy
 */
public class SummaryManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(SummaryManager.class);
  private static final String KEY_SUMMARY = "Summary";
  private static final String KEY_CLIENTS_LIST = "ClientsList";
  private final ConcurrentHashMap<Client, Message> listeningClients;

  private SummaryManager() {
    this.listeningClients = new ConcurrentHashMap<>();
    Timer timer = new Timer("SummaryManager Timer", true);
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        triggerSummaryEvent();
      }
    }, 1000, 1000);
  }

  public boolean registerSummaryListener(Message message, Client client) {
    boolean success = listeningClients.putIfAbsent(client, message) == null;
    //do something
    LOGGER.trace("Client[{}, {}] registered for summary listening. Now SummeryManager has {} listeners.",
            client.getId(), client.getName(), -1);
    return success;
  }

  public void unregisterSummaryListener(Client client) {
    listeningClients.remove(client);
    //do something
    LOGGER.trace("Client[{}, {}] unregistered for summary listening. Now SummaryManager has {} listeners.",
            client.getId(), client.getName(), -1);
  }

  private void triggerSummaryEvent() {
    List< Map> summaryList = generateSummaryInfomation();
    for (Map.Entry<Client, Message> entry : listeningClients.entrySet()) {
      try {
        Message response = entry.getValue().response().put(KEY_SUMMARY, summaryList);
        entry.getKey().write(response);
      } catch (Exception e) {
        LOGGER.warn("Exception in writing summary message at Client[" + entry.getKey().getId() + ", " + entry.getKey().getName() + "]", e);
      }
    }
  }
  private static final SummaryManager INSTANCE = new SummaryManager();

  public static SummaryManager getDefault() {
    return INSTANCE;
  }

  private List<Map> generateSummaryInfomation() {
    Collection<Client> clients = Client.getClients();
    List<Map> summaryList = new ArrayList<>();
    for (Client client : clients) {
      Map csi = client.getSummaryInfomation();
      summaryList.add(csi);
    }
    return summaryList;
  }
}
