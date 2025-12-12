package com.articurated.integration;

import com.articurated.order.domain.valueobjects.Address;
import com.articurated.order.dto.CreateOrderRequest;
import com.articurated.order.dto.OrderItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Test
    void createOrder_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
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

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderNumber").exists())
                .andExpect(jsonPath("$.currentState").value("PENDING_PAYMENT"));
    }

    @Test
    void getOrder_ExistingOrder_ReturnsOrder() throws Exception {
        // Given
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        CreateOrderRequest createRequest = CreateOrderRequest.builder()
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

        // Create order first
        String createResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract order ID from response
        String orderId = objectMapper.readTree(createResponse).get("id").asText();

        // When & Then - Get the created order
        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.orderNumber").exists());
    }

    @Test
    void transitionOrder_ValidTransition_ReturnsUpdatedOrder() throws Exception {
        // Given
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        CreateOrderRequest createRequest = CreateOrderRequest.builder()
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

        // Create order first
        String createResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract order ID from response
        String orderId = objectMapper.readTree(createResponse).get("id").asText();

        // When & Then - Transition order state
        mockMvc.perform(put("/api/orders/" + orderId + "/transition")
                        .param("event", "PAYMENT_RECEIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.currentState").value("PAID"));
    }
}
