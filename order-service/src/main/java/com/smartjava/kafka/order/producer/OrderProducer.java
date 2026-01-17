package com.smartjava.kafka.order.producer;

import com.smartjava.kafka.common.OrderPlacedEvent;
import com.smartjava.kafka.order.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @org.springframework.transaction.annotation.Transactional
    public void sendOrderEvent(OrderPlacedEvent event) {
        log.info("Producing message: {}. (orderId={})", event, event.getOrderId());

        CompletableFuture<SendResult<String, OrderPlacedEvent>> future = kafkaTemplate
                .send(KafkaConfig.ORDER_PLACED_TOPIC, event.getOrderId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Message sent successfully. (topic={}, partition={}, offset={})",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send message. (orderId={}, error={})",
                        event.getOrderId(), ex.getMessage());
            }
        });
    }
}
