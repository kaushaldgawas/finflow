package com.finflow.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")   // ← MongoDB collection name
public class Notification {

    @Id
    private String id;                    // MongoDB uses String IDs by default

    private String transactionRef;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String amount;
    private String type;
    private String status;
    private String message;               // Human readable notification message
    private LocalDateTime createdAt;

}