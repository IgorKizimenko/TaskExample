package org.my.company.service;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Inside service lock.
 * Needed for situation, then you have 2 rest calls at the same time to one account.
 * NOT THREAD SAFE! protected by {@link AwaitAccountLockingService}
 */
@Component
public class AccountLockingService {
    private final Set<Long> lockedIds = new HashSet<>();

    /**
     *
     * @param accountId requested account id.
     * @return true if obtained, false if not.
     */
    public boolean obtainLock(long accountId) {
        return lockedIds.add(accountId);
    }

    /**
     *
     * @param accountId requested account id.
     * @throws IllegalArgumentException in case then you tried to obtain not presented lock.
     */
    public void releaseLock(long accountId) {
        boolean isReleased = lockedIds.remove(accountId);
        if (!isReleased) {
            throw new IllegalArgumentException("Unexpected! Someone trying to release the lock (" + accountId + ") which is not present.");
        }
    }
}
