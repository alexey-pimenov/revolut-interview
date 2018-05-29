package com.revolut.interview.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Jackson DTO for transfer operation
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class TransferRequest extends RefillRequest {

    private Long toAccountId;

    public TransferRequest(BigDecimal amount, Long toAccountId) {
        super(amount);
        this.toAccountId = toAccountId;
    }
}
