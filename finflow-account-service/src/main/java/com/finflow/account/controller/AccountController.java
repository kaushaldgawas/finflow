package com.finflow.account.controller;

import com.finflow.account.dto.AccountResponse;
import com.finflow.account.dto.BalanceUpdateRequest;
import com.finflow.account.dto.CreateAccountRequest;
import com.finflow.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // ─── Create Account ───
    // For now, userId comes via header (will switch to JWT claims once Gateway validates tokens)
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountService.createAccount(userId, request));
    }

    // ─── Get All Accounts for Logged-in User ───
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    // ─── Get Single Account by ID ───
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    // ─── Get Account by Account Number (used by Transaction Service later) ───
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByNumber(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    // ─── Deposit ───
    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<AccountResponse> deposit(
            @PathVariable Long accountId,
            @Valid @RequestBody BalanceUpdateRequest request) {
        return ResponseEntity.ok(accountService.deposit(accountId, request));
    }

    // ─── Withdraw ───
    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(
            @PathVariable Long accountId,
            @Valid @RequestBody BalanceUpdateRequest request) {
        return ResponseEntity.ok(accountService.withdraw(accountId, request));
    }

    // ─── Health Check ───
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Account Service is running!");
    }

}
