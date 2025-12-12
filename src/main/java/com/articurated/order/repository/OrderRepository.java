package com.articurated.order.repository;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Using default JPA findById method to avoid Hibernate collection issues
    // Optional<Order> findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.stateHistory WHERE o.id = :id")
    Optional<Order> findByIdWithStateHistory(@Param("id") Long id);
    
    // Method to find order with all related data loaded
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items LEFT JOIN FETCH o.stateHistory WHERE o.id = :id")
    Optional<Order> findByIdWithAllDetails(@Param("id") Long id);
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);
    
    Page<Order> findByCurrentState(OrderState state, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.customerEmail = :customerEmail AND o.currentState = :state")
    List<Order> findByCustomerEmailAndState(@Param("customerEmail") String customerEmail, 
                                          @Param("state") OrderState state);
    
    long countByCurrentState(OrderState state);
}
