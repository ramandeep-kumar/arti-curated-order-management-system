package com.articurated.returns.repository;

import com.articurated.returns.domain.ReturnStateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnStateHistoryRepository extends JpaRepository<ReturnStateHistory, Long> {
    
    List<ReturnStateHistory> findByReturnEntityIdOrderByChangedAtDesc(Long returnId);
}
