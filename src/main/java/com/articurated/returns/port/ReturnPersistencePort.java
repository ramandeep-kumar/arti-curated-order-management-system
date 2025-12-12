package com.articurated.returns.port;

import com.articurated.returns.domain.Return;
import com.articurated.returns.domain.ReturnState;

import java.util.List;
import java.util.Optional;

public interface ReturnPersistencePort {
    Return save(Return entity);
    Optional<Return> findById(Long id);
    Optional<Return> findByIdWithDetails(Long id);
    List<Return> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    List<Return> findByCurrentStateOrderByCreatedAtDesc(ReturnState state);
    long countByCurrentState(ReturnState state);
}
