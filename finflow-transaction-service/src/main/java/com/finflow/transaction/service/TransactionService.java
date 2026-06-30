package com.finflow.transaction.service;

import com.finflow.transaction.client.AccountServiceClient;
import com.finflow.transaction.dto.AccountResponse;
import com.finflow.transaction.dto.BalanceUpdateRequest;
import com.finflow.transaction.dto.TransactionResponse;
import com.finflow.transaction.dto.TransferRequest;
import com.finflow.transaction.entity.Transaction;
import com.finflow.transaction.entity.TransactionStatus;
import com.finflow.transaction.entity.TransactionType;
import com.finflow.transaction.event.KafkaTopics;
import com.finflow.transaction.event.TransactionEvent;
import com.finflow.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ─── Fund Transfer (the core operation) ───
    @Transactional
    public TransactionResponse transferFunds(TransferRequest request) {

        // ─── Step 1: Idempotency check - prevent duplicate transfers ───
        if (transactionRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            Transaction existing = transactionRepository
                    .findByIdempotencyKey(request.getIdempotencyKey())
                    .orElseThrow();
            log.info("Duplicate transfer detected for idempotency key: {}", request.getIdempotencyKey());
            return mapToResponse(existing);
        }

        // ─── Step 2: Basic validation ───
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        // ─── Step 3: Create a PENDING transaction record first ───
        Transaction transaction = Transaction.builder()
                .transactionRef(generateTransactionRef())
                .idempotencyKey(request.getIdempotencyKey())
                .fromAccountNumber(request.getFromAccountNumber())
                .toAccountNumber(request.getToAccountNumber())
                .amount(request.getAmount())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .remarks(request.getRemarks())
                .build();

        transaction = transactionRepository.save(transaction);

        try {
            // ─── Step 4: Fetch sender account ───
            AccountResponse fromAccount = accountServiceClient
                    .getAccountByNumber(request.getFromAccountNumber());

            // ─── Step 5: Fetch receiver account ───
            AccountResponse toAccount = accountServiceClient
                    .getAccountByNumber(request.getToAccountNumber());

            // ─── Step 6: Withdraw from sender ───
            accountServiceClient.withdraw(
                    fromAccount.getId(),
                    BalanceUpdateRequest.builder().amount(request.getAmount()).build()
            );

            // ─── Step 7: Deposit to receiver ───
            accountServiceClient.deposit(
                    toAccount.getId(),
                    BalanceUpdateRequest.builder().amount(request.getAmount()).build()
            );

            // ─── Step 8: Mark transaction as SUCCESS ───
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction = transactionRepository.save(transaction);

            // ─── Step 9: Publish Kafka event ───
            publishTransactionEvent(transaction);

            log.info("Transfer successful: {}", transaction.getTransactionRef());

        } catch (Exception e) {
            // ─── If anything fails, mark transaction as FAILED ───
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transaction = transactionRepository.save(transaction);

            publishTransactionEvent(transaction);

            log.error("Transfer failed: {}", e.getMessage());
            throw new RuntimeException("Transfer failed: " + e.getMessage());
        }

        return mapToResponse(transaction);
    }

    // ─── Get Transaction History for an Account ───
    public List<TransactionResponse> getTransactionHistory(String accountNumber) {
        return transactionRepository
                .findByFromAccountNumberOrToAccountNumberOrderByCreatedAtDesc(
                        accountNumber, accountNumber)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Single Transaction ───
    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return mapToResponse(transaction);
    }

    // ─── Publish event to Kafka ───
    private void publishTransactionEvent(Transaction transaction) {
        TransactionEvent event = TransactionEvent.builder()
                .transactionRef(transaction.getTransactionRef())
                .fromAccountNumber(transaction.getFromAccountNumber())
                .toAccountNumber(transaction.getToAccountNumber())
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .status(transaction.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(KafkaTopics.TRANSACTION_EVENTS, transaction.getTransactionRef(), event);
        log.info("Published Kafka event for transaction: {}", transaction.getTransactionRef());
    }

    // ─── Generate unique transaction reference ───
    private String generateTransactionRef() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TXN" + datePart + randomPart;
    }

    // ─── Map Entity to Response DTO ───
    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionRef(transaction.getTransactionRef())
                .fromAccountNumber(transaction.getFromAccountNumber())
                .toAccountNumber(transaction.getToAccountNumber())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .remarks(transaction.getRemarks())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

}
