package com.articurated.returns.adapter.persistence;

import com.articurated.returns.domain.Return;
import com.articurated.returns.port.ReturnPersistencePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SpringDataReturnPersistenceAdapter.class)
public class ReturnPersistenceAdapterIT {

    @Autowired
    private ReturnPersistencePort port;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saveAndFindById_shouldPersistAndRetrieveReturn() {
    com.articurated.order.domain.Order dummyOrder = com.articurated.order.domain.Order.builder()
            .orderNumber("IT-ORD-REF")
            .customerEmail("itref@example.com")
            .customerFirstName("Ref")
            .customerLastName("Tester")
            .street("123 Ref St")
            .city("TestCity")
            .state("TS")
            .zipCode("00000")
            .country("USA")
            .subtotal(new java.math.BigDecimal("10.00"))
            .tax(new java.math.BigDecimal("0.00"))
            .shipping(new java.math.BigDecimal("0.00"))
            .total(new java.math.BigDecimal("10.00"))
            .currentState(com.articurated.order.domain.OrderState.DELIVERED)
            .build();

        // persist the referenced order so the return FK won't be transient
        entityManager.persist(dummyOrder);
        entityManager.flush();

        Return r = Return.builder()
            .returnNumber("IT-RET-1")
            .order(dummyOrder)
            .reason("defect")
            .currentState(com.articurated.returns.domain.ReturnState.REQUESTED)
            .build();

        Return saved = port.save(r);
        assertThat(saved.getId()).isNotNull();

        Return found = port.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getReturnNumber()).isEqualTo("IT-RET-1");
    }
}
