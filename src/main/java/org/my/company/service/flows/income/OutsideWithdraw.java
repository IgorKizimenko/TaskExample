package org.my.company.service.flows.income;

import org.my.company.service.Address;
import org.my.company.service.WithdrawalId;

import java.math.BigDecimal;

public record OutsideWithdraw(WithdrawalId withdrawalId, long accountId, Address address, BigDecimal amount) {
}
