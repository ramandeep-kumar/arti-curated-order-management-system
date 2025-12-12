package com.articurated.order.repository;

import com.articurated.order.domain.OrderStateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStateHistoryRepository extends JpaRepository<OrderStateHistory, Long> {
    
    List<OrderStateHistory> findByOrderIdOrderByChangedAtDesc(Long orderId);
}
