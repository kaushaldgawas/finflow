package com.finflow.loan.service;

import com.finflow.loan.client.AccountServiceClient;
import com.finflow.loan.dto.AccountResponse;
import com.finflow.loan.dto.LoanApplicationRequest;
import com.finflow.loan.dto.LoanResponse;
import com.finflow.loan.dto.LoanStatusUpdateRequest;
import com.finflow.loan.entity.Loan;
import com.finflow.loan.entity.LoanStatus;
import com.finflow.loan.entity.LoanType;
import com.finflow.loan.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final AccountServiceClient accountServiceClient;

    // ─── Apply for Loan ───
    public LoanResponse applyForLoan(Long userId, LoanApplicationRequest request) {

        // ─── Step 1: Fetch account and check eligibility ───
        AccountResponse account = accountServiceClient
                .getAccountByNumber(request.getAccountNumber());

        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException("Account is not active");
        }

        // ─── Step 2: Basic eligibility check ───
        // Minimum balance should be 10% of loan amount
        BigDecimal minimumBalance = request.getLoanAmount()
                .multiply(BigDecimal.valueOf(0.10));

        if (account.getBalance().compareTo(minimumBalance) < 0) {
            throw new RuntimeException(
                    "Insufficient account balance for loan eligibility. " +
                            "Minimum required balance: ₹" + minimumBalance);
        }

        // ─── Step 3: Calculate interest rate based on loan type ───
        BigDecimal interestRate = getInterestRate(request.getLoanType());

        // ─── Step 4: Calculate EMI ───
        BigDecimal emiAmount = calculateEMI(
                request.getLoanAmount(),
                interestRate,
                request.getTenureMonths()
        );

        // ─── Step 5: Create loan ───
        Loan loan = Loan.builder()
                .loanRef(generateLoanRef())
                .userId(userId)
                .accountNumber(request.getAccountNumber())
                .loanType(request.getLoanType())
                .loanAmount(request.getLoanAmount())
                .tenureMonths(request.getTenureMonths())
                .interestRate(interestRate)
                .emiAmount(emiAmount)
                .status(LoanStatus.PENDING)
                .remarks(request.getRemarks())
                .build();

        Loan saved = loanRepository.save(loan);
        log.info("Loan application submitted: {}", saved.getLoanRef());

        return mapToResponse(saved);
    }

    // ─── Get All Loans for a User ───
    public List<LoanResponse> getLoansByUserId(Long userId) {
        return loanRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Single Loan ───
    public LoanResponse getLoanById(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        return mapToResponse(loan);
    }

    // ─── Get Loan by Reference ───
    public LoanResponse getLoanByRef(String loanRef) {
        Loan loan = loanRepository.findByLoanRef(loanRef)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        return mapToResponse(loan);
    }

    // ─── Update Loan Status (Admin) ───
    public LoanResponse updateLoanStatus(Long loanId,
                                         LoanStatusUpdateRequest request) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus(request.getStatus());

        if (request.getRemarks() != null) {
            loan.setRemarks(request.getRemarks());
        }

        if (request.getRejectionReason() != null) {
            loan.setRejectionReason(request.getRejectionReason());
        }

        if (request.getStatus() == LoanStatus.APPROVED) {
            loan.setApprovedAt(LocalDateTime.now());
        }

        Loan updated = loanRepository.save(loan);
        log.info("Loan {} status updated to {}", loan.getLoanRef(), request.getStatus());

        return mapToResponse(updated);
    }

    // ─── Get All Pending Loans (Admin) ───
    public List<LoanResponse> getPendingLoans() {
        return loanRepository.findByStatus(LoanStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── EMI Calculator ───
    // Formula: EMI = P * r * (1+r)^n / ((1+r)^n - 1)
    private BigDecimal calculateEMI(BigDecimal principal,
                                    BigDecimal annualRate,
                                    int tenureMonths) {
        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowN = onePlusR.pow(tenureMonths);

        BigDecimal emi = principal
                .multiply(monthlyRate)
                .multiply(onePlusRPowN)
                .divide(onePlusRPowN.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);

        return emi;
    }

    // ─── Interest rates per loan type ───
    private BigDecimal getInterestRate(LoanType loanType) {
        return switch (loanType) {
            case PERSONAL -> BigDecimal.valueOf(12.5);
            case HOME -> BigDecimal.valueOf(8.5);
            case VEHICLE -> BigDecimal.valueOf(9.5);
            case EDUCATION -> BigDecimal.valueOf(7.5);
            case BUSINESS -> BigDecimal.valueOf(14.0);
        };
    }

    // ─── Generate unique loan reference ───
    private String generateLoanRef() {
        String datePart = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString()
                .substring(0, 6).toUpperCase();
        return "LN" + datePart + randomPart;
    }

    // ─── Map Entity to Response ───
    private LoanResponse mapToResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .loanRef(loan.getLoanRef())
                .userId(loan.getUserId())
                .accountNumber(loan.getAccountNumber())
                .loanType(loan.getLoanType())
                .loanAmount(loan.getLoanAmount())
                .tenureMonths(loan.getTenureMonths())
                .interestRate(loan.getInterestRate())
                .emiAmount(loan.getEmiAmount())
                .status(loan.getStatus())
                .remarks(loan.getRemarks())
                .rejectionReason(loan.getRejectionReason())
                .appliedAt(loan.getAppliedAt())
                .approvedAt(loan.getApprovedAt())
                .build();
    }

}
