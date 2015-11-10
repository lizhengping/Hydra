package com.labatlas.atlas.test.dcclient;

import com.labatlas.atlas.Message;
import com.labatlas.atlas.ProtocolException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.Timer;
import org.apache.mina.api.IdleStatus;
import org.apache.mina.api.IoHandler;
import org.apache.mina.api.IoService;
import org.apache.mina.api.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hwaipy
 */
public class MessageClientHandler implements IoHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageClientHandler.class);
  private final String name;
  private int messageIndex = 0;
  private IoSession session;
  private final DCSupplyFrame display;
  private final ArrayList<String> deviceNames = new ArrayList<>();

  public MessageClientHandler(String name, DCSupplyFrame display) {
    this.name = name;
    this.display = display;
    deviceNames.add("DC Supply Alice 1");
    deviceNames.add("DC Supply Alice 2");
    deviceNames.add("DC Supply Bob 1");
    deviceNames.add("DC Supply Bob 2");
  }

  @Override
  public void sessionOpened(IoSession session) {
    Message connectionMessage = newRequest(Message.COMMAND_CONNECTION);
    connectionMessage.put(Message.KEY_NAME, name);
    session.write(connectionMessage);
    this.session = session;
  }

  @Override
  public void sessionClosed(IoSession session) {
  }

  @Override
  public void sessionIdle(IoSession session, IdleStatus status) {
  }

  @Override
  public void messageReceived(IoSession session, Object messageObject) {
    if (messageObject instanceof List) {
      List messageList = (List) messageObject;
      for (Object messageO : messageList) {
        if (messageO instanceof Message) {
          Message message = (Message) messageO;
          if (message.getType() == Message.Type.RESPONSE) {
            switch (message.getCommandString()) {
              case Message.COMMAND_CONNECTION:
                monitorStart();
                break;
              case "Measure":
                List<Double> voltages = message.getAsList("Voltages", Double.class);
                List<Double> currents = message.getAsList("Currents", Double.class);
                String deviceName = message.get("Source", String.class);
                int deviceIndex = deviceNames.indexOf(deviceName);
                display.updateStatus(deviceIndex, voltages, currents);
            }
          }
        }
      }
    }
  }

  @Override
  public void messageSent(IoSession session, Object message) {
  }

  @Override
  public void serviceActivated(IoService service) {
  }

  @Override
  public void serviceInactivated(IoService service) {
  }

  @Override
  public void exceptionCaught(IoSession session, Exception cause) {
    if (cause instanceof ProtocolException) {
      System.out.println(cause);
    } else {
      cause.printStackTrace(System.out);
    }
  }

  private Message newRequest(String cmd) {
    Message message = new Message();
    message.put(Message.KEY_MESSAGE_ID, messageIndex).put(Message.KEY_REQUEST, cmd);
    messageIndex++;
    return message;
  }

  private void monitorStart() {
    Timer timer = new Timer(1000, new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        for (String deviceName : deviceNames) {
          Message monitorQuery = newRequest("Measure");
          monitorQuery.put(Message.KEY_TARGET, deviceName);
          session.write(monitorQuery);
        }
      }
    });
    timer.setInitialDelay(1000);
    timer.start();
    display.setReady();
  }

  public void pockelsCellOn(int deviceIndex) {
    Message remote1 = newRequest("Remote");
    remote1.put(Message.KEY_TARGET, deviceNames.get(deviceIndex));
    session.write(remote1);
    Message remote2 = newRequest("Remote");
    remote2.put(Message.KEY_TARGET, deviceNames.get(deviceIndex + 1));
    session.write(remote2);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      java.util.logging.Logger.getLogger(MessageClientHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
    Message set1 = newRequest("Set");
    set1.put(Message.KEY_TARGET, deviceNames.get(deviceIndex))
            .put("Voltages", new double[]{24, 24, 6})
            .put("Currents", new double[]{1, 1.5, 1});
    session.write(set1);
    Message set2 = newRequest("Set");
    set2.put(Message.KEY_TARGET, deviceNames.get(deviceIndex + 1))
            .put("Voltages", new double[]{12, 0, 0})
            .put("Currents", new double[]{3, 3, 3});
    session.write(set2);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      java.util.logging.Logger.getLogger(MessageClientHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
    Message on1 = newRequest("Output");
    on1.put(Message.KEY_TARGET, deviceNames.get(deviceIndex + 1))
            .put("Outputs", new int[]{1, 0, 0});
    session.write(on1);
    Message on2 = newRequest("Output");
    on2.put(Message.KEY_TARGET, deviceNames.get(deviceIndex))
            .put("Outputs", new int[]{1, 0, 0});
    session.write(on2);
    try {
      Thread.sleep(3000);
    } catch (InterruptedException ex) {
      java.util.logging.Logger.getLogger(MessageClientHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
    Message on3 = newRequest("Output");
    on3.put(Message.KEY_TARGET, deviceNames.get(deviceIndex))
            .put("Outputs", new int[]{1, 1, 0});
    session.write(on3);
    try {
      Thread.sleep(3000);
    } catch (InterruptedException ex) {
      java.util.logging.Logger.getLogger(MessageClientHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
    Message on4 = newRequest("Output");
    on4.put(Message.KEY_TARGET, deviceNames.get(deviceIndex))
            .put("Outputs", new int[]{1, 1, 1});
    session.write(on4);
    try {
      Thread.sleep(3000);
    } catch (InterruptedException ex) {
      java.util.logging.Logger.getLogger(MessageClientHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void pockelsCellOff(int deviceIndex) {
    Message on1 = newRequest("Output");
    on1.put(Message.KEY_TARGET, deviceNames.get(deviceIndex))
            .put("Outputs", new int[]{1, 1, 0});
    session.write(on1);
    try {
      Thread.sleep(3000);
    } catch (InterruptedException ex) {
      java.util.logging.Logger.getLogger(MessageClientHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
    Message on2 = newRequest("Output");
    on2.put(Message.KEY_TARGET, deviceNames.get(deviceIndex))
            .put("Outputs", new int[]{1, 0, 0});
    session.write(on2);
    try {
      Thread.sleep(3000);
    } catch (InterruptedException ex) {
      java.util.logging.Logger.getLogger(MessageClientHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
    Message on3 = newRequest("Output");
    on3.put(Message.KEY_TARGET, deviceNames.get(deviceIndex))
            .put("Outputs", new int[]{0, 0, 0});
    session.write(on3);
    Message on4 = newRequest("Output");
    on4.put(Message.KEY_TARGET, deviceNames.get(deviceIndex + 1))
            .put("Outputs", new int[]{0, 0, 0});
    session.write(on4);
    try {
      Thread.sleep(3000);
    } catch (InterruptedException ex) {
      java.util.logging.Logger.getLogger(MessageClientHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void testOn() {
    Message remote1 = newRequest("Remote");
    remote1.put(Message.KEY_TARGET, deviceNames.get(1));
    session.write(remote1);
    Message remote2 = newRequest("Remote");
    remote2.put(Message.KEY_TARGET, deviceNames.get(3));
    session.write(remote2);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      java.util.logging.Logger.getLogger(MessageClientHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
    Message set1 = newRequest("Set");
    set1.put(Message.KEY_TARGET, deviceNames.get(1))
            .put("Voltages", new double[]{0, 0, 6})
            .put("Currents", new double[]{1, 1.5, 1});
    session.write(set1);
    Message set2 = newRequest("Set");
    set2.put(Message.KEY_TARGET, deviceNames.get(3))
            .put("Voltages", new double[]{0, 0, 6})
            .put("Currents", new double[]{1, 1.5, 1});
    session.write(set2);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      java.util.logging.Logger.getLogger(MessageClientHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
    Message on1 = newRequest("Output");
    on1.put(Message.KEY_TARGET, deviceNames.get(1))
            .put("Outputs", new int[]{0, 0, 1});
    session.write(on1);
    Message on2 = newRequest("Output");
    on2.put(Message.KEY_TARGET, deviceNames.get(3))
            .put("Outputs", new int[]{0, 0, 1});
    session.write(on2);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      java.util.logging.Logger.getLogger(MessageClientHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void testOff() {
    Message on1 = newRequest("Output");
    on1.put(Message.KEY_TARGET, deviceNames.get(1))
            .put("Outputs", new int[]{0, 0, 0});
    session.write(on1);
    Message on2 = newRequest("Output");
    on2.put(Message.KEY_TARGET, deviceNames.get(3))
            .put("Outputs", new int[]{0, 0, 0});
    session.write(on2);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      java.util.logging.Logger.getLogger(MessageClientHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
