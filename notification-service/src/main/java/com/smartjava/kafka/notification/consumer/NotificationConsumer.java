package com.smartjava.kafka.notification.consumer;

import com.smartjava.kafka.common.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Service responsible for consuming Order events and sending notifications
 * (Email/SMS).
 * 
 * <p>
 * Architectural Case Study:
 * <b>Competing Consumers vs Multiple Consumer Groups</b>
 * <ul>
 * <li>Payment Service and Notification Service have <b>different</b> group
 * IDs.</li>
 * <li>This means both services receive a COPY of every message. This is
 * <b>Fan-out Architecture</b>.</li>
 * <li>If we had two Notification Services with the <b>same</b> group ID, they
 * would SHARE the load. This is <b>Competing Consumers</b>.</li>
 * </ul>
 */
@Service
@Slf4j
public class NotificationConsumer {

    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public NotificationConsumer(org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Consumes events to trigger notifications.
     */
    @KafkaListener(topics = "order-placed-events", groupId = "notification-group")
    public void consume(@Payload OrderPlacedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[Kafka Consumer] Message Received by NotificationGroup. (partition={}, offset={}, orderId={})",
                partition, offset, event.getOrderId());

        // Push the event to the WebSocket topic for real-time UI updates
        messagingTemplate.convertAndSend("/topic/notifications", event);

        log.info("[Kafka Consumer] Pushed notification to WebSocket. (orderId={})", event.getOrderId());
    }
}
