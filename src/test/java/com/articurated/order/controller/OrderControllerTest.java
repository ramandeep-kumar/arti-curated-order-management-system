package com.articurated.order.controller;

import com.articurated.order.domain.Order;
import com.articurated.order.domain.OrderEvent;
import com.articurated.order.domain.OrderState;
import com.articurated.order.domain.valueobjects.Address;
import com.articurated.order.dto.CreateOrderRequest;
import com.articurated.order.dto.OrderItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

        private MockMvc mockMvc;
    
        private ObjectMapper objectMapper = new ObjectMapper();
    
        @Mock
        private com.articurated.order.service.app.OrderReadService orderReadService;

        @Mock
        private com.articurated.order.service.app.OrderWriteService orderWriteService;

                @Mock
                private com.articurated.order.mapper.OrderResponseMapper orderResponseMapper;
    
        @InjectMocks
        private OrderController orderController;
    
        @BeforeEach
        void setup() {
                mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        }

    @Test
    void createOrder_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerEmail("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .items(List.of(OrderItemRequest.builder()
                        .productName("Test Product")
                        .price(new BigDecimal("99.99"))
                        .quantity(1)
                        .build()))
                .address(Address.builder()
                        .street("123 Test St")
                        .city("Test City")
                        .state("TS")
                        .zipCode("12345")
                        .country("USA")
                        .build())
                .build();

        Order mockOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-TEST-001")
                .currentState(OrderState.PENDING_PAYMENT)
                .customerEmail("test@example.com")
                .customerFirstName("John")
                .customerLastName("Doe")
                .build();

        when(orderWriteService.createOrder(any(CreateOrderRequest.class))).thenReturn(mockOrder);
                org.mockito.Mockito.lenient().when(orderResponseMapper.toResponse(mockOrder)).thenReturn(
                        com.articurated.order.dto.OrderResponse.builder()
                                .id(mockOrder.getId())
                                .orderNumber(mockOrder.getOrderNumber())
                                .currentState(mockOrder.getCurrentState())
                                .customerEmail(mockOrder.getCustomerEmail())
                                .build());

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-TEST-001"))
                .andExpect(jsonPath("$.currentState").value("PENDING_PAYMENT"));
    }

    @Test
    void getOrder_ExistingOrder_ReturnsOrder() throws Exception {
        // Given
        Order mockOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-TEST-001")
                .currentState(OrderState.PENDING_PAYMENT)
                .customerEmail("test@example.com")
                .customerFirstName("John")
                .customerLastName("Doe")
                .build();

        when(orderReadService.getOrderById(1L)).thenReturn(mockOrder);
                org.mockito.Mockito.lenient().when(orderResponseMapper.toResponse(mockOrder)).thenReturn(
                        com.articurated.order.dto.OrderResponse.builder()
                                .id(mockOrder.getId())
                                .orderNumber(mockOrder.getOrderNumber())
                                .currentState(mockOrder.getCurrentState())
                                .build());

        // When & Then
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-TEST-001"));
    }

    @Test
    void transitionOrder_ValidTransition_ReturnsUpdatedOrder() throws Exception {
        // Given
        Order mockOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-TEST-001")
                .currentState(OrderState.PAID)
                .customerEmail("test@example.com")
                .customerFirstName("John")
                .customerLastName("Doe")
                .build();

        when(orderWriteService.transitionOrderState(1L, OrderEvent.PAYMENT_RECEIVED)).thenReturn(mockOrder);
                org.mockito.Mockito.lenient().when(orderResponseMapper.toResponse(mockOrder)).thenReturn(
                        com.articurated.order.dto.OrderResponse.builder()
                                .id(mockOrder.getId())
                                .orderNumber(mockOrder.getOrderNumber())
                                .currentState(mockOrder.getCurrentState())
                                .build());

        // When & Then
        mockMvc.perform(put("/api/orders/1/transition")
                        .param("event", "PAYMENT_RECEIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.currentState").value("PAID"));
    }
}
