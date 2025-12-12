package com.articurated.returns.adapter.persistence;

import com.articurated.returns.domain.ReturnStateHistory;
import com.articurated.returns.port.ReturnStateHistoryPersistencePort;
import com.articurated.returns.repository.ReturnStateHistoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringDataReturnStateHistoryPersistenceAdapter implements ReturnStateHistoryPersistencePort {

    private final ReturnStateHistoryRepository historyRepository;

    public SpringDataReturnStateHistoryPersistenceAdapter(ReturnStateHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public ReturnStateHistory save(ReturnStateHistory history) {
        return historyRepository.save(history);
    }

    @Override
    public List<ReturnStateHistory> findByReturnEntityIdOrderByChangedAtDesc(Long returnId) {
        return historyRepository.findByReturnEntityIdOrderByChangedAtDesc(returnId);
    }
}
