package com.revolut.interview.service;

import com.google.common.base.Preconditions;
import com.revolut.interview.exception.AccountNotFoundException;
import com.revolut.interview.exception.InsufficientFundsException;
import com.revolut.interview.exception.InvalidTransferException;
import com.revolut.interview.model.Account;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.function.Consumer;

@Singleton
public class TransferService {

    private final AccountService accountService;

    @Inject
    public TransferService(AccountService accountService) {
        this.accountService = accountService;
    }


    /**
     * Deposit money on account
     *
     * @param accountId accountId
     * @param amount    amount of money to deposit
     * @return modified account
     * @throws AccountNotFoundException - when account not found
     * @throws IllegalArgumentException - when amount is negative or zero
     */
    public Account deposit(Long accountId, BigDecimal amount) {
        Account account = accountService.getById(accountId);
        checkAmount(amount);

        synchronized (account) {
            addAmount(account, amount);
        }
        return account;
    }

    /**
     * Withdraw money from account
     *
     * @param accountId accountId
     * @param amount    amount of money to deposit
     * @return modified account
     * @throws AccountNotFoundException   - when account not found
     * @throws IllegalArgumentException   - when amount is negative or zero
     * @throws InsufficientFundsException - when there is not enough money on account
     */

    public Account withdraw(Long accountId, BigDecimal amount) {
        Account account = accountService.getById(accountId);
        checkAmount(amount);

        synchronized (account) {
            addAmount(account, amount.negate());
        }

        return account;
    }


    public Account transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        Account from = accountService.getById(fromAccountId);
        Account to = accountService.getById(toAccountId);
        checkAmount(amount);

        if (to.equals(from)) {
            throw new InvalidTransferException("Cannot transfer to same account");
        }

        Account firstLock = from.getId() > to.getId() ? from : to;
        Account secondLock = from.getId() < to.getId() ? from : to;

        synchronized (firstLock) {
            synchronized (secondLock) {
                addAmount(from, amount.negate());
                addAmount(to, amount);
            }
        }

        return from;

    }

    private void addAmount(Account account, BigDecimal amount) {

        BigDecimal result = account.getAmount().add(amount);
        if (result.signum() == -1) {
            throw new InsufficientFundsException("Insufficient funds on account #" + account.getId());
        }

        account.setAmount(result);
    }


    private void checkAmount(BigDecimal amount) {
        Preconditions.checkArgument(amount != null, "Amount should be set");
        Preconditions.checkArgument(amount.signum() > 0, "Amount must be positive");
    }


}
