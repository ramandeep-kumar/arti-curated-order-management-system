package com.articurated.order.mapper;

import com.articurated.order.domain.Order;
import com.articurated.order.dto.OrderItemResponse;
import com.articurated.order.dto.OrderResponse;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OrderResponseMapperImpl implements OrderResponseMapper {

    @Override
    public OrderResponse toResponse(Order order) {
    if (order == null) return null;

    List<OrderItemResponse> items = (order.getItems() != null && !order.getItems().isEmpty())
        ? order.getItems().stream().map(OrderItemResponse::from).collect(Collectors.toList())
        : List.of();

    OrderResponse.OrderAmountResponse amount = OrderResponse.OrderAmountResponse.builder()
        .subtotal(order.getSubtotal())
        .tax(order.getTax())
        .shipping(order.getShipping())
        .total(order.getTotal())
        .build();

    OrderResponse.AddressResponse address = OrderResponse.AddressResponse.builder()
        .street(order.getStreet())
        .city(order.getCity())
        .state(order.getState())
        .zipCode(order.getZipCode())
        .country(order.getCountry())
        .build();

    return OrderResponse.builder()
        .id(order.getId())
        .orderNumber(order.getOrderNumber())
        .customerEmail(order.getCustomerEmail())
        .customerName(order.getFullName())
        .currentState(order.getCurrentState())
        .amount(amount)
        .items(items)
        .address(address)
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .build();
    }
}
