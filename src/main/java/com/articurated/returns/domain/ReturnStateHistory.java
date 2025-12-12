package com.articurated.returns.domain;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "return_state_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnStateHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    @JsonIgnore
    private Return returnEntity;
    
    @Enumerated(EnumType.STRING)
    private ReturnState fromState;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnState toState;
    
    @Column(length = 500)
    private String reason;
    
    @Column(nullable = false)
    private String changedBy;
    
    @Column(nullable = false)
    private LocalDateTime changedAt;
}
