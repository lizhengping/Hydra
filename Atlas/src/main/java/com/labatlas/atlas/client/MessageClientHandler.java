package com.labatlas.atlas.client;

import com.labatlas.atlas.Client;
import org.apache.mina.api.IdleStatus;
import org.apache.mina.api.IoHandler;
import org.apache.mina.api.IoService;
import org.apache.mina.api.IoSession;
import org.apache.mina.session.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hwaipy
 */
public class MessageClientHandler implements IoHandler {

  private static final AttributeKey<Client> CLIENT_KEY = AttributeKey.createKey(Client.class, "Client");
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageClientHandler.class);

  @Override
  public void sessionOpened(IoSession session) {
//    Client client = Client.create();
//    session.setAttribute(CLIENT_KEY, client);
//    client.setSession(session);
//    LOGGER.info("Client[{}] connected. Session[{}], Address[{}].", client.getId(), session.getId(), session.getRemoteAddress());
  }

  @Override
  public void sessionClosed(IoSession session) {
//    Client client = session.getAttribute(CLIENT_KEY);
//    LOGGER.info("Client[{}] disconnected. Session[{}], Address[{}].", client.getId(), session.getId(), session.getRemoteAddress());
  }

  @Override
  public void sessionIdle(IoSession session, IdleStatus status) {
  }

  @Override
  public void messageReceived(IoSession session, Object messageObject) {
//    if (messageObject instanceof Collection) {
//      Collection<Message> messageList = (Collection<Message>) messageObject;
//      Client client = session.getAttribute(CLIENT_KEY);
//      client.feed(messageList);
//    } else {
//      throw new IllegalArgumentException("message passed should be a list of Message.");
//    }
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
//    if (cause instanceof MessageFormatException) {
//      MessageFormatException mfe = (MessageFormatException) cause;
//      session.write(mfe.getMessageObject().responseError().put(Message.KEY_ERROR_MESSAGE, mfe.getMessage()));
//    } else {
//      LOGGER.info("Exception caught, Session[" + session.getId() + "] is about to close.", cause);
//      session.close(true);
//    }
  }

}
