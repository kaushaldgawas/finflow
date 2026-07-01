package com.finflow.loan.dto;

import com.finflow.loan.entity.LoanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private LoanStatus status;

    private String remarks;

    private String rejectionReason;

}
