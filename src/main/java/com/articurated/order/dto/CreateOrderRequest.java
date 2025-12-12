package com.articurated.order.dto;

import com.articurated.order.domain.valueobjects.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    
    @NotBlank
    @Email
    private String customerEmail;
    
    @NotBlank
    private String firstName;
    
    @NotBlank
    private String lastName;
    
    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
    
    @NotNull
    @Valid
    private Address address;
}
