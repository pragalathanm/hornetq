package com.hornetq.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.ServerLocator;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class HornetQSender implements Closeable {

    private final ClientSessionFactory factory;
    private final boolean ordered;
    private final String queueName;
    private final ServerLocator locator;
    private final ClientSession session;
    private final ClientProducer producer;
    private static final Logger logger = Logger.getLogger(HornetQSender.class.getName());

    HornetQSender(ServerLocator locator, String queueName, boolean ordered) throws Exception {
        this.locator = locator;
        this.ordered = ordered;
        this.queueName = queueName;
        factory = locator.createSessionFactory();
        session = factory.createSession();
        producer = session.createProducer(queueName);
        String address = factory.getConnection().getRemoteAddress();
        logger.log(Level.INFO, "Creating sender on {0} for {1}", new Object[]{address, queueName});
    }

    public boolean isOrdered() {
        return ordered;
    }

    public void sendMessage(String text) throws HornetQException {
        ClientMessage message = session.createMessage(true);
        message.getBodyBuffer().writeString(text);
        producer.send(message);
    }

    @Override
    public void close() throws IOException {
        try {
            producer.close();
            session.close();
            String address = factory.getConnection().getRemoteAddress();
            factory.close();
            if (ordered) {
                locator.close();
            }
            logger.log(Level.INFO, "Shutting down sender on {0} for {1}", new Object[]{address, queueName});
        } catch (HornetQException ex) {
            throw new IOException(ex);
        }
    }
}
