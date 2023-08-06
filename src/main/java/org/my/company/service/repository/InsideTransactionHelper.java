package org.my.company.service.repository;

import lombok.AllArgsConstructor;
import org.my.company.service.repository.records.AccountRecord;
import org.my.company.service.repository.records.InsideTransactionRecord;
import org.springframework.stereotype.Component;

/**
 * FIXME with jira number.
 * We don't have @Transactional or TransactionalManager in my "in memory db", that's why let's assume that it's here.
 * H2 + jpa will provide me this, but it's literally spring instruments based on annotations.
 * I did this 'strange' solution only for preventing this project to be more 'spring'-ish.
 * I expect what it's correct for now, but hard to support in long term.
 */
@Component
@AllArgsConstructor
public class InsideTransactionHelper {
    private final AccountsRepository accountsRepository;
    private final InsideTransactionRepository insideTransactionRepository;

    public void updateTogether(AccountRecord updatedRecord,
                               InsideTransactionRecord transactionRecord) {
        //lock on repository, because it's monitor object for synchronized methods inside,
        // we need to prevent reads while we are changing the data.
        // the same sequence of locks in methods will prevent us from deadlock.
        synchronized (insideTransactionRepository) {
            synchronized (accountsRepository) {
                var prev = accountsRepository.getAccountById(updatedRecord.accountId()).orElseThrow();
                try {
                    accountsRepository.update(updatedRecord);
                    insideTransactionRepository.update(transactionRecord);
                } catch (RuntimeException e) {
                    //rollback
                    accountsRepository.update(prev);
                    throw e;
                }
            }
        }
    }

    public void updateAndAddTogether(AccountRecord updatedRecord, InsideTransactionRecord insideTransactionRecord) {
        synchronized (insideTransactionRepository) {
            synchronized (accountsRepository) {
                var prev = accountsRepository.getAccountById(updatedRecord.accountId()).orElseThrow();
                try {
                    accountsRepository.update(updatedRecord);
                    insideTransactionRepository.add(insideTransactionRecord);
                } catch (RuntimeException e) {
                    //rollback
                    accountsRepository.update(prev);
                    throw e;
                }
            }
        }
    }
}
