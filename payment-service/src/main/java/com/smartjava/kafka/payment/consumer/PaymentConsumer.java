package com.smartjava.kafka.payment.consumer;

import com.smartjava.kafka.common.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentConsumer {

    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 1000, multiplier = 2.0), autoCreateTopics = "true", topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE, dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "order-placed-events", groupId = "payment-group")
    public void consume(@Payload OrderPlacedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Payment service received message: {}. (partition={}, offset={}, orderId={})",
                event, partition, offset, event.getOrderId());

        // Simulate a failure for specific orders to demonstrate DLT
        if ("FAIL".equalsIgnoreCase(event.getStatus())) {
            log.error("Simulating payment failure for order: {}. (Retrying...)", event.getOrderId());
            throw new RuntimeException("Payment processing failed!");
        }

        log.info("Processing payment for order: {}. (status=SUCCESS)", event.getOrderId());
    }

    @DltHandler
    public void handleDlt(OrderPlacedEvent event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Event sent to DLT: {}. (topic={}, orderId={})", event, topic, event.getOrderId());
    }
}
