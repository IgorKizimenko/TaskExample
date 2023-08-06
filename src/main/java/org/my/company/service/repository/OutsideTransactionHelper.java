package org.my.company.service.repository;

import lombok.AllArgsConstructor;
import org.my.company.service.repository.records.AccountRecord;
import org.my.company.service.repository.records.OutsideTransactionRecord;
import org.springframework.stereotype.Component;

/**
 * FIXME with jira number.
 * We don't have @Transactional or TransactionalManager in my "in memory db", that's why let's assume that it's here.
 * H2 + jpa will provide me this, but it's literally spring instruments based on annotations.
 * I did this 'strange' solution only for preventing this solution to be more spring-ish.
 * I expect what it's correct for now, but hard to maintain it in future.
 */
@Component
@AllArgsConstructor
public class OutsideTransactionHelper {
    private final AccountsRepository accountsRepository;
    private final OutsideTransactionRepository outsideTransactionRepository;

    public void updateTogether(AccountRecord updatedRecord,
                               OutsideTransactionRecord outsideTransaction) {
        //lock on repository, because it's monitor synchronized methods inside,
        // we need to prevent reads while we are changing the data.
        // the same sequence of locks will prevent us from deadlock.
        synchronized (outsideTransactionRepository) {
            synchronized (accountsRepository) {
                var prev = accountsRepository.getAccountById(updatedRecord.accountId()).orElseThrow();
                try {
                    accountsRepository.update(updatedRecord);
                    outsideTransactionRepository.update(outsideTransaction);
                } catch (RuntimeException e) {
                    //rollback
                    accountsRepository.update(prev);
                    throw e;
                }
            }
        }
    }

    public void updateAndAddTogether(AccountRecord updatedRecord,
                                     OutsideTransactionRecord outsideTransaction) {
        //lock on repository, because it's monitor synchronized methods inside,
        // we need to prevent reads while we are changing the data.
        // the same sequence of locks will prevent us from deadlock.
        synchronized (outsideTransactionRepository) {
            synchronized (accountsRepository) {
                var prev = accountsRepository.getAccountById(updatedRecord.accountId()).orElseThrow();
                try {
                    accountsRepository.update(updatedRecord);
                    outsideTransactionRepository.add(outsideTransaction);
                } catch (RuntimeException e) {
                    //rollback
                    accountsRepository.update(prev);
                    throw e;
                }
            }
        }
    }

}
