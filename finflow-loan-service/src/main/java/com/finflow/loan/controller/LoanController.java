package com.finflow.loan.controller;

import com.finflow.loan.dto.LoanApplicationRequest;
import com.finflow.loan.dto.LoanResponse;
import com.finflow.loan.dto.LoanStatusUpdateRequest;
import com.finflow.loan.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    // ─── Apply for Loan ───
    @PostMapping
    public ResponseEntity<LoanResponse> applyForLoan(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(loanService.applyForLoan(userId, request));
    }

    // ─── Get My Loans ───
    @GetMapping
    public ResponseEntity<List<LoanResponse>> getMyLoans(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(loanService.getLoansByUserId(userId));
    }

    // ─── Get Loan by ID ───
    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> getLoan(
            @PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getLoanById(loanId));
    }

    // ─── Get Loan by Reference ───
    @GetMapping("/ref/{loanRef}")
    public ResponseEntity<LoanResponse> getLoanByRef(
            @PathVariable String loanRef) {
        return ResponseEntity.ok(loanService.getLoanByRef(loanRef));
    }

    // ─── Get All Pending Loans (Admin) ───
    @GetMapping("/pending")
    public ResponseEntity<List<LoanResponse>> getPendingLoans() {
        return ResponseEntity.ok(loanService.getPendingLoans());
    }

    // ─── Update Loan Status (Admin) ───
    @PutMapping("/{loanId}/status")
    public ResponseEntity<LoanResponse> updateLoanStatus(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanStatusUpdateRequest request) {
        return ResponseEntity.ok(loanService.updateLoanStatus(loanId, request));
    }

    // ─── Health Check ───
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Loan Service is running!");
    }

}
