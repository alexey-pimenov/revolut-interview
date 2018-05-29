package com.revolut.interview.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Account {

    private final Long id;

    private final String name;

    private BigDecimal amount;
}
