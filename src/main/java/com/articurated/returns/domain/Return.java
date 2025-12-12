package com.articurated.returns.domain;

import com.articurated.order.domain.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Return {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String returnNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;
    
    @Column(nullable = false, length = 500)
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnState currentState = ReturnState.REQUESTED;
    
    @Column(length = 100)
    private String approvedBy;
    
    @Column(length = 100)
    private String trackingNumber;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "returnEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReturnStateHistory> stateHistory = new ArrayList<>();
    
    public void addStateHistory(ReturnState fromState, ReturnState toState, String reason) {
        ReturnStateHistory history = ReturnStateHistory.builder()
            .returnEntity(this)
            .fromState(fromState)
            .toState(toState)
            .reason(reason)
            .changedBy("SYSTEM")
            .changedAt(LocalDateTime.now())
            .build();
        this.stateHistory.add(history);
    }
}
