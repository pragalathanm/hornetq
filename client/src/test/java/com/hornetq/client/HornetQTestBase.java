package com.hornetq.client;

import com.hornetq.client.HornetQClientFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class HornetQTestBase {

    protected static HornetQServer server;
    private static final Logger logger = Logger.getLogger(HornetQTestBase.class.getName());

//    @BeforeSuite
    public static void startServer() {
        Configuration configuration = new ConfigurationImpl();
        //we only need this for the server lock file
        configuration.setJournalDirectory("target/data/journal");
        configuration.setPersistenceEnabled(false);
        configuration.setSecurityEnabled(false);
        configuration.getAcceptorConfigurations().add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));
        configuration.getAcceptorConfigurations().add(new TransportConfiguration(NettyAcceptorFactory.class.getName()));

        // Step 2. Create and start the server
        server = HornetQServers.newHornetQServer(configuration);
        logger.info("Starting HornetQ Server...");
        try {
            server.start();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    protected void createQueue(String queueName) throws Exception {
        ServerLocator locator = HornetQClientFactory.getTestFactory().getLocator();
        try (ClientSessionFactory factory = locator.createSessionFactory();
                ClientSession session = factory.createSession()) {
            session.createQueue(queueName, queueName, true);
        }
    }
}
