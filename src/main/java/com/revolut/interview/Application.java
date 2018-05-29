package com.revolut.interview;

import com.revolut.interview.dto.CreateAccountRequest;
import com.revolut.interview.dto.RefillRequest;
import com.revolut.interview.dto.TransferRequest;
import com.revolut.interview.dto.mapping.AccountMapper;
import com.revolut.interview.model.Account;
import com.revolut.interview.service.AccountService;
import com.revolut.interview.service.TransferService;
import lombok.extern.slf4j.Slf4j;
import org.jooby.Jooby;
import org.jooby.MediaType;
import org.jooby.apitool.ApiTool;
import org.jooby.json.Jackson;
import org.mapstruct.factory.Mappers;

import javax.inject.Inject;

@Slf4j
public class Application extends Jooby {

    @Inject
    private AccountService accountService;

    @Inject
    private TransferService transferService;

    private AccountMapper mapper = Mappers.getMapper(AccountMapper.class);

    public Application() {
        use(new Jackson());

        path("/api/accounts", () -> {

            /**
             * Return all accounts
             */
            get("/", req -> accountService.getAll()
                    .stream()
                    .map(mapper::toResponse));

            /**
             * Returns account by id
             *
             * @param id account id
             * @return Returns <code>200</code> with account or <code>404</code> if account not exists
             */
            get("/:id", req -> accountService.getById(req.param("id").longValue()));


            /**
             * Adds a new account
             *
             * @param body Account object
             * @return Returns a saved account or <code>400</code> in case of validation error.
             */
            post("/", req -> {
                CreateAccountRequest body = req.body(CreateAccountRequest.class);
                Account account = accountService.create(body.getName(), body.getAmount());
                return mapper.toResponse(account);
            });


            /**
             * Deposit money on account
             *
             * @param id accountId
             * @param body refill request
             * @return Returns a modified account or <code>400</code> in case of validation error.
             */
            post("/:id/deposit", req -> {
                Account account = transferService.deposit(
                        req.param("id").longValue(),
                        req.body(RefillRequest.class).getAmount());
                return mapper.toResponse(account);
            });

            /**
             * Withdraw money from account
             *
             * @param id accountId
             * @param body refill request
             * @return Returns a modified account or <code>400</code> in case of validation error.
             */
            post("/:id/withdraw", req -> {
                Account account = transferService.withdraw(
                        req.param("id").longValue(),
                        req.body(RefillRequest.class).getAmount());
                return mapper.toResponse(account);
            });


            /**
             * Transfer money form one account to another
             *
             * @param id accountId
             * @param body transfer request
             * @return Returns a modified account or <code>400</code> in case of validation error.
             */
            post("/:id/transfer", req -> {
                TransferRequest transferRequest = req.body(TransferRequest.class);
                Account account = transferService.transfer(
                        req.param("id").longValue(),
                        transferRequest.getToAccountId(),
                        transferRequest.getAmount());
                return mapper.toResponse(account);
            });

        }).consumes(MediaType.json).produces(MediaType.json);

        err(new ErrorHandler());

        use(new ApiTool()
                .swagger()
                .raml());

    }

    public static void main(String[] args) {
        run(Application::new, args);
    }
}
