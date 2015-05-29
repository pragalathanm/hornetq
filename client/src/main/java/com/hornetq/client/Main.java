package com.hornetq.client;

import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import org.hornetq.api.core.DiscoveryGroupConfiguration;
import org.hornetq.api.core.JGroupsBroadcastGroupConfiguration;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.jgroups.JChannel;
import org.jgroups.conf.ConfiguratorFactory;
import org.jgroups.conf.ProtocolConfiguration;
import org.jgroups.conf.ProtocolStackConfigurator;

/**
 *
 * @author Pragalathan M<pragalathanm@gmail.com>
 */
public class Main {

    protected String testQueueName = "jms.queue.testQueue";

    public static void main(String[] args) throws Exception {
        new Main().send();
//        new Main().receive();
//        test();
    }

    static void test() throws Exception {
        String jgroupsConfig = "hornetq-tcp.xml";
        JChannel channel = new JChannel(jgroupsConfig);
        URL properties = Thread.currentThread().getContextClassLoader().getResource(jgroupsConfig);
        ProtocolStackConfigurator configurator = ConfiguratorFactory.getStackConfigurator(properties);
        List<ProtocolConfiguration> configs = configurator.getProtocolStack();
        for (ProtocolConfiguration config : configs) {
            System.out.println(config.getProtocolName() + "==" + config.getProperties());
            if (config.getProtocolName().equals("TCPPING")) {
                config.getProperties().put("initial_hosts", "${jgroups.tcpping.initial_hosts:hornetq-1.com[7800]}");
            }
        }
        for (ProtocolConfiguration config : configs) {
            System.out.println(config.getProtocolName() + "==" + config.getProperties());
        }
    }
    private ServerLocator getLocator() throws UnknownHostException, Exception {
//        JChannel channel = new JChannel(false);         // (1)
//        ProtocolStack stack = new ProtocolStack(); // (2)
//        channel.setProtocolStack(stack);
//
//        stack.addProtocol(new TCP().setValue("bind_port", 7800))
//                .addProtocol(new TCPPING().setValue("initial_hosts", Arrays.asList(
//                                        new IpAddress[]{
//                                            new IpAddress("hornetq-1.com", 7800),
//                                            new IpAddress("hornetq-2.com", 7800)
//                                        }
//                                )))
//                .addProtocol(new MERGE3())
//                .addProtocol(new FD_SOCK())
//                .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
//                .addProtocol(new VERIFY_SUSPECT())
//                .addProtocol(new BARRIER())
//                .addProtocol(new NAKACK())
//                .addProtocol(new UNICAST2())
//                //                .addProtocol(new STABLE())
//                .addProtocol(new GMS())
//                //                .addProtocol(new UFC())
//                .addProtocol(new MFC())
//                .addProtocol(new FRAG2());       // (3)
//        stack.init();
//        channel.connect("hornetq_broadcast_channel");

//        JGroupsBroadcastGroupConfiguration jGroupsBroadcastGroupConfiguration = new JGroupsBroadcastGroupConfiguration(channel, "hornetq_broadcast_channel");
        JGroupsBroadcastGroupConfiguration jGroupsBroadcastGroupConfiguration = new JGroupsBroadcastGroupConfiguration("jgroups-tcp.xml", "hornetq_broadcast_channel");
        ServerLocator factory = HornetQClient.createServerLocatorWithHA(new DiscoveryGroupConfiguration(1000, 1000, jGroupsBroadcastGroupConfiguration));
        return factory;
    }

    public void send() throws Exception {
        HornetQClientFactory clientFactory = HornetQClientFactory.getDefault();
        try (HornetQSender sender = clientFactory.createSender(testQueueName)) {
            for (int i = 0; i < 20; i++) {
//                Thread.sleep(5000);
//                try {
                sender.sendMessage("msg: " + i);
                System.out.println("sent " + i);
//                } catch (Exception ex) {
//                    System.out.println(ex.getMessage());
//                }
            }
        }
        System.exit(0);
    }

    public void receive() throws Exception {
        HornetQClientFactory clientFactory = HornetQClientFactory.getDefault();
        final int[] count = {20};
        HornetQReceiver.MessageListener listener = new HornetQReceiver.MessageListener() {

            @Override
            public void onMessage(String message) {
                System.err.println("message = " + message);
                count[0]--;
            }
        };
        try (HornetQReceiver receiver = clientFactory.createReceiver(testQueueName, listener, 2)) {
            while (count[0] > 0) {
//                Thread.sleep(500);
            }
        }
        System.exit(0);
    }
}
