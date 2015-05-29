package com.hornetq.client;

import com.hornetq.client.HornetQClientFactory;
import com.hornetq.client.HornetQSender;
import com.hornetq.client.HornetQReceiver;
import java.util.logging.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
public class HornetQClientFactoryTest extends HornetQTestBase {

    private static final Logger logger = Logger.getLogger(HornetQClientFactoryTest.class.getName());
    protected String testQueueName = "jms.queue.testQueue";

    @BeforeTest
    public void createQueue() throws Exception {
//        createQueue(testQueueName);
    }

    @Test
    public void send() throws Exception {
        HornetQClientFactory clientFactory = HornetQClientFactory.getDefault();
        try (HornetQSender sender = clientFactory.createSender(testQueueName)) {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(5000);
                sender.sendMessage("msg: " + i);
                System.out.println("sent " + i);
            }
        }
    }

//    @Test(dependsOnMethods = {"send"}, timeOut = 5000)
    public void receive() throws Exception {
//        HornetQClientFactory clientFactory = HornetQClientFactory.getTestFactory();
        HornetQClientFactory clientFactory = HornetQClientFactory.getDefault();
        final int[] count = {10};
        HornetQReceiver.MessageListener listener = new HornetQReceiver.MessageListener() {

            @Override
            public void onMessage(String message) {
                count[0]--;
            }
        };
        try (HornetQReceiver receiver = clientFactory.createReceiver(testQueueName, listener, 1)) {
            while (count[0] > 0) {
                Thread.sleep(500);
            }
        }
    }
}
