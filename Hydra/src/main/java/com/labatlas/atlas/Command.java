package com.labatlas.atlas;

/**
 *
 * @author Hwaipy
 */
public abstract class Command {

  private final String name;

  public Command(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void execute(Message message, Client client) {
    if (client.isInitialed()) {
      executeCommand(message, client);
    } else {
      throw new ProtocolException("Command should be \"Connection\" for first Message.", message);
    }
  }

  protected abstract void executeCommand(Message message, Client client);
}
