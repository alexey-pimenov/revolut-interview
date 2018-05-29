package com.revolut.interview.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountRequest {

    private String name;

    private BigDecimal amount;
}
