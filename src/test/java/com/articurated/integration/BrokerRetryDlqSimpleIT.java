package com.articurated.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
// ...existing imports...
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = BrokerRetryDlqSimpleIT.TestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
public class BrokerRetryDlqSimpleIT {

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.8-management");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", () -> rabbit.getAmqpPort());
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }

    @Configuration
    static class TestConfig {
        @Bean
        public CachingConnectionFactory connectionFactory() {
            CachingConnectionFactory cf = new CachingConnectionFactory(rabbit.getHost(), rabbit.getAmqpPort());
            cf.setUsername(rabbit.getAdminUsername());
            cf.setPassword(rabbit.getAdminPassword());
            return cf;
        }

        @Bean
        public RabbitTemplate rabbitTemplate(CachingConnectionFactory cf) {
            return new RabbitTemplate(cf);
        }

        @Bean
        public RabbitAdmin rabbitAdmin(CachingConnectionFactory cf) {
            return new RabbitAdmin(cf);
        }
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin admin;

    @Test
    public void message_should_route_through_retry_queues_to_dlq() throws Exception {
        // declare exchange and queues with short TTLs
        DirectExchange exchange = new DirectExchange("articurated.exchange");
        admin.declareExchange(exchange);

    // main queue dead-letters to retry exchange; set short TTL so message expires and moves to retry exchange
    Queue main = QueueBuilder.durable("invoice.generation.queue")
        .withArgument("x-message-ttl", 500)
        .withArgument("x-dead-letter-exchange", "articurated.retry.exchange")
        .withArgument("x-dead-letter-routing-key", "invoice.generate.1000")
        .build();
        admin.declareQueue(main);

    Queue retry1 = QueueBuilder.durable("invoice.retry.1000")
        .withArgument("x-message-ttl", 1000)
        // after TTL expire, route to next retry via retry exchange
        .withArgument("x-dead-letter-exchange", "articurated.retry.exchange")
        .withArgument("x-dead-letter-routing-key", "invoice.generate.2000")
        .build();
        admin.declareQueue(retry1);

    Queue retry2 = QueueBuilder.durable("invoice.retry.2000")
        .withArgument("x-message-ttl", 2000)
        // after TTL expire, route to final retry which will send to DLQ
        .withArgument("x-dead-letter-exchange", "articurated.retry.exchange")
        .withArgument("x-dead-letter-routing-key", "invoice.generate.final")
        .build();
        admin.declareQueue(retry2);

    Queue dlq = QueueBuilder.durable("invoice.generation.dlq").build();

    Queue retryFinal = QueueBuilder.durable("invoice.retry.final")
        .withArgument("x-message-ttl", 500)
        .withArgument("x-dead-letter-exchange", "articurated.dlx")
        .withArgument("x-dead-letter-routing-key", "invoice.generation.dlq")
        .build();
        admin.declareQueue(dlq);

        DirectExchange retryExchange = new DirectExchange("articurated.retry.exchange");
        admin.declareExchange(retryExchange);

        DirectExchange dlx = new DirectExchange("articurated.dlx");
        admin.declareExchange(dlx);

        admin.declareBinding(BindingBuilder.bind(main).to(exchange).with("invoice.generate"));
    admin.declareBinding(BindingBuilder.bind(retry1).to(retryExchange).with("invoice.generate.1000"));
    admin.declareBinding(BindingBuilder.bind(retry2).to(retryExchange).with("invoice.generate.2000"));
    admin.declareQueue(retryFinal);
    admin.declareBinding(BindingBuilder.bind(retryFinal).to(retryExchange).with("invoice.generate.final"));
    admin.declareBinding(BindingBuilder.bind(dlq).to(dlx).with("invoice.generation.dlq"));

        // send a message to main exchange -> main queue
        rabbitTemplate.convertAndSend("articurated.exchange", "invoice.generate", "payload-999");

        // message should traverse retry queues due to no consumer; wait for DLQ
        org.springframework.amqp.core.Message received = null;
        int tries = 0;
        while (tries < 30) {
            received = rabbitTemplate.receive("invoice.generation.dlq");
            if (received != null) break;
            Thread.sleep(500);
            tries++;
        }

        Assertions.assertNotNull(received, "Expected a message in DLQ after retries");
        String body = new String(received.getBody());
        Assertions.assertTrue(body.contains("payload-999"));
    }
}
