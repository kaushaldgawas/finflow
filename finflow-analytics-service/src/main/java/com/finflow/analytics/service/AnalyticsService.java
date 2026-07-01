package com.finflow.analytics.service;

import com.finflow.analytics.client.AccountServiceClient;
import com.finflow.analytics.client.TransactionServiceClient;
import com.finflow.analytics.dto.AccountSummary;
import com.finflow.analytics.dto.AnalyticsResponse;
import com.finflow.analytics.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionServiceClient transactionServiceClient;
    private final AccountServiceClient accountServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "analytics:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    // ─── Get Account Analytics (with Redis caching) ───
    public AnalyticsResponse getAccountAnalytics(String accountNumber) {

        String cacheKey = CACHE_PREFIX + accountNumber;

        // ─── Step 1: Check Redis cache first ───
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("Cache HIT for account: {}", accountNumber);
            AnalyticsResponse cachedResponse = convertToAnalyticsResponse(cached);
            cachedResponse.setCachedAt("FROM CACHE");
            return cachedResponse;
        }

        log.info("Cache MISS for account: {} — computing analytics", accountNumber);

        // ─── Step 2: Fetch account details ───
        AccountSummary account = accountServiceClient
                .getAccountByNumber(accountNumber);

        // ─── Step 3: Fetch transaction history ───
        List<TransactionResponse> transactions = transactionServiceClient
                .getTransactionHistory(accountNumber);

        // ─── Step 4: Compute analytics ───
        AnalyticsResponse analytics = computeAnalytics(accountNumber, transactions);

        // ─── Step 5: Save to Redis cache ───
        redisTemplate.opsForValue().set(cacheKey, analytics, CACHE_TTL);
        log.info("Analytics cached for account: {} (TTL: 10 minutes)", accountNumber);

        return analytics;
    }

    // ─── Invalidate cache for an account (call after new transaction) ───
    public void invalidateCache(String accountNumber) {
        String cacheKey = CACHE_PREFIX + accountNumber;
        redisTemplate.delete(cacheKey);
        log.info("Cache invalidated for account: {}", accountNumber);
    }

    // ─── Compute analytics from transactions ───
    private AnalyticsResponse computeAnalytics(String accountNumber,
                                               List<TransactionResponse> transactions) {

        BigDecimal totalMoneyIn = BigDecimal.ZERO;
        BigDecimal totalMoneyOut = BigDecimal.ZERO;
        long successfulCount = 0;
        long failedCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (TransactionResponse tx : transactions) {

            if (!"SUCCESS".equals(tx.getStatus())) {
                failedCount++;
                continue;
            }

            successfulCount++;
            totalAmount = totalAmount.add(tx.getAmount());

            // Money coming IN to this account
            if (accountNumber.equals(tx.getToAccountNumber())) {
                totalMoneyIn = totalMoneyIn.add(tx.getAmount());
            }

            // Money going OUT from this account
            if (accountNumber.equals(tx.getFromAccountNumber())) {
                totalMoneyOut = totalMoneyOut.add(tx.getAmount());
            }
        }

        long totalTransactions = transactions.size();
        BigDecimal avgAmount = totalTransactions > 0
                ? totalAmount.divide(
                BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return AnalyticsResponse.builder()
                .accountNumber(accountNumber)
                .totalMoneyIn(totalMoneyIn)
                .totalMoneyOut(totalMoneyOut)
                .netFlow(totalMoneyIn.subtract(totalMoneyOut))
                .totalTransactions(totalTransactions)
                .successfulTransactions(successfulCount)
                .failedTransactions(failedCount)
                .averageTransactionAmount(avgAmount)
                .generatedAt(LocalDateTime.now())
                .cachedAt("FRESH")
                .build();
    }

    // ─── Convert Redis cached object to AnalyticsResponse ───
    private AnalyticsResponse convertToAnalyticsResponse(Object cached) {
        if (cached instanceof AnalyticsResponse) {
            return (AnalyticsResponse) cached;
        }
        // Jackson deserializes from Redis as LinkedHashMap, handle it
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            String json = mapper.writeValueAsString(cached);
            return mapper.readValue(json, AnalyticsResponse.class);
        } catch (Exception e) {
            log.error("Failed to deserialize cached analytics: {}", e.getMessage());
            return new AnalyticsResponse();
        }
    }

}