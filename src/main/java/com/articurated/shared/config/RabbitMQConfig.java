package com.articurated.shared.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// no unused imports
@Configuration
public class RabbitMQConfig {
    
    public static final String EXCHANGE = "articurated.exchange";
    public static final String DLX = "articurated.dlx";
    public static final String INVOICE_QUEUE = "invoice.generation.queue";
    public static final String REFUND_QUEUE = "refund.processing.queue";
    public static final String INVOICE_DLQ = "invoice.generation.dlq";
    public static final String REFUND_DLQ = "refund.processing.dlq";
    public static final String INVOICE_ROUTING_KEY = "invoice.generate";
    public static final String REFUND_ROUTING_KEY = "refund.process";
    // Broker-side retry infrastructure
    public static final String RETRY_EXCHANGE = "articurated.retry.exchange";
    public static final String INVOICE_RETRY_QUEUE_PREFIX = "invoice.retry."; // append ttl value
    public static final String REFUND_RETRY_QUEUE_PREFIX = "refund.retry.";
    
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }
    
    @Bean
    public Queue invoiceQueue() {
    // route failed messages into the retry exchange; retry queues will TTL then route back to the main exchange
    return QueueBuilder.durable(INVOICE_QUEUE)
        .withArgument("x-dead-letter-exchange", RETRY_EXCHANGE)
        .withArgument("x-dead-letter-routing-key", INVOICE_ROUTING_KEY + ".10000")
        .build();
    }
    
    @Bean
    public Queue refundQueue() {
    return QueueBuilder.durable(REFUND_QUEUE)
        .withArgument("x-dead-letter-exchange", RETRY_EXCHANGE)
        .withArgument("x-dead-letter-routing-key", REFUND_ROUTING_KEY + ".10000")
        .build();
    }

    @Bean
    public Queue invoiceDeadLetterQueue() {
        return QueueBuilder.durable(INVOICE_DLQ).build();
    }

    @Bean
    public Queue refundDeadLetterQueue() {
        return QueueBuilder.durable(REFUND_DLQ).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX);
    }

    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange(RETRY_EXCHANGE);
    }
    
    @Bean
    public Binding invoiceBinding() {
        return BindingBuilder.bind(invoiceQueue()).to(exchange()).with("invoice.generate");
    }
    
    @Bean
    public Binding refundBinding() {
        return BindingBuilder.bind(refundQueue()).to(exchange()).with("refund.process");
    }

    @Bean
    public Binding invoiceDlqBinding() {
        return BindingBuilder.bind(invoiceDeadLetterQueue()).to(deadLetterExchange()).with(INVOICE_DLQ);
    }

    @Bean
    public Binding refundDlqBinding() {
        return BindingBuilder.bind(refundDeadLetterQueue()).to(deadLetterExchange()).with(REFUND_DLQ);
    }

    // Create retry queues with increasing TTLs and route back to the main exchange
    @Bean
    public Queue invoiceRetryQueue1() {
        return QueueBuilder.durable(INVOICE_RETRY_QUEUE_PREFIX + "10000")
                .withArgument("x-message-ttl", 10000)
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", INVOICE_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue invoiceRetryQueue2() {
        return QueueBuilder.durable(INVOICE_RETRY_QUEUE_PREFIX + "30000")
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", INVOICE_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue refundRetryQueue1() {
        return QueueBuilder.durable(REFUND_RETRY_QUEUE_PREFIX + "10000")
                .withArgument("x-message-ttl", 10000)
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", REFUND_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue refundRetryQueue2() {
        return QueueBuilder.durable(REFUND_RETRY_QUEUE_PREFIX + "30000")
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", REFUND_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding invoiceRetryBinding1() {
        return BindingBuilder.bind(invoiceRetryQueue1()).to(retryExchange()).with(INVOICE_ROUTING_KEY + ".10000");
    }

    @Bean
    public Binding invoiceRetryBinding2() {
        return BindingBuilder.bind(invoiceRetryQueue2()).to(retryExchange()).with(INVOICE_ROUTING_KEY + ".30000");
    }

    @Bean
    public Binding refundRetryBinding1() {
        return BindingBuilder.bind(refundRetryQueue1()).to(retryExchange()).with(REFUND_ROUTING_KEY + ".10000");
    }

    @Bean
    public Binding refundRetryBinding2() {
        return BindingBuilder.bind(refundRetryQueue2()).to(retryExchange()).with(REFUND_ROUTING_KEY + ".30000");
    }
    
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}
