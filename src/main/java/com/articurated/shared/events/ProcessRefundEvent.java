package com.articurated.shared.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessRefundEvent {
    private Long returnId;
}
