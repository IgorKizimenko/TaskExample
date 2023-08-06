package org.my.company.service.flows;

import lombok.RequiredArgsConstructor;
import org.my.company.service.AwaitAccountLockingService;
import org.my.company.service.flows.exceptions.AccountNotFoundException;
import org.my.company.service.flows.exceptions.NotEnoughMoneyException;
import org.my.company.service.flows.income.InsideWithdraw;
import org.my.company.service.repository.AccountsRepository;
import org.my.company.service.repository.InsideTransactionHelper;
import org.my.company.service.repository.InsideTransactionRepository;
import org.my.company.service.repository.records.AccountRecord;
import org.my.company.service.repository.records.InsideTransactionRecord;
import org.my.company.service.repository.records.InsideTransactionState;
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
public class MoveMoneyFromAccountToInsideTransactionFlow {
    private final AccountsRepository accountsRepository;
    private final InsideTransactionHelper transactionHelper;
    private final AwaitAccountLockingService accountLocker;
    @Value("${move-from-account-to-inside-transaction-queue.max-lock-await:1m}")
    private final Duration maxLockWait;
    public void process(InsideWithdraw insideWithdraw) throws AccountNotFoundException, NotEnoughMoneyException {

        long accountFrom = insideWithdraw.accountFrom();
        long accountTo = insideWithdraw.accountTo();
        verifyAccountExist(accountTo);
        boolean isLockedByYou = accountLocker.awaitLock(accountFrom, maxLockWait);
        if (!isLockedByYou) {
            throw new IllegalStateException("failed to obtain the lock on account. Please, contact with administrator to solve this problem");
        }
        try {
            AccountRecord accountRecord = accountsRepository.getAccountById(accountFrom).orElseThrow(() -> new AccountNotFoundException(accountFrom));
            AccountRecord updatedRecord = accountRecord.extractAmount(insideWithdraw.amount());
            InsideTransactionRecord insideTransactionRecord = buildTransactionEntity(insideWithdraw);
            transactionHelper.updateAndAddTogether(updatedRecord, insideTransactionRecord);
        } finally {
            accountLocker.releaseLock(accountFrom);
        }
    }

    private void verifyAccountExist(long accountTo) throws AccountNotFoundException {
        accountsRepository.getAccountById(accountTo).orElseThrow(() -> new AccountNotFoundException(accountTo));
    }

    private InsideTransactionRecord buildTransactionEntity(InsideWithdraw params) {
        return new InsideTransactionRecord(params.withdrawalId(), params.accountFrom(), params.accountTo(), params.amount(), InsideTransactionState.NEW, "");
    }
}
