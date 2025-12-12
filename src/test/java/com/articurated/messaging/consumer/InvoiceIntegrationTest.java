package com.articurated.messaging.consumer;

import com.articurated.order.dto.CreateOrderRequest;
import com.articurated.order.dto.OrderItemRequest;
import com.articurated.order.service.app.OrderAppService;
import com.articurated.order.domain.OrderEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;

import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class InvoiceIntegrationTest {

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.12-management-alpine").withExposedPorts(5672, 15672);

    @Autowired
    private OrderAppService orderAppService;

    @DynamicPropertySource
    static void registerRabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", () -> rabbit.getAmqpPort());
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
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
