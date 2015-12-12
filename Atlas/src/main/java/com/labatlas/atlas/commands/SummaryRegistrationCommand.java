package com.labatlas.atlas.commands;

import com.labatlas.atlas.Client;
import com.labatlas.atlas.Command;
import com.labatlas.atlas.Message;
import com.labatlas.atlas.SummaryManager;

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
