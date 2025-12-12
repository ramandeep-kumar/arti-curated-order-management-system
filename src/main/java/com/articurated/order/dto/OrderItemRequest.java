package com.articurated.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {
    
    @NotBlank
    @JsonAlias("sku")
    private String productName;
    
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal price;
    
    @NotNull
    @Min(1)
    private Integer quantity;
}
