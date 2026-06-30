package com.finflow.transaction.client;

import com.finflow.transaction.dto.AccountResponse;
import com.finflow.transaction.dto.BalanceUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "finflow-account-service")   // ← Eureka service name, no hardcoded URL!
public interface AccountServiceClient {

    @GetMapping("/api/v1/accounts/number/{accountNumber}")
    AccountResponse getAccountByNumber(@PathVariable String accountNumber);

    @PostMapping("/api/v1/accounts/{accountId}/deposit")
    AccountResponse deposit(@PathVariable Long accountId,
                            @RequestBody BalanceUpdateRequest request);

    @PostMapping("/api/v1/accounts/{accountId}/withdraw")
    AccountResponse withdraw(@PathVariable Long accountId,
                             @RequestBody BalanceUpdateRequest request);

}
