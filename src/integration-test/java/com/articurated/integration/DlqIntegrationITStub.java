package com.articurated.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.amqp.core.Message;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class DlqIntegrationITStub {

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.11-management");

    static CachingConnectionFactory connectionFactory;
    static RabbitTemplate rabbitTemplate;

    @BeforeAll
    public static void setup() {
        rabbit.start();
        connectionFactory = new CachingConnectionFactory(rabbit.getHost(), rabbit.getAmqpPort());
        connectionFactory.setUsername(rabbit.getAdminUsername());
        connectionFactory.setPassword(rabbit.getAdminPassword());
        rabbitTemplate = new RabbitTemplate(connectionFactory);
    }

    @AfterAll
    public static void tearDown() {
        if (connectionFactory != null) connectionFactory.destroy();
        rabbit.stop();
    }

    @Test
    public void failedMessageEndsUpInDlqStub() throws Exception {
        // Publish a message directly to the invoice queue that will be unhandled/failed by any consumer in this lightweight test.
        String dlqName = "invoice.generation.dlq";
        String payload = "12345";
        MessageProperties props = new MessageProperties();
        props.setContentType("text/plain");
        Message message = new Message(payload.getBytes(), props);

        // Send to the exchange with the invoice routing key
        rabbitTemplate.convertAndSend("articurated.exchange", "invoice.generate", payload);

        // Wait briefly for routing to the main queue
        Thread.sleep(1000);

        // Use raw RabbitMQ client to fetch from main queue and reject it to simulate a consumer failure -> dead-letter
        com.rabbitmq.client.ConnectionFactory cf = new com.rabbitmq.client.ConnectionFactory();
        cf.setHost(rabbit.getHost());
        cf.setPort(rabbit.getAmqpPort());
        cf.setUsername(rabbit.getAdminUsername());
        cf.setPassword(rabbit.getAdminPassword());

        try (com.rabbitmq.client.Connection conn = cf.newConnection();
             com.rabbitmq.client.Channel ch = conn.createChannel()) {
            // Get message from main invoice queue
            com.rabbitmq.client.GetResponse mainResp = ch.basicGet("invoice.generation.queue", false);
            assertThat(mainResp).isNotNull();
            long deliveryTag = mainResp.getEnvelope().getDeliveryTag();

            // Reject message (requeue=false) so broker dead-letters it to the configured DLX
            ch.basicReject(deliveryTag, false);

            // Wait briefly for dead-letter routing
            Thread.sleep(1000);

            com.rabbitmq.client.GetResponse resp = ch.basicGet(dlqName, true);
            assertThat(resp).isNotNull();
            String body = new String(resp.getBody());
            assertThat(body).contains("12345");
        }
    }
}
