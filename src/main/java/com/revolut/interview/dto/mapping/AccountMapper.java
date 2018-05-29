package com.revolut.interview.dto.mapping;

import com.revolut.interview.dto.AccountResponse;
import com.revolut.interview.model.Account;
import org.mapstruct.Mapper;

@Mapper
public interface AccountMapper {

    AccountResponse toResponse(Account account);

}
