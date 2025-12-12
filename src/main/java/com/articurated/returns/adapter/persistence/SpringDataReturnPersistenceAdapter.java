package com.articurated.returns.adapter.persistence;

import com.articurated.returns.domain.Return;
import com.articurated.returns.port.ReturnPersistencePort;
import com.articurated.returns.repository.ReturnRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SpringDataReturnPersistenceAdapter implements ReturnPersistencePort {

    private final ReturnRepository returnRepository;

    public SpringDataReturnPersistenceAdapter(ReturnRepository returnRepository) {
        this.returnRepository = returnRepository;
    }

    @Override
    public Return save(Return entity) {
        return returnRepository.save(entity);
    }

    @Override
    public Optional<Return> findById(Long id) {
        return returnRepository.findById(id);
    }

    @Override
    public Optional<Return> findByIdWithDetails(Long id) {
        return returnRepository.findByIdWithDetails(id);
    }

    @Override
    public List<Return> findByOrderIdOrderByCreatedAtDesc(Long orderId) {
        return returnRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    @Override
    public List<Return> findByCurrentStateOrderByCreatedAtDesc(com.articurated.returns.domain.ReturnState state) {
        return returnRepository.findByCurrentStateOrderByCreatedAtDesc(state);
    }

    @Override
    public long countByCurrentState(com.articurated.returns.domain.ReturnState state) {
        return returnRepository.countByCurrentState(state);
    }
}
