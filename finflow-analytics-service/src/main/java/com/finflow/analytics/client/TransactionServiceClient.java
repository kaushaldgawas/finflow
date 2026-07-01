package com.finflow.analytics.client;

import com.finflow.analytics.dto.TransactionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "finflow-transaction-service", url = "http://localhost:8083")
public interface TransactionServiceClient {

    @GetMapping("/api/v1/transactions/history/{accountNumber}")
    List<TransactionResponse> getTransactionHistory(
            @PathVariable String accountNumber);

}
