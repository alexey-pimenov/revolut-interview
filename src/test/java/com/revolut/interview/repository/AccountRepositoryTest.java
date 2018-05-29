package com.revolut.interview.repository;

import com.revolut.interview.model.Account;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class AccountRepositoryTest {

    private AccountRepository repository;

    @Before
    public void setUp() {
        this.repository = new AccountRepository();
    }

    @Test
    public void successfullyCreatesAccountWithAnyArguments() {
        Account account = repository.create("name", BigDecimal.ONE);
        assertNotNull(account);
        assertNotNull(account.getId());
        assertEquals(BigDecimal.ONE, account.getAmount());
        assertEquals("name", account.getName());


        Account account2 = repository.create(null, BigDecimal.ZERO);
        assertNotNull(account2);
        assertNotNull(account.getId());
        assertEquals(BigDecimal.ZERO, account2.getAmount());
        assertNull(account2.getName());

        Account account3 = repository.create(null, null);
        assertNotNull(account3);
        assertNotNull(account3.getId());
        assertNull(account3.getAmount());
        assertNull(account3.getName());

    }

    @Test
    public void idStartsWithOneAndIncrementsWithOne() {
        Account account = repository.create("name", BigDecimal.ONE);
        assertEquals(1L, account.getId().longValue());

        account = repository.create(null, BigDecimal.ZERO);
        assertEquals(2L, account.getId().longValue());


    }

}