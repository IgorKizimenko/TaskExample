package org.my.company.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Inside service lock.
 * Needed for situation, then you have 2 rest calls at the same time to one account.
 */
@Component
@AllArgsConstructor
public class AwaitAccountLockingService {
    private static final int CONTEXT_SWITCHING_MAX_TIME_CHECK = 1000;

    //guarded by writeLock
    private final AccountLockingService service;
    private final Object checkLock = new Object();
    private final Object writeLock = new Object();

    /**
     * @param accountId requested account id.
     * @return true if obtained, false if not.
     */
    public boolean awaitLock(long accountId, Duration duration) {
        long mils = duration.toMillis();
        long timeToMoveOn = System.currentTimeMillis() + mils;
        while (System.currentTimeMillis() < timeToMoveOn) {
            boolean isOwnByMe;
            synchronized (writeLock) {
                isOwnByMe = service.obtainLock(accountId);
            }

            if (!isOwnByMe) {
                synchronized (checkLock) {
                    try {
                        long timeToWait = compute(mils);
                        checkLock.wait(timeToWait);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException();
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * 1s is protection from possibility of context switching between obtain lock and wait
     * we possibly can miss notifyAll. But in this case we'll wait at most 1s.
     * Basically we have from 1ms to 1000 ms interval for checking.
     * In case then we have, for example, 50 ms to wait, it would be only 50ms.
     */
    private long compute(long mils) {
        return Math.min(Math.max(1, mils - System.currentTimeMillis()), CONTEXT_SWITCHING_MAX_TIME_CHECK);
    }

    /**
     * @param accountId requested account id.
     * @throws IllegalArgumentException in case then you tried to obtain not presented lock.
     */
    public void releaseLock(long accountId) {
        synchronized (writeLock) {
            service.releaseLock(accountId);
        }
        synchronized (checkLock) {
            checkLock.notifyAll(); //let all waiters check their lock.
        }
    }
}
