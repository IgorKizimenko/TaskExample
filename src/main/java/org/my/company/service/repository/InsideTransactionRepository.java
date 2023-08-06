package org.my.company.service.repository;

import org.my.company.service.configuration.AccountsRange;
import org.my.company.service.WithdrawalId;
import org.my.company.service.repository.records.InsideTransactionRecord;
import org.my.company.service.repository.records.InsideTransactionState;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Responsibility - keep *TransactionRecords ({@link InsideTransactionRecord} and requests for them
 * notice: UUID is very bad like private key in real db, but for in memory - it's not that important.
 * If you change synchronized to something also, keep in mind - {@link InsideTransactionHelper}, it uses this.
 */
@Repository
public class InsideTransactionRepository {

    private final Map<WithdrawalId, InsideTransactionRecord> insideMap = new HashMap<>();

    public synchronized void add(InsideTransactionRecord insideTransactionRecord) {
        if (insideTransactionRecord == null) {
            throw new IllegalArgumentException("can't process null");
        }
        if (insideMap.containsKey(insideTransactionRecord.uuid())) {
            throw new IllegalArgumentException("Called add for already presented id.");
        }

        insideMap.put(insideTransactionRecord.uuid(), insideTransactionRecord);

    }

    public synchronized void update(InsideTransactionRecord insideTransactionRecord) {
        if (insideTransactionRecord == null) {
            throw new IllegalArgumentException("can't process null");
        }
        if (!insideMap.containsKey(insideTransactionRecord.uuid())) {
            throw new IllegalArgumentException("Called update for not presented id.");
        }

        insideMap.put(insideTransactionRecord.uuid(), insideTransactionRecord);
    }

    /**
     * minor: range could be ignored for now, because all accounts owned by this instance.
     */
    public synchronized List<InsideTransactionRecord> findInsideRequestsTo(AccountsRange range) {
        return insideMap.values().stream()
                .filter(i -> i.state() == InsideTransactionState.NEW
                        && range.start() <= i.accountIdTo()
                        && range.finish() >= i.accountIdTo())
                .toList();
    }

    public synchronized Optional<InsideTransactionState> findStateById(WithdrawalId params) {
        if (params == null) {
            return Optional.empty();
        }
        if (insideMap.containsKey(params)) {
            return Optional.of(insideMap.get(params).state());
        }

        return Optional.empty();
    }

}
