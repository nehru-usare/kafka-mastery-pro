package com.smartjava.kafka.notification.consumer;

import com.smartjava.kafka.common.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumer {

    @KafkaListener(topics = "order-placed-events", groupId = "notification-group")
    public void consume(@Payload OrderPlacedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Notification service received message: {}. (partition={}, offset={}, orderId={})",
                event, partition, offset, event.getOrderId());

        log.info("Sending notification for order: {}. (type=EMAIL)", event.getOrderId());
    }
}
