package com.labatlas.atlas.commands;

import com.labatlas.atlas.Client;
import com.labatlas.atlas.Command;
import com.labatlas.atlas.Message;
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
      client.response(response);
    }
  }

}
