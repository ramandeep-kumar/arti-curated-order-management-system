package com.articurated.order.domain;

import com.articurated.returns.domain.Return;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;
    
    // Customer information (flattened)
    @Column(name = "email", nullable = false)
    private String customerEmail;
    
    @Column(name = "first_name", nullable = false)
    private String customerFirstName;
    
    @Column(name = "last_name", nullable = false)
    private String customerLastName;
    
    // Address information (flattened)
    @Column(name = "street")
    private String street;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "state")
    private String state;
    
    @Column(name = "zip_code")
    private String zipCode;
    
    @Column(name = "country")
    private String country;
    
    // Amount information (flattened)
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(name = "tax", nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;
    
    @Column(name = "shipping", nullable = false, precision = 10, scale = 2)
    private BigDecimal shipping;
    
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_state", nullable = false)
    private OrderState currentState = OrderState.PENDING_PAYMENT;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<OrderStateHistory> stateHistory = new HashSet<>();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Return> returns = new ArrayList<>();
    
    // Business methods
    public boolean canBeReturned() {
    if (this.currentState != OrderState.DELIVERED) return false;
    if (this.createdAt == null) return false;
    return this.createdAt.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    public void addStateHistory(OrderState fromState, OrderState toState, String reason) {
        OrderStateHistory history = OrderStateHistory.builder()
            .order(this)
            .fromState(fromState)
            .toState(toState)
            .reason(reason)
            .changedBy("SYSTEM")
            .changedAt(LocalDateTime.now())
            .build();
        this.stateHistory.add(history);
    }
    
    // Helper methods for backward compatibility
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public String getCustomerFirstName() {
        return customerFirstName;
    }
    
    public String getCustomerLastName() {
        return customerLastName;
    }
    
    public String getFullName() {
        return customerFirstName + " " + customerLastName;
    }
    
    public String getAddress() {
        return street + ", " + city + ", " + state + " " + zipCode + ", " + country;
    }
}
