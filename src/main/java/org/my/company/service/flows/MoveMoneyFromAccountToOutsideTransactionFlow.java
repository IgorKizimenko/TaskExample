package org.my.company.service.flows;

import lombok.RequiredArgsConstructor;
import org.my.company.service.AwaitAccountLockingService;
import org.my.company.service.flows.exceptions.AccountNotFoundException;
import org.my.company.service.flows.exceptions.NotEnoughMoneyException;
import org.my.company.service.flows.income.OutsideWithdraw;
import org.my.company.service.repository.AccountsRepository;
import org.my.company.service.repository.InsideTransactionRepository;
import org.my.company.service.repository.OutsideTransactionHelper;
import org.my.company.service.repository.records.AccountRecord;
import org.my.company.service.repository.records.OutsideTransactionRecord;
import org.my.company.service.repository.records.OutsideTransactionState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Responsibility:
 * perform workflow part related to block required amount on "From" side account {@link AccountsRepository}
 * and publish transitionally to {@link InsideTransactionRepository}
 * hint: Keep in mind that we are currently in service which is responsible for this account.
 */
@Component
@RequiredArgsConstructor
public class MoveMoneyFromAccountToOutsideTransactionFlow {
    private final AccountsRepository accountsRepository;
    private final OutsideTransactionHelper transactionHelper;
    private final AwaitAccountLockingService accountLocker;
    @Value("${move-from-account-to-outcome-transaction-queue.max-lock-await:1m}")
    private final Duration maxLockWait;

    public void process(OutsideWithdraw outsideWithdraw) throws AccountNotFoundException, NotEnoughMoneyException {
        long accountFrom = outsideWithdraw.accountId();
        boolean isLockedByYou = accountLocker.awaitLock(accountFrom, maxLockWait);
        if (!isLockedByYou) {
            throw new IllegalStateException("failed to obtain the lock on account. Please, try to do it later.");
        }
        try {
            AccountRecord accountRecord = accountsRepository.getAccountById(accountFrom).orElseThrow(() -> new AccountNotFoundException(accountFrom));
            AccountRecord updatedRecord = accountRecord.extractAmount(outsideWithdraw.amount());
            OutsideTransactionRecord outsideTransactionRecord = buildTransactionEntity(outsideWithdraw);

            transactionHelper.updateAndAddTogether(updatedRecord, outsideTransactionRecord);
        } finally {
            accountLocker.releaseLock(accountFrom);
        }
    }

    private OutsideTransactionRecord buildTransactionEntity(OutsideWithdraw params) {
        return new OutsideTransactionRecord(params.withdrawalId(), params.accountId(), params.address(), params.amount(), OutsideTransactionState.NEW, "");
    }
}
