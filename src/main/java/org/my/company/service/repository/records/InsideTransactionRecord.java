package org.my.company.service.repository.records;

import org.my.company.service.WithdrawalId;

import java.math.BigDecimal;

/**
 * Immutability provide to us opportunity to not copy objects on save to repo.
 */
public record InsideTransactionRecord(WithdrawalId uuid, long accountIdFrom, long accountIdTo, BigDecimal amount,
                                      InsideTransactionState state, String errorMessage) {


    public InsideTransactionRecord buildAsCompleted() {
        return new InsideTransactionRecord(uuid, this.accountIdFrom(), this.accountIdTo(), this.amount(), InsideTransactionState.COMPLETE, "");
    }

    public InsideTransactionRecord buildAsError(String errorMessage) {
        return new InsideTransactionRecord(uuid, this.accountIdFrom(), this.accountIdTo(), this.amount(), InsideTransactionState.ERROR, errorMessage);
    }

    public InsideTransactionRecord buildAsFailedToComplete(String errorMessage) {
        return new InsideTransactionRecord(uuid, this.accountIdFrom(), this.accountIdTo(), this.amount(), InsideTransactionState.FAILED_TO_COMPLETE, errorMessage);
    }
}
