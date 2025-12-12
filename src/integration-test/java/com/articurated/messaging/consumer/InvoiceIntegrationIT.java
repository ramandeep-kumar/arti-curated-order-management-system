package com.articurated.messaging.consumer;

import com.articurated.order.dto.CreateOrderRequest;
import com.articurated.order.dto.OrderItemRequest;
import com.articurated.order.service.app.OrderAppService;
import com.articurated.order.domain.OrderEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.RabbitMQContainer;

import java.io.File;
import java.time.Duration;

import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
public class InvoiceIntegrationIT {

    static RabbitMQContainer rabbit;

    @Autowired
    private OrderAppService orderAppService;

    @BeforeAll
    static void setup() {
        rabbit = new RabbitMQContainer("rabbitmq:3.12-management-alpine");
        rabbit.withExposedPorts(5672, 15672);
        rabbit.start();
        System.setProperty("spring.rabbitmq.host", rabbit.getHost());
        System.setProperty("spring.rabbitmq.port", Integer.toString(rabbit.getAmqpPort()));
        System.setProperty("spring.rabbitmq.username", rabbit.getAdminUsername());
        System.setProperty("spring.rabbitmq.password", rabbit.getAdminPassword());
    }

    @AfterAll
    static void teardown() {
        if (rabbit != null) rabbit.stop();
    }

    @Test
    void generatesPdfWhenGenerateInvoiceEventPublished() {
        // create a simple order via app service and transition to SHIPPED
        CreateOrderRequest req = CreateOrderRequest.builder()
            .customerEmail("itest@example.com")
            .firstName("IT")
            .lastName("Test")
            .items(java.util.List.of(OrderItemRequest.builder().productName("Prod").price(new java.math.BigDecimal("5.00")).quantity(1).build()))
                    .address(com.articurated.order.domain.valueobjects.Address.builder().street("Street").city("City").state("State").zipCode("ZIP").country("Country").build())
            .build();

        var order = orderAppService.createOrder(req);
        Long orderId = order.getId();

        // simulate payment -> shipping flow: mark as PAID then SHIP_ORDER
        orderAppService.transitionOrderState(orderId, OrderEvent.PAYMENT_RECEIVED);
        orderAppService.transitionOrderState(orderId, OrderEvent.SHIP_ORDER);

        File out = new File(System.getProperty("user.dir") + File.separator + "target" + File.separator + "invoices" + File.separator + "invoice-" + orderId + ".pdf");

        await().atMost(Duration.ofSeconds(30)).until(out::exists);

        // cleanup
        out.delete();
    }
}
