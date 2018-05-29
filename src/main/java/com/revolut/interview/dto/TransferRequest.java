package com.revolut.interview.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Jackson DTO for transfer operation
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TransferRequest extends RefillRequest {

    private Long toAccountId;
}
