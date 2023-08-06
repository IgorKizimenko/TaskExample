package org.my.company.service.flows.exceptions;

import lombok.Getter;

public class AccountNotFoundException extends Exception {
    @Getter
    private final long accountId;

    public AccountNotFoundException(long accountId) {

        this.accountId = accountId;
    }
}
