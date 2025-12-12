package com.articurated.shared.events;

import com.articurated.messaging.producer.MessageProducer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

class GenerateInvoiceEventMapperTest {

    @Test
    void supports_returnsTrueForGenerateInvoiceEvent() {
    MessageProducer producer = mock(MessageProducer.class);
        GenerateInvoiceEventMapper mapper = new GenerateInvoiceEventMapper(producer);

        GenerateInvoiceEvent event = new GenerateInvoiceEvent(123L);
        assertThat(mapper.supports(event)).isTrue();
    }

    @Test
    void supports_returnsFalseForOtherEvent() {
    MessageProducer producer = mock(MessageProducer.class);
        GenerateInvoiceEventMapper mapper = new GenerateInvoiceEventMapper(producer);

        Object other = new Object();
        assertThat(mapper.supports(other)).isFalse();
    }

    @Test
    void mapAndSend_callsMessageProducer() {
    MessageProducer producer = mock(MessageProducer.class);
        GenerateInvoiceEventMapper mapper = new GenerateInvoiceEventMapper(producer);

        GenerateInvoiceEvent event = new GenerateInvoiceEvent(456L);
        mapper.mapAndSend(event);

        verify(producer, times(1)).sendInvoiceGenerationMessage(456L);
    }
}
