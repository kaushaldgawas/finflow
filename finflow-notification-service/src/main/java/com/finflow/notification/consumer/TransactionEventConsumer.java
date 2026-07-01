package com.finflow.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.notification.event.TransactionEvent;
import com.finflow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "transaction-events",
            groupId = "finflow-notification-group"
    )
    public void consumeTransactionEvent(String message) {
        try {
            log.info("Raw Kafka message received: {}", message);
            TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);
            log.info("Deserialized event for transaction: {}", event.getTransactionRef());
            notificationService.processTransactionNotification(event);
        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", e.getMessage());
        }
    }

}