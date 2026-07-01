package com.finflow.loan.dto;

import com.finflow.loan.entity.LoanStatus;
import com.finflow.loan.entity.LoanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {

    private Long id;
    private String loanRef;
    private Long userId;
    private String accountNumber;
    private LoanType loanType;
    private BigDecimal loanAmount;
    private Integer tenureMonths;
    private BigDecimal interestRate;
    private BigDecimal emiAmount;
    private LoanStatus status;
    private String remarks;
    private String rejectionReason;
    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;

}
