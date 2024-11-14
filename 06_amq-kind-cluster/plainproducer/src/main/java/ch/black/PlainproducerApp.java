package ch.black;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.jms.Connection;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

public class PlainproducerApp {
    private static final Logger log = LoggerFactory.getLogger(PlainproducerApp.class);
    public static void main( String[] args ) {
        log.info("PlainproducerApp started (version 1.0.6)");
        try (ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://amq-broker-service.default.svc.cluster.local:61616", "admin", "admin")) {
            log.info("factory created");
            try (Connection connection = factory.createConnection()) {
                log.info("connection created");
                connection.start();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Queue queue = session.createQueue("queueOne");
                MessageProducer producer = session.createProducer(queue);
                TextMessage message = session.createTextMessage("This is the plain producers message BIG!");
                producer.send(message);
                log.info("Sent message: {}", message.getText());
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}
