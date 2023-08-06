package org.my.company.service;

import java.math.BigDecimal;

import static org.my.company.service.WithdrawalState.PROCESSING;

record Withdrawal(WithdrawalState state, long finaliseAt, Address address, BigDecimal amount) {
    public WithdrawalState finalState() {
        return finaliseAt <= System.currentTimeMillis() ? state : PROCESSING;
    }
}
