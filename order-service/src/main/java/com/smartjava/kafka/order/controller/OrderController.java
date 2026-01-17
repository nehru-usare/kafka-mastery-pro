package com.smartjava.kafka.order.controller;

import com.smartjava.kafka.common.OrderPlacedEvent;
import com.smartjava.kafka.order.producer.OrderProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderProducer orderProducer;

    @PostMapping
    public String placeOrder(@RequestBody OrderPlacedEvent event) {
        if (event.getOrderId() == null) {
            event.setOrderId(UUID.randomUUID().toString());
        }
        orderProducer.sendOrderEvent(event);
        return "Order Placed: " + event.getOrderId();
    }
}
