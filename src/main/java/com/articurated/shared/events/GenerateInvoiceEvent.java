package com.articurated.shared.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerateInvoiceEvent {
    private Long orderId;
}
