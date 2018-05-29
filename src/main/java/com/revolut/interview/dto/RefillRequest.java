package com.revolut.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Jackson DTO for withdraw/deposit operations
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefillRequest {

    private BigDecimal amount;
}
