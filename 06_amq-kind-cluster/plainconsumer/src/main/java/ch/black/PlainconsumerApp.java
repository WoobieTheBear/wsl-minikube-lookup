package ch.black;

import java.util.concurrent.TimeUnit;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

public class PlainconsumerApp {
    private static final Logger log = LoggerFactory.getLogger(PlainconsumerApp.class);

    public static void main(String[] args) {
        log.info("PlainconsumerApp started (version 1.0.0)");
        boolean listen = true;
        try (ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://amq-broker-service.default.svc.cluster.local:61616", "admin", "admin")) {
            log.info("factory created");
            try (Connection connection = factory.createConnection()) {
                log.info("connection created");
                connection.start();

                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createQueue("queueOne");
                MessageConsumer consumer = session.createConsumer(destination);
                while (listen) {
                    TextMessage message = (TextMessage) consumer.receive();
                    if (message != null) {
                        log.info("Received: {}", message.getText());
                        listen = false;
                    } else {
                        TimeUnit.MILLISECONDS.sleep(600);
                    }
                }
                consumer.close();
                session.close();
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}