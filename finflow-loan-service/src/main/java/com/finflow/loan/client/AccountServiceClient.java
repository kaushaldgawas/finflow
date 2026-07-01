package com.finflow.loan.client;

import com.finflow.loan.dto.AccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "finflow-account-service", url = "http://localhost:8082")
public interface AccountServiceClient {

    @GetMapping("/api/v1/accounts/number/{accountNumber}")
    AccountResponse getAccountByNumber(@PathVariable String accountNumber);

}
