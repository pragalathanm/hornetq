package com.hornetq.client;

import com.hornetq.client.HornetQReceiver.MessageListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.hornetq.api.core.DiscoveryGroupConfiguration;
import org.hornetq.api.core.HornetQConnectionTimedOutException;
import org.hornetq.api.core.JGroupsBroadcastGroupConfiguration;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.MessageHandler;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.api.core.client.loadbalance.RoundRobinConnectionLoadBalancingPolicy;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.jgroups.JChannel;
import org.jgroups.conf.ConfiguratorFactory;
import org.jgroups.conf.ProtocolConfiguration;
import org.jgroups.conf.ProtocolStackConfigurator;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class HornetQClientFactory {

    private static final HornetQClientFactory INSTANCE = new HornetQClientFactory();
    private static final HornetQClientFactory TEST_INSTANCE = new HornetQClientFactory(true);
    private final List<ServerConfiguration> configurations = new ArrayList<>();
    private ServerLocator locator;
    private boolean testEnvironment;
    private static final Logger logger = Logger.getLogger(HornetQClientFactory.class.getName());

    private HornetQClientFactory() {
        this(false);
    }

    private HornetQClientFactory(boolean testEnvironment) {
        this.testEnvironment = testEnvironment;
        if (testEnvironment) {
            configurations.add(new ServerConfiguration());
        } else {
            try {
                configurations.addAll(new HQBrowserClient().getConfiguration());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        locator = createLocator();
    }

    /**
     * Creates a {@link HornetQClientFactory} which is aware of all the HornetQ
     * servers. This also takes care of server fail over, i.e., if any of the
     * HornetQ servers fail, then the {@code HornetQSender} and
     * {@code HornetQReceiver} created using this factory will automatically
     * reconnect to any other available server.
     *
     * @return the {@code HornetQClientFactory}
     */
    public static HornetQClientFactory getDefault() {
        return INSTANCE;
    }

    /**
     * Creates a {@link HornetQClientFactory} for testing purpose. This assumes
     * that the HornetQ server is running on local host.
     *
     * @return the {@code HornetQClientFactory}
     */
    public static HornetQClientFactory getTestFactory() {
        return TEST_INSTANCE;
    }

    /**
     * Creates a sender for the given {@code queueName}. For better performance,
     * create multiple senders and use them in separate threads.
     *
     * @param queueName the name of the queue,to which the message is to be sent
     * @return {@code HornetQSender} object
     * @throws Exception
     */
    public HornetQSender createSender(String queueName) throws Exception {
        return createSender(queueName, false);
    }

    /**
     * Creates a sender for the given {@code queueName}. For better performance,
     * create multiple senders and use them in separate threads.
     *
     * If ordering of messages is enabled then only one {@link HornetQReceiver}
     * will receive all the messages. In that case you can't also use multiple
     * senders. If ordering is not required, then you should turn off the
     * {@code ordered} flag, for maximum performance.
     *
     * @param queueName the name of the queue,to which the message to be sent
     * @param ordered messages are guaranteed to be delivered in order, if true,
     * false otherwise
     * @return {@code HornetQSender} object
     * @throws Exception
     */
    public HornetQSender createSender(String queueName, boolean ordered) throws Exception {
        ServerLocator locatorCopy = locator;
        if (ordered) {
            locatorCopy = createLocator();
            locatorCopy.setAutoGroup(true);
        }
        return new HornetQSender(locatorCopy, queueName, ordered);
    }

    /**
     * Creates a message listener for the give queue {@code queueName}.
     * Acknowledging the message received should be handled by the
     * {@code handler}. To use the default acknowledgment, use the
     * {@link #createReceiver(java.lang.String, com.hornetq.client.HornetQReceiver.MessageListener)}
     * variation.
     *
     * @param queueName the name of the queue for which receiver to be created
     * @param handler the handler to receive messages
     * @return the {@code HornetQReceiver}
     * @throws Exception
     */
    public HornetQReceiver createReceiver(String queueName, MessageHandler handler) throws Exception {
        return new HornetQReceiver(locator, queueName, handler, configurations.size());
    }

    /**
     * Creates a message listener for the give queue {@code queueName} along
     * with the concurrency level. Acknowledging the message received should be
     * handled by the {@code handler}. To use the default acknowledgment, use
     * the
     * {@link #createReceiver(java.lang.String, com.hornetq.client.HornetQReceiver.MessageListener)}
     * variation.
     *
     * @param queueName the name of the queue for which receiver to be created
     * @param handler the handler to receive messages
     * @param concurrencyLevel the number of messages to be received in parallel
     * @return the {@code HornetQReceiver}
     * @throws Exception
     */
    public HornetQReceiver createReceiver(String queueName, MessageHandler handler, int concurrencyLevel) throws Exception {
        return new HornetQReceiver(locator, queueName, handler, configurations.size());
    }

    /**
     * Creates a message listener for the give queue {@code queueName}. Once
     * consumed the message is automatically acknowledged.
     *
     * @param queueName the name of the queue for which receiver to be created
     * @param listener the listener to receive messages
     * @return the {@code HornetQReceiver}
     * @throws Exception
     */
    public HornetQReceiver createReceiver(String queueName, MessageListener listener) throws Exception {
        return new HornetQReceiver(locator, queueName, listener, configurations.size());
    }

    /**
     * Creates a message listener for the give queue {@code queueName}. Once
     * consumed the message is automatically acknowledged.
     *
     * @param queueName the name of the queue for which receiver to be created
     * @param listener the listener to receive messages
     * @param concurrencyLevel the number of messages to be received in parallel
     * @return the {@code HornetQReceiver}
     * @throws Exception
     */
    public HornetQReceiver createReceiver(String queueName, MessageListener listener, int concurrencyLevel) throws Exception {
        HornetQConnectionTimedOutException exception = null;
        for (int i = 1; i <= 3; i++) {
            // try for 3 times in case you dont get connected to hornetq server
            try {
                return new HornetQReceiver(locator, queueName, listener, concurrencyLevel);
            } catch (HornetQConnectionTimedOutException ex) {
                logger.warning(ex.getMessage());
                exception = ex;
            }
        }
        throw exception;
    }

    private ServerLocator createLocator() {
        if (testEnvironment) {
            TransportConfiguration[] servers = new TransportConfiguration[configurations.size()];
            for (int i = 0; i < configurations.size(); i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("host", configurations.get(i).getHost());
                map.put("port", configurations.get(i).getPort());
                servers[i] = new TransportConfiguration(NettyConnectorFactory.class.getName(), map);
            }
            ServerLocator l = HornetQClient.createServerLocatorWithHA(servers);
            l.setReconnectAttempts(5);
            return l;
        } else {
            try {
                String jgroupsConfig = "hornetq-tcp.xml";
                URL properties = Thread.currentThread().getContextClassLoader().getResource(jgroupsConfig);
                ProtocolStackConfigurator configurator = ConfiguratorFactory.getStackConfigurator(properties);
                List<ProtocolConfiguration> configs = configurator.getProtocolStack();
                for (ProtocolConfiguration config : configs) {
                    if (config.getProtocolName().equals("TCPPING")) {
                        config.getProperties().put("initial_hosts", getInitialHosts());
                    }
                }
                JChannel channel = new JChannel(configurator);
                JGroupsBroadcastGroupConfiguration jGroupsBroadcastGroupConfiguration = new JGroupsBroadcastGroupConfiguration(channel, "hornetq_broadcast_channel");
                ServerLocator l = HornetQClient.createServerLocatorWithHA(new DiscoveryGroupConfiguration(2000, 5000, jGroupsBroadcastGroupConfiguration));
                l.setReconnectAttempts(5);
                l.setConnectionLoadBalancingPolicyClassName(RoundRobinConnectionLoadBalancingPolicy.class.getName());
                return l;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private String getInitialHosts() {
        StringBuilder builder = new StringBuilder("${jgroups.tcpping.initial_hosts:");
        for (ServerConfiguration configuration : configurations) {
            builder.append(configuration.getHost());
            builder.append("[").append(configuration.getPort()).append("],");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder.toString();
    }

    ServerLocator getLocator() {
        return locator;
    }
}
