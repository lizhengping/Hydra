package com.labatlas.atlas;

import com.labatlas.atlas.commands.ServiceRegistrationCommand;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Hwaipy
 */
public class Commands {

  private static final ConcurrentHashMap<String, Command> COMMAND_MAP = new ConcurrentHashMap<>();

  public static boolean registerCommand(Command command) {
    Command previous = COMMAND_MAP.putIfAbsent(command.getName(), command);
    return previous == null;
  }

  public static Command getCommand(String name) {
    return COMMAND_MAP.get(name);
  }

  static {
    registerCommand(new ConnectionCommand());
    registerCommand(new ServiceRegistrationCommand());
  }
}
