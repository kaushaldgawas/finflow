package com.finflow.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse implements Serializable {  // ← Serializable for Redis

    private String accountNumber;
    private BigDecimal totalMoneyIn;        // Total deposits/transfers received
    private BigDecimal totalMoneyOut;       // Total withdrawals/transfers sent
    private BigDecimal netFlow;             // totalMoneyIn - totalMoneyOut
    private Long totalTransactions;
    private Long successfulTransactions;
    private Long failedTransactions;
    private BigDecimal averageTransactionAmount;
    private LocalDateTime generatedAt;
    private String cachedAt;               // Shows if result came from cache

}
