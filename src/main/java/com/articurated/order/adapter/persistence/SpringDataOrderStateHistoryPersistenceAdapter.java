package com.articurated.order.adapter.persistence;

import com.articurated.order.domain.OrderStateHistory;
import com.articurated.order.port.OrderStateHistoryPersistencePort;
import com.articurated.order.repository.OrderStateHistoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringDataOrderStateHistoryPersistenceAdapter implements OrderStateHistoryPersistencePort {

    private final OrderStateHistoryRepository historyRepository;

    public SpringDataOrderStateHistoryPersistenceAdapter(OrderStateHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public OrderStateHistory save(OrderStateHistory history) {
        return historyRepository.save(history);
    }

    @Override
    public List<OrderStateHistory> findByOrderIdOrderByChangedAtDesc(Long orderId) {
        return historyRepository.findByOrderIdOrderByChangedAtDesc(orderId);
    }
}
