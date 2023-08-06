package org.my.company.service.flows.income;

import org.my.company.service.WithdrawalId;

import java.math.BigDecimal;

public record InsideWithdraw(WithdrawalId withdrawalId, long accountFrom, long accountTo, BigDecimal amount) {
}
