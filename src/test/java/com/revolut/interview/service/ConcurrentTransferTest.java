package com.revolut.interview.service;

import com.revolut.interview.model.Account;
import com.revolut.interview.repository.AccountRepository;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrentTransferTest {

    private static final int ACCOUNT_NUMBER = 5;

    private static final BigDecimal MAX_AMOUNT = new BigDecimal(1_000_000_000);

    private static final long RUN_COUNT = 15_000_000;


    private AccountService accountService = new AccountService(new AccountRepository());

    private TransferService transferService = new TransferService(accountService);

    @Before
    public void setUp() {
        for (int i = 0; i < ACCOUNT_NUMBER; i++) {
            createAccount(MAX_AMOUNT);
        }
    }


    @Test(timeout = 120_000)
    public void concurrentTransferShouldRunAsConsecutive() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(50);

        Random random = new Random();

        LongAdder count = new LongAdder();
        List<Future> tasks = random.ints(RUN_COUNT, 1, 10).boxed()
                .map(BigDecimal::valueOf)
                .map(amount -> {
                    long fromId = random.nextInt(ACCOUNT_NUMBER) + 1;
                    long toId = (long) random.nextInt(ACCOUNT_NUMBER) + 1;
                    if (toId == fromId) {
                        toId = ((toId + 1) % ACCOUNT_NUMBER) + 1;
                    }
                    final long toAccountId = toId;
                    return (Runnable) () -> {
                        transferService.transfer(fromId, toAccountId, amount);
                        count.increment();
                    };
                })
                .map(executor::submit)
                .collect(Collectors.toList());


        for (Future task : tasks) {
            task.get(); //So exceptions are propagated
        }
        assertThat(count.longValue())
                .isEqualTo(RUN_COUNT);//Check that tasks really ran
        BigDecimal allAccountsSum = accountService.getAll()
                .stream()
                .map(Account::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        //Check for race conditions
        assertThat(allAccountsSum).isEqualTo(MAX_AMOUNT.multiply(new BigDecimal(ACCOUNT_NUMBER)));


    }

    private Account createAccount(BigDecimal amount) {
        return accountService.create("name", amount);
    }
}
