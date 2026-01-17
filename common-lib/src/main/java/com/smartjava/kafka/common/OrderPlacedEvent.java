package com.smartjava.kafka.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPlacedEvent {
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String status;
}
