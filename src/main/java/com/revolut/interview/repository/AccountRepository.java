package com.revolut.interview.repository;

import com.google.common.annotations.VisibleForTesting;
import com.revolut.interview.model.Account;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class AccountRepository {

    private final Map<Long, Account> accounts = new ConcurrentHashMap<>();

    private final AtomicLong idCounter = new AtomicLong(1L);

    public Account create(String name, BigDecimal initialAmount) {
        long id = idCounter.getAndIncrement();
        Account account = new Account(id, name, initialAmount);
        accounts.put(id, account);
        return account;
    }

    public Account getById(Long id) {
        return accounts.get(id);
    }

    public Collection<Account> getAll() {
        return Collections.unmodifiableCollection(accounts.values());
    }


    @VisibleForTesting
    public void clear(){
        accounts.clear();
    }
}
