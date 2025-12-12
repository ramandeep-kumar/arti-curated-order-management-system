package com.articurated.returns.port;

import com.articurated.returns.domain.ReturnStateHistory;

import java.util.List;

public interface ReturnStateHistoryPersistencePort {
    ReturnStateHistory save(ReturnStateHistory history);
    List<ReturnStateHistory> findByReturnEntityIdOrderByChangedAtDesc(Long returnId);
}
