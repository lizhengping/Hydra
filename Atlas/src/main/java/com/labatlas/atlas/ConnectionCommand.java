package com.labatlas.atlas;

import com.labatlas.atlas.message.Message;
import com.labatlas.atlas.message.MessageFormatException;

/**
 *
 * @author Hwaipy
 */
class ConnectionCommand extends Command {

  public ConnectionCommand() {
    super("Connection");
  }

  @Override
  public void execute(Message message, Client client) {
    if (client.initialed()) {
      throw new MessageFormatException("Command \"Connection\" should only be thge first Message.", message);
    } else {
      String name = message.get(Message.KEY_NAME, String.class);
      client.init(name);
      Message response = message.response();
      client.response(response);
    }
  }

  @Override
  protected void executeCommand(Message message, Client client) {
  }

}
