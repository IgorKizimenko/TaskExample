package org.my.company.service.repository;

import org.my.company.service.configuration.AccountsRange;
import org.my.company.service.WithdrawalId;
import org.my.company.service.repository.records.OutsideTransactionRecord;
import org.my.company.service.repository.records.OutsideTransactionState;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Responsibility - keep {@link OutsideTransactionRecord}) and requests for them
 * notice: UUID is very bad like private key in real db, but for in memory - it's not that important.
 * If you change synchronized to something also, keep in mind - {@link OutsideTransactionHelper}, it uses this.
 */
@Repository
public class OutsideTransactionRepository {
    private final Map<WithdrawalId, OutsideTransactionRecord> outsideMap = new HashMap<>();

    public synchronized void add(OutsideTransactionRecord transactionRecord) {
        if (transactionRecord == null) {
            throw new IllegalArgumentException("can't process null");
        }
        if (outsideMap.containsKey(transactionRecord.uuid())) {
            throw new IllegalArgumentException("Called add for already presented id.");
        }

        outsideMap.put(transactionRecord.uuid(), transactionRecord);
    }

    public synchronized void update(OutsideTransactionRecord transactionRecord) {
        if (transactionRecord == null) {
            throw new IllegalArgumentException("can't process null");
        }
        if (!outsideMap.containsKey(transactionRecord.uuid())) {
            throw new IllegalArgumentException("Called update for not presented id.");
        }

        outsideMap.put(transactionRecord.uuid(), transactionRecord);
    }

    /**
     * minor: range could be ignored for now, because all accounts owned by this instance.
     */
    public synchronized List<OutsideTransactionRecord> findRequestsForSending(AccountsRange range) {
        return outsideMap.values().stream()
                .filter(i -> i.state() == OutsideTransactionState.NEW
                        && range.start() <= i.accountIdFrom()
                        && range.finish() >= i.accountIdFrom())
                .toList();
    }

    /**
     *
     * @param range it's current node account range.
     * @return list of records for processing.
     */
    public synchronized List<OutsideTransactionRecord> findRequestsForChecking(AccountsRange range) {
        return outsideMap.values().stream()
                .filter(i -> i.state() == OutsideTransactionState.SENT
                        && range.start() <= i.accountIdFrom()
                        && range.finish() >= i.accountIdFrom())
                .toList();
    }

    public synchronized Optional<OutsideTransactionRecord> findById(WithdrawalId params) {
        if (params == null) {
            return Optional.empty();
        }
        if (outsideMap.containsKey(params)) {
            return Optional.of(outsideMap.get(params));
        }

        return Optional.empty();
    }

    public synchronized Optional<OutsideTransactionState> findStateById(WithdrawalId params) {
        if (params == null) {
            return Optional.empty();
        }
        if (outsideMap.containsKey(params)) {
            return Optional.of(outsideMap.get(params).state());
        }

        return Optional.empty();
    }
}
