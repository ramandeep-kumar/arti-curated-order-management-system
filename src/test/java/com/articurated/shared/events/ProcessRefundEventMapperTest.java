package com.articurated.shared.events;

import com.articurated.messaging.producer.MessageProducer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

class ProcessRefundEventMapperTest {

    @Test
    void supports_returnsTrueForProcessRefundEvent() {
    MessageProducer producer = mock(MessageProducer.class);
        ProcessRefundEventMapper mapper = new ProcessRefundEventMapper(producer);

        ProcessRefundEvent event = new ProcessRefundEvent(321L);
        assertThat(mapper.supports(event)).isTrue();
    }

    @Test
    void supports_returnsFalseForOtherEvent() {
    MessageProducer producer = mock(MessageProducer.class);
        ProcessRefundEventMapper mapper = new ProcessRefundEventMapper(producer);

        Object other = new Object();
        assertThat(mapper.supports(other)).isFalse();
    }

    @Test
    void mapAndSend_callsMessageProducer() {
    MessageProducer producer = mock(MessageProducer.class);
        ProcessRefundEventMapper mapper = new ProcessRefundEventMapper(producer);

        ProcessRefundEvent event = new ProcessRefundEvent(654L);
        mapper.mapAndSend(event);

        verify(producer, times(1)).sendRefundProcessingMessage(654L);
    }
}
