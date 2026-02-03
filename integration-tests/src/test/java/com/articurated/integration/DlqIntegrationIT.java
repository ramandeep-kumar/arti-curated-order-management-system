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
public class DlqIntegrationIT {

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
    public void failedMessageEndsUpInDlq() throws Exception {
        String dlqName = "invoice.generation.dlq";
        String payload = "12345";
        // Use raw RabbitMQ client to declare exchange and queues with DLX args
        com.rabbitmq.client.ConnectionFactory cf = new com.rabbitmq.client.ConnectionFactory();
        cf.setHost(rabbit.getHost());
        cf.setPort(rabbit.getAmqpPort());
        cf.setUsername(rabbit.getAdminUsername());
        cf.setPassword(rabbit.getAdminPassword());

        try (com.rabbitmq.client.Connection conn = cf.newConnection();
             com.rabbitmq.client.Channel ch = conn.createChannel()) {
            // declare main exchange and DLX and queues
            ch.exchangeDeclare("articurated.exchange", "direct", true);
            ch.exchangeDeclare("articurated.dlx", "direct", true);

            java.util.Map<String, Object> args = new java.util.HashMap<>();
            args.put("x-dead-letter-exchange", "articurated.dlx");
            args.put("x-dead-letter-routing-key", dlqName);
            ch.queueDeclare("invoice.generation.queue", true, false, false, args);
            ch.queueDeclare(dlqName, true, false, false, null);
            ch.queueBind("invoice.generation.queue", "articurated.exchange", "invoice.generate");
            ch.queueBind(dlqName, "articurated.dlx", dlqName);
        }

        // Publish the message using the same channel so declaration and publish are consistent
        try (com.rabbitmq.client.Connection conn = cf.newConnection();
             com.rabbitmq.client.Channel ch = conn.createChannel()) {
            String exchange = "articurated.exchange";
            String routingKey = "invoice.generate";
            // publish
            ch.basicPublish(exchange, routingKey, null, payload.getBytes());

            // Wait briefly for message routing
            Thread.sleep(500);

            // Fetch from main queue and reject it to simulate a consumer failure -> dead-letter
            com.rabbitmq.client.GetResponse mainResp = ch.basicGet("invoice.generation.queue", false);
            assertThat(mainResp).isNotNull();
            long deliveryTag = mainResp.getEnvelope().getDeliveryTag();

            // Reject message (requeue=false) so broker dead-letters it to the configured DLX
            ch.basicReject(deliveryTag, false);

            // Wait briefly for dead-letter routing
            Thread.sleep(500);

            com.rabbitmq.client.GetResponse resp = ch.basicGet(dlqName, true);
            assertThat(resp).isNotNull();
            String body = new String(resp.getBody());
            assertThat(body).contains("12345");
        }
    }
}
