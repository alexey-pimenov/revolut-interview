package com.revolut.interview.service;

import com.revolut.interview.exception.AccountNotFoundException;
import com.revolut.interview.model.Account;
import com.revolut.interview.repository.AccountRepository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {

    private AccountService service;

    @Mock
    private AccountRepository repository;

    @Before
    public void setUp() {
        service = new AccountService(repository);
    }

    @Test
    public void successfullyCreatesAccount() {
        //Test that account creates successfully with valid arguments
        Account expected = new Account(1L, "name", BigDecimal.ONE);
        when(repository.create("name", BigDecimal.ONE))
                .thenReturn(expected);
        assertThat(service.create("name", BigDecimal.ONE))
                .isSameAs(expected);

        verify(repository).create("name", BigDecimal.ONE);

    }

    @Test
    public void validatesAccountName() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.create("", BigDecimal.ONE))
                .withMessageContaining("name");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.create(null, BigDecimal.ONE))
                .withMessageContaining("name");
    }

    @Test
    public void validatesInitialAmount() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.create("name", BigDecimal.ONE.negate()))
                .withMessageContaining("amount");

        when(repository.create("name", BigDecimal.ZERO))
                .thenReturn(new Account(1L, "name", BigDecimal.ZERO));
        service.create("name", null);

        verify(repository).create("name", BigDecimal.ZERO);
    }


    @Test
    public void testGetById() {
        Account expected = new Account(1L, "name", BigDecimal.TEN);
        when(repository.getById(1L)).thenReturn(expected);
        when(repository.getById(2L)).thenReturn(null);

        assertThat(service.getById(1L)).isSameAs(expected);

        assertThatExceptionOfType(AccountNotFoundException.class)
                .isThrownBy(() -> service.getById(2L))
                .withMessageContaining("not found");

        verify(repository, times(2)).getById(anyLong());

    }

}