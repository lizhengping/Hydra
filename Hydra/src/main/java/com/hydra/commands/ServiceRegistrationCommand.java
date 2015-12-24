package com.hydra.commands;

import com.hydra.core.Client;
import com.hydra.core.Command;
import com.hydra.core.Message;
import java.util.List;

/**
 *
 * @author Hwaipy
 */
public class ServiceRegistrationCommand extends Command {

  public static final String KEY_SERVICE = "Service";

  public ServiceRegistrationCommand() {
    super("ServiceRegistration");
  }

  @Override
  protected void executeCommand(Message message, Client client) {
    List<String> list = message.getAsList(KEY_SERVICE, String.class);
    for (String servicename : list) {
      client.registerService(servicename);
      Message response = message.response()
              .put(KEY_SERVICE, servicename)
              .put(Message.KEY_STATUS, Message.VALUE_STATUS_OK);
      client.write(response);
    }
  }

}
