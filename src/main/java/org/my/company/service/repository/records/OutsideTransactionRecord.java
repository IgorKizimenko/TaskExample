package org.my.company.service.repository.records;

import org.my.company.service.Address;
import org.my.company.service.WithdrawalId;

import java.math.BigDecimal;

public record OutsideTransactionRecord(WithdrawalId uuid, long accountIdFrom, Address address, BigDecimal amount,
                                       OutsideTransactionState state, String errorMessage) {
    public OutsideTransactionRecord buildSent() {
        return new OutsideTransactionRecord(uuid, accountIdFrom, address,amount, OutsideTransactionState.SENT, "");
    }

    public OutsideTransactionRecord buildComplete() {
        return new OutsideTransactionRecord(uuid, accountIdFrom, address,amount, OutsideTransactionState.COMPLETE, "");
    }

    public OutsideTransactionRecord buildRejected() {
        return new OutsideTransactionRecord(uuid, accountIdFrom, address,amount, OutsideTransactionState.REJECTED, "");
    }

    public OutsideTransactionRecord buildRejectFail(String errorMessage) {
        return new OutsideTransactionRecord(uuid, accountIdFrom, address,amount, OutsideTransactionState.REJECT_FAILED, errorMessage);
    }

    public OutsideTransactionRecord buildError(String errorMessage) {
        return new OutsideTransactionRecord(uuid, accountIdFrom, address,amount, OutsideTransactionState.ERROR, errorMessage);
    }
}
