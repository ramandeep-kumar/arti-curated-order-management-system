package com.articurated.order.domain;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_state_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStateHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;
    
    @Enumerated(EnumType.STRING)
    private OrderState fromState;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderState toState;
    
    @Column(length = 500)
    private String reason;
    
    @Column(nullable = false)
    private String changedBy;
    
    @Column(nullable = false)
    private LocalDateTime changedAt;
}
