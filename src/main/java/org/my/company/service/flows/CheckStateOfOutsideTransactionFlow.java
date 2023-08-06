package org.my.company.service.flows;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.my.company.service.AwaitAccountLockingService;
import org.my.company.service.WithdrawalService;
import org.my.company.service.repository.AccountsRepository;
import org.my.company.service.repository.OutsideTransactionRepository;
import org.my.company.service.repository.OutsideTransactionHelper;
import org.my.company.service.repository.records.AccountRecord;
import org.my.company.service.repository.records.OutsideTransactionRecord;
import org.my.company.service.repository.records.OutsideTransactionState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@AllArgsConstructor
public class CheckStateOfOutsideTransactionFlow {
    private final AccountsRepository accountsRepository;
    private final OutsideTransactionRepository outsideTransactionRepository;
    private final AwaitAccountLockingService accountLocker;
    private final WithdrawalService withdrawalService;
    private final OutsideTransactionHelper outsideTransactionHelper;
    @Value("${check-state-of-outcome-transaction-queue.max-lock-await:1m}")
    private final Duration maxLockWait;

    public void process(OutsideTransactionRecord outsideTransaction) {
        if (outsideTransaction.state() != OutsideTransactionState.SENT) {
            throw new IllegalArgumentException("this execution brunch was made only for tracking position of request.");
        }

        var state = withdrawalService.getRequestState(outsideTransaction.uuid());
        switch (state) {
            case COMPLETED -> outsideTransactionRepository.update(outsideTransaction.buildComplete());
            case FAILED -> submitAmountBackToAccount(outsideTransaction.buildRejected());
        }
    }

    private void submitAmountBackToAccount(OutsideTransactionRecord outsideTransaction) {
        long accountFrom = outsideTransaction.accountIdFrom();
        boolean isLockedByYou = accountLocker.awaitLock(accountFrom, maxLockWait);
        if (!isLockedByYou) {
            //to avoid reprocessing (infinity wait).
            outsideTransactionRepository.update(outsideTransaction.buildRejectFail("fail to obtain the lock on account."));
            return;
        }

        try {
            var recordOptional = accountsRepository.getAccountById(accountFrom);
            if (recordOptional.isPresent()) {
                AccountRecord updatedRecord = recordOptional.get().addAmount(outsideTransaction.amount());
                OutsideTransactionRecord updatedTransaction = outsideTransaction.buildRejected();
                outsideTransactionHelper.updateTogether(updatedRecord, updatedTransaction);
            } else {
                outsideTransactionRepository.update(outsideTransaction.buildError("destination account was not found."));
            }
        } finally {
            accountLocker.releaseLock(accountFrom);
        }
    }

}
