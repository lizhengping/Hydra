package com.hydra.commands;

import com.hydra.core.Client;
import com.hydra.core.Command;
import com.hydra.core.Message;
import com.hydra.core.SummaryManager;

/**
 *
 * @author Hwaipy
 */
public class SummaryRegistrationCommand extends Command {

  public SummaryRegistrationCommand() {
    super("SummaryRegistration");
  }

  @Override
  protected void executeCommand(Message message, Client client) {
    if (SummaryManager.getDefault().registerSummaryListener(message, client)) {
      Message response = message.response()
              .put(Message.KEY_STATUS, Message.VALUE_STATUS_OK);
      client.write(response);
    } else {
      Message response = message.responseError().put(Message.KEY_ERROR_MESSAGE, "Summary already linstening.");
      client.write(response);
    }
  }
}
