package com.articurated.order.controller;

import com.articurated.order.dto.OrderItemRequest;
import com.articurated.order.service.app.OrderWriteService;
import com.articurated.shared.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerIntegrationTest {

    MockMvc mvc;
    ObjectMapper mapper = new ObjectMapper();

    @Mock OrderWriteService writeService;
    @Mock com.articurated.order.service.app.OrderReadService readService;
    @Mock com.articurated.order.mapper.OrderResponseMapper orderResponseMapper;

    @BeforeEach
    void setUp() {
        OrderController controller = new OrderController(readService, writeService, orderResponseMapper);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new com.articurated.shared.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void postItems_success_returnsUpdatedOrder() throws Exception {
        OrderItemRequest req = OrderItemRequest.builder().productName("X").price(BigDecimal.valueOf(10)).quantity(1).build();
        when(writeService.addItems(ArgumentMatchers.eq(1L), ArgumentMatchers.anyList())).thenReturn(new com.articurated.order.domain.Order());

        mvc.perform(post("/api/orders/1/items").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(List.of(req))))
                .andExpect(status().isOk());
    }

    @Test
    void postItems_businessException_returns409() throws Exception {
        OrderItemRequest req = OrderItemRequest.builder().productName("X").price(BigDecimal.valueOf(10)).quantity(1).build();
        when(writeService.addItems(ArgumentMatchers.eq(2L), ArgumentMatchers.anyList())).thenThrow(new BusinessException("Cannot add items"));

        mvc.perform(post("/api/orders/2/items").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(List.of(req))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot add items"));
    }
}
