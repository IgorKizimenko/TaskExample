package org.my.company.service.flows;

import lombok.AllArgsConstructor;
import org.my.company.service.AwaitAccountLockingService;
import org.my.company.service.repository.AccountsRepository;
import org.my.company.service.repository.InsideTransactionHelper;
import org.my.company.service.repository.InsideTransactionRepository;
import org.my.company.service.repository.records.AccountRecord;
import org.my.company.service.repository.records.InsideTransactionRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@AllArgsConstructor
public class MoveMoneyFromInsideTransactionToDestinationAccountFlow {
    private final AccountsRepository accountsRepository;
    private final InsideTransactionRepository transactionRepository;
    private final AwaitAccountLockingService accountLocker;
    private final InsideTransactionHelper transactionHelper;
    @Value("${move-from-transaction-to-account.max-lock-await:1m}")
    private final Duration maxLockWait;

    public void process(InsideTransactionRecord transaction) {
        long accountIdTo = transaction.accountIdTo();
        boolean isLockedByYou = accountLocker.awaitLock(accountIdTo, maxLockWait);
        if (!isLockedByYou) {
            transactionRepository.update(transaction.buildAsFailedToComplete("failed to obtain the lock on account."));
            return;
        }
        try {
            Optional<AccountRecord> accountRecord = accountsRepository.getAccountById(accountIdTo);
            if (accountRecord.isPresent()) {
                AccountRecord updatedRecord = accountRecord.get().addAmount(transaction.amount());
                transactionHelper.updateTogether(updatedRecord, transaction.buildAsCompleted());
            } else {
                transactionRepository.update(transaction.buildAsError("account id is not presented in Db."));
            }
        } finally {
            accountLocker.releaseLock(accountIdTo);
        }
    }

}
