package com.articurated.order.dto;

import com.articurated.order.domain.OrderState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    
    private Long id;
    private String orderNumber;
    private String customerEmail;
    private String customerName;
    private OrderState currentState;
    private OrderAmountResponse amount;
    private List<OrderItemResponse> items;
    private AddressResponse address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Static factory removed: use `com.articurated.order.mapper.OrderResponseMapper#toResponse(Order)` instead
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderAmountResponse {
        private BigDecimal subtotal;
        private BigDecimal tax;
        private BigDecimal shipping;
        private BigDecimal total;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressResponse {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
    }
}
