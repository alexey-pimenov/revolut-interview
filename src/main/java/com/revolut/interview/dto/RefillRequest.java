package com.revolut.interview.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Jackson DTO for withdraw/deposit operations
 */
@Data
public class RefillRequest {

    private BigDecimal amount;
}
