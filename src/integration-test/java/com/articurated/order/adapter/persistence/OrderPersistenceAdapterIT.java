package com.articurated.order.adapter.persistence;

import com.articurated.order.domain.Order;
import com.articurated.order.port.OrderPersistencePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SpringDataOrderPersistenceAdapter.class)
public class OrderPersistenceAdapterIT {

    @Autowired
    private OrderPersistencePort port;

    @Test
    void saveAndFindById_shouldPersistAndRetrieveOrder() {
        Order o = Order.builder()
            .orderNumber("IT-ORD-1")
            .customerEmail("itorder@example.com")
            .customerFirstName("IT")
            .customerLastName("Tester")
            .street("123 Test St")
            .city("TestCity")
            .state("TS")
            .zipCode("00000")
            .country("USA")
            .subtotal(new java.math.BigDecimal("10.00"))
            .tax(new java.math.BigDecimal("0.00"))
            .shipping(new java.math.BigDecimal("0.00"))
            .total(new java.math.BigDecimal("10.00"))
            .currentState(com.articurated.order.domain.OrderState.PENDING_PAYMENT)
            .build();

        Order saved = port.save(o);
        assertThat(saved.getId()).isNotNull();

        Order found = port.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getOrderNumber()).isEqualTo("IT-ORD-1");
    }
}
