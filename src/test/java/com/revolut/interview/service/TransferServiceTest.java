package com.revolut.interview.service;

import com.revolut.interview.exception.InsufficientFundsException;
import com.revolut.interview.exception.InvalidTransferException;
import com.revolut.interview.model.Account;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransferServiceTest {

    @Mock
    private AccountService accountService;

    private TransferService transferService;

    @Before
    public void setUp() {
        this.transferService = new TransferService(accountService);
    }

    @Test
    public void cantDepositWrongAmount() {
        //can't deposit negative or null amount
        mockAccount(1L);
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> transferService.deposit(1L, null))
                .withMessageContaining("Amount should be set");

        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> transferService.deposit(1L, BigDecimal.TEN.negate()))
                .withMessageContaining("positive");

        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> transferService.withdraw(1L, BigDecimal.ZERO))
                .withMessageContaining("positive");

        verify(accountService, times(3)).getById(1L);
    }

    @Test
    public void successfullyDepositMoney() {
        mockAccount(1L, BigDecimal.ONE);
        Account account = transferService.deposit(1L, BigDecimal.TEN);
        assertThat(account.getAmount()).isEqualTo((BigDecimal.valueOf(11)));

    }


    @Test
    public void cantWithdrawWrongAmount() {
        //can't deposit negative or null amount
        mockAccount(1L);
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> transferService.withdraw(1L, null))
                .withMessageContaining("Amount should be set");

        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> transferService.withdraw(1L, BigDecimal.TEN.negate()))
                .withMessageContaining("positive");

        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> transferService.withdraw(1L, BigDecimal.ZERO))
                .withMessageContaining("positive");

        verify(accountService, times(3)).getById(1L);
    }


    @Test
    public void cantWithdrawMoreThanHave() {
        //can't deposit negative or null amount
        mockAccount(1L, BigDecimal.ONE);
        Assertions.assertThatExceptionOfType(InsufficientFundsException.class)
                .isThrownBy(() -> transferService.withdraw(1L, BigDecimal.TEN))
                .withMessageContaining("funds");
        verify(accountService).getById(1L);
    }


    @Test
    public void successfullyWithdrawMoney() {
        mockAccount(1L, BigDecimal.TEN);
        Account account = transferService.withdraw(1L, BigDecimal.ONE);
        assertThat(account.getAmount()).isEqualTo((BigDecimal.valueOf(9)));

    }

    @Test
    public void cantTransferWrongAmount() {
        //can't deposit negative or null amount
        mockAccount(1L);
        mockAccount(2L);
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> transferService.transfer(1L, 2L, null))
                .withMessageContaining("Amount should be set");

        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> transferService.transfer(1L, 2L, BigDecimal.ZERO))
                .withMessageContaining("positive");

        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> transferService.transfer(1L, 2L, BigDecimal.ONE.negate()))
                .withMessageContaining("positive");

        verify(accountService, times(3)).getById(1L);
    }

    @Test
    public void cantTransferToSelf() {
        //can't deposit negative or null amount
        mockAccount(1L);
        Assertions.assertThatExceptionOfType(InvalidTransferException.class)
                .isThrownBy(() -> transferService.transfer(1L, 1L, BigDecimal.TEN))
                .withMessageContaining("same");

    }

    @Test
    public void cantTransferMoreThanHave() {
        //can't deposit negative or null amount
        mockAccount(1L, BigDecimal.ONE);
        mockAccount(2L);
        Assertions.assertThatExceptionOfType(InsufficientFundsException.class)
                .isThrownBy(() -> transferService.transfer(1L, 2L, BigDecimal.TEN))
                .withMessageContaining("funds");
    }

    @Test
    public void successfullyTransfer() {
        //can't deposit negative or null amount
        Account first = mockAccount(1L, BigDecimal.TEN);
        Account second = mockAccount(2L);
        transferService.transfer(1L, 2L, BigDecimal.valueOf(4));

        assertThat(first.getAmount()).isEqualTo(BigDecimal.valueOf(6));
        assertThat(second.getAmount()).isEqualTo(BigDecimal.valueOf(14));

    }


    private Account mockAccount(Long id) {
        return mockAccount(id, BigDecimal.TEN);
    }

    private Account mockAccount(Long id, BigDecimal amount) {
        Account account = new Account(id, "name", amount);
        when(accountService.getById(id)).thenReturn(account);
        return account;
    }

}