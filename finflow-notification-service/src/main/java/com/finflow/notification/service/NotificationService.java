package com.finflow.notification.service;

import com.finflow.notification.entity.Notification;
import com.finflow.notification.event.TransactionEvent;
import com.finflow.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // ─── Process incoming Kafka event and save notification ───
    public void processTransactionNotification(TransactionEvent event) {

        String message = buildMessage(event);

        Notification notification = Notification.builder()
                .transactionRef(event.getTransactionRef())
                .fromAccountNumber(event.getFromAccountNumber())
                .toAccountNumber(event.getToAccountNumber())
                .amount(event.getAmount().toString())
                .type(event.getType())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        // ─── Simulate sending email/SMS (in real system, use JavaMail or Twilio here) ───
        log.info("═══════════════════════════════════════");
        log.info("📧 NOTIFICATION SENT");
        log.info("Transaction Ref : {}", event.getTransactionRef());
        log.info("From Account    : {}", event.getFromAccountNumber());
        log.info("To Account      : {}", event.getToAccountNumber());
        log.info("Amount          : ₹{}", event.getAmount());
        log.info("Status          : {}", event.getStatus());
        log.info("Message         : {}", message);
        log.info("═══════════════════════════════════════");
    }

    // ─── Get notifications for an account ───
    public List<Notification> getNotificationsByAccount(String accountNumber) {
        return notificationRepository
                .findByFromAccountNumberOrToAccountNumberOrderByCreatedAtDesc(
                        accountNumber, accountNumber);
    }

    // ─── Build human readable message ───
    private String buildMessage(TransactionEvent event) {
        if ("SUCCESS".equals(event.getStatus())) {
            return String.format(
                    "Your transfer of ₹%s from account %s to account %s was successful. Ref: %s",
                    event.getAmount(),
                    event.getFromAccountNumber(),
                    event.getToAccountNumber(),
                    event.getTransactionRef()
            );
        } else {
            return String.format(
                    "Your transfer of ₹%s from account %s to account %s failed. Ref: %s",
                    event.getAmount(),
                    event.getFromAccountNumber(),
                    event.getToAccountNumber(),
                    event.getTransactionRef()
            );
        }
    }

}