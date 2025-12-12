package com.articurated.returns.repository;

import com.articurated.returns.domain.Return;
import com.articurated.returns.domain.ReturnState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {
    
    @Query("SELECT r FROM Return r LEFT JOIN FETCH r.stateHistory WHERE r.id = :id")
    Optional<Return> findByIdWithDetails(@Param("id") Long id);
    
    Optional<Return> findByReturnNumber(String returnNumber);
    
    List<Return> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    
    List<Return> findByCurrentStateOrderByCreatedAtDesc(ReturnState state);
    
    long countByCurrentState(ReturnState state);
}
