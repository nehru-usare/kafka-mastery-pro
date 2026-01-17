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

/**
 * Service responsible for consuming Order events and processing payments.
 * 
 * <p>
 * Architectural Significance:
 * <ul>
 * <li><b>Scalability:</b> Multiple instances of this service form a "Consumer
 * Group".
 * Kafka distributes partitions among them.</li>
 * <li><b>Fault Tolerance:</b> If this consumer fails, another instance in the
 * same group will take over its partitions (Rebalancing).</li>
 * </ul>
 */
@Service
@Slf4j
public class PaymentConsumer {

    /**
     * Consumes messages from the 'order-placed-events' topic.
     * 
     * <p>
     * <b>Kafka Features Used:</b>
     * <ul>
     * <li><b>Consumer Groups:</b> By setting {@code groupId}, Kafka ensures that
     * only ONE consumer in this group processes each message.</li>
     * <li><b>Non-Blocking Retries:</b> {@code @RetryableTopic} creates helper
     * topics
     * (retry-0, retry-1...) to handle failures without blocking other messages
     * in the main topic. This is a "Production-Grade" pattern.</li>
     * <li><b>DLT (Dead Letter Topic):</b> If all 4 attempts fail, the message is
     * automatically moved to a DLT topic for manual inspection.</li>
     * </ul>
     * 
     * @param event     The record payload (deserialized JSON)
     * @param partition The partition this message came from
     * @param offset    The unique position of this message in the partition
     */
    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 1000, multiplier = 2.0), autoCreateTopics = "true", topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE, dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "order-placed-events", groupId = "payment-group")
    public void consume(@Payload OrderPlacedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info(
                "[Kafka Consumer] Message Received by PaymentGroup. (topic=order-placed-events, partition={}, offset={}, orderId={})",
                partition, offset, event.getOrderId());

        // Demonstration of Failure Logic
        if ("FAIL".equalsIgnoreCase(event.getStatus())) {
            log.warn(
                    "[Kafka Consumer] Simulating processing failure for order: {}. (Action: Throwing exception to trigger Retry)",
                    event.getOrderId());
            throw new RuntimeException("Payment processing failed internally!");
        }

        log.info("[Kafka Consumer] Payment Processed Successfully. (orderId={}, status=SUCCESS)", event.getOrderId());
    }

    /**
     * Handler for messages that have exhausted all retry attempts.
     * In a real system, you would save this to a database or alert an admin.
     */
    @DltHandler
    public void handleDlt(OrderPlacedEvent event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("[Kafka DLT] CRITICAL: Message moved to Dead Letter Topic. (sourceTopic={}, orderId={}, payload={})",
                topic, event.getOrderId(), event);
    }
}
