package org.my.company.service.repository.records;

import org.my.company.service.flows.exceptions.NotEnoughMoneyException;

import java.math.BigDecimal;


public record AccountRecord(long accountId, BigDecimal freeAmount) {

    public AccountRecord extractAmount(BigDecimal requestAmount) throws NotEnoughMoneyException {
        BigDecimal result = this.freeAmount().add(requestAmount.negate());
        if (BigDecimal.ZERO.compareTo(result) > 0) {
            throw new NotEnoughMoneyException("required " + requestAmount + " is not presented on acccount.");
        }

        return new AccountRecord(this.accountId(), result);
    }

    public AccountRecord addAmount(BigDecimal requestAmount) {
        if (BigDecimal.ZERO.compareTo(requestAmount) > 0) {
            throw new IllegalArgumentException("required " + requestAmount + " should be positive.");
        }

        BigDecimal result = this.freeAmount().add(requestAmount);
        return new AccountRecord(this.accountId(), result);
    }

}
