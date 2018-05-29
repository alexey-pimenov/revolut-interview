package com.revolut.interview.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.revolut.interview.exception.AccountNotFoundException;
import com.revolut.interview.model.Account;
import com.revolut.interview.repository.AccountRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Collection;

@Singleton
public class AccountService {

    private final AccountRepository repository;

    @Inject
    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    /**
     * Create new account
     *
     * @param accountName
     * @param initialAmount
     * @return
     */
    public Account create(String accountName, BigDecimal initialAmount) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(accountName), "Account name should be supplied");

        if (initialAmount == null) {
            initialAmount = BigDecimal.ZERO;
        } else if (initialAmount.signum() < 0) {
            throw new IllegalArgumentException("Initial amount cannot be negative");
        }
        return repository.create(accountName, initialAmount);
    }

    /**
     * Get all accounts
     *
     * @return
     */

    public Collection<Account> getAll() {
        return repository.getAll();
    }

    /**
     * Find existing account by id
     *
     * @param id accountId
     * @return account
     * @throws AccountNotFoundException - when account is not found
     */
    public Account getById(Long id) {
        Preconditions.checkArgument(id != null, "Account id must not be null");
        Account account = repository.getById(id);

        if (account == null) {
            throw new AccountNotFoundException("Account #" + id + " not found");
        }
        return account;
    }


}
