package com.hornetq.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.MessageHandler;
import org.hornetq.api.core.client.ServerLocator;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class HornetQReceiver implements Closeable {

    private final List<ClientSession> sessions = new ArrayList<>();
    private final List<ClientSessionFactory> factories = new ArrayList<>();
    private final List<ClientConsumer> consumers = new ArrayList<>();
    private final int concurrencyLevel;
    private final String queueName;
    private static final Logger logger = Logger.getLogger(HornetQReceiver.class.getName());
    private static int WAIT_TIME = 5000;

    HornetQReceiver(ServerLocator locator, String queueName, MessageListener listener, int concurrencyLevel) throws Exception {
        this(locator, queueName, (MessageHandler) listener, concurrencyLevel);
    }

    HornetQReceiver(ServerLocator locator, String queueName, MessageHandler handler, int concurrencyLevel) throws Exception {
        this.concurrencyLevel = concurrencyLevel;
        this.queueName = queueName;
        if (WAIT_TIME == 0) {
            initialize(locator, handler, concurrencyLevel);
        } else {
            try (ClientSessionFactory firstFactory = locator.createSessionFactory()) {
                Thread.sleep(WAIT_TIME); // this is to ensure that the client gets the updated view of the cluster
                WAIT_TIME = 0;
                initialize(locator, handler, concurrencyLevel);
            }
        }
    }

    private void initialize(ServerLocator locator, MessageHandler handler, int concurrencyLevel) throws Exception {
        for (int i = 1; i <= concurrencyLevel; i++) {
            ClientSessionFactory factory = locator.createSessionFactory();
            String address = factory.getConnection().getRemoteAddress();
            ClientSession session = factory.createSession(true, true, 1);
            session.start();

            ClientConsumer consumer = session.createConsumer(queueName);
            consumer.setMessageHandler(handler);
            sessions.add(session);
            consumers.add(consumer);
            factories.add(factory);
            logger.log(Level.INFO, "Started receiver #{0} on {1} for {2}", new Object[]{i, address, queueName});
        }
    }

    @Override
    public void close() throws IOException {
        for (int i = 0; i < concurrencyLevel; i++) {
            try {
                String address = factories.get(i).getConnection().getRemoteAddress();
                consumers.get(i).close();
                sessions.get(i).close();
                factories.get(i).close();
                logger.log(Level.INFO, "Shutting down receiver #{0} on {1} for {2}", new Object[]{i + 1, address, queueName});
            } catch (HornetQException ex) {
                throw new IOException(ex);
            }
        }
    }

    public static abstract class MessageListener implements MessageHandler {

        @Override
        public final void onMessage(ClientMessage message) {
            try {
                onMessage(message.getBodyBuffer().readString());
                message.acknowledge();
            } catch (HornetQException ex) {
                logger.log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }

        public abstract void onMessage(String message);
    }
}
