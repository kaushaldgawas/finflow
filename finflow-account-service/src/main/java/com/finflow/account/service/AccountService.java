package com.finflow.account.service;

import com.finflow.account.dto.AccountResponse;
import com.finflow.account.dto.BalanceUpdateRequest;
import com.finflow.account.dto.CreateAccountRequest;
import com.finflow.account.entity.Account;
import com.finflow.account.entity.AccountStatus;
import com.finflow.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    // ─── Create Account ───
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .userId(userId)
                .accountHolderName(request.getAccountHolderName())
                .accountType(request.getAccountType())
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .ifscCode("FINF0001234")     // Fixed IFSC for our fictional bank
                .build();

        Account saved = accountRepository.save(account);

        return mapToResponse(saved);
    }

    // ─── Get All Accounts for a User ───
    public List<AccountResponse> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Single Account by ID ───
    public AccountResponse getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return mapToResponse(account);
    }

    // ─── Get Account by Account Number (used internally by Transaction Service later) ───
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return mapToResponse(account);
    }

    // ─── Deposit Money ───
    public AccountResponse deposit(Long accountId, BalanceUpdateRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }

        account.setBalance(account.getBalance().add(request.getAmount()));
        Account updated = accountRepository.save(account);

        return mapToResponse(updated);
    }

    // ─── Withdraw Money ───
    public AccountResponse withdraw(Long accountId, BalanceUpdateRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        Account updated = accountRepository.save(account);

        return mapToResponse(updated);
    }

    // ─── Generate Unique 12-digit Account Number ───
    private String generateAccountNumber() {
        String accountNumber;
        Random random = new Random();
        do {
            StringBuilder sb = new StringBuilder("FF");   // FinFlow prefix
            for (int i = 0; i < 10; i++) {
                sb.append(random.nextInt(10));
            }
            accountNumber = sb.toString();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    // ─── Map Entity to Response DTO ───
    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountHolderName(account.getAccountHolderName())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .ifscCode(account.getIfscCode())
                .createdAt(account.getCreatedAt())
                .build();
    }

}
