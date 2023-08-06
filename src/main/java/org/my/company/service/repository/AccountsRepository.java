package org.my.company.service.repository;

import org.my.company.service.repository.records.AccountRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * It's in-memory storage for accounts.
 * In more "spring-ish" world I assume it would be jpa repo + h2/(postgres + redis hot data boosters).
 * <p>
 * Currently, it's records (immutable) +
 */
@Repository
public class AccountsRepository {

    private final ConcurrentHashMap<Long, AccountRecord> records = new ConcurrentHashMap<>();

    public AccountsRepository(@Value("#{${accounts}}") Map<Long, BigDecimal> accounts) {
        for (var acc : accounts.entrySet()) {
            records.put(acc.getKey(), new AccountRecord(acc.getKey(), acc.getValue()));
        }
    }

    public Optional<AccountRecord> getAccountById(long accountId) {
        return Optional.ofNullable(records.get(accountId));
    }

    public void update(AccountRecord updatedRecord) {
        records.put(updatedRecord.accountId(), updatedRecord);
    }
}
