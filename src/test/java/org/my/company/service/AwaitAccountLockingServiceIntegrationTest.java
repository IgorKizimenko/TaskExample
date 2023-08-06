package org.my.company.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

class AwaitAccountLockingServiceIntegrationTest {

    @DisplayName("happy case with multiply threads, expect that all will obtain the lock do their job and leave.")
    @Test
    void awaitLock() throws InterruptedException {
        AwaitAccountLockingService lockingService = new AwaitAccountLockingService(new AccountLockingService());
        int count = 100;
        Thread[] threads = new Thread[count];
        AtomicBoolean[] jobs = new AtomicBoolean[count];
        for (int i = 0; i < threads.length; i++) {
            final var job = new AtomicBoolean(false);
            jobs[i] = job;
            threads[i] = new Thread(() -> {
                lockingService.awaitLock(1, Duration.of(2, ChronoUnit.SECONDS));
                job.set(true);
                lockingService.releaseLock(1);
            });
        }

        for (Thread value : threads) {
            value.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        Assertions.assertTrue(Arrays.stream(jobs).allMatch(AtomicBoolean::get));
    }

    //Heavy test.
    @DisplayName("happy case with multiply threads, with sleep, expect that all will obtain the lock do their job and leave.")
    @Test
    void awaitLock2() throws InterruptedException {
        AwaitAccountLockingService lockingService = new AwaitAccountLockingService(new AccountLockingService());
        int count = 100;
        Thread[] threads = new Thread[count];
        AtomicBoolean[] jobs = new AtomicBoolean[count];
        for (int i = 0; i < threads.length; i++) {
            final var job = new AtomicBoolean(false);
            jobs[i] = job;
            threads[i] = new Thread(() -> {
                lockingService.awaitLock(1, Duration.of(20, ChronoUnit.SECONDS));
                job.set(true);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException();
                }
                lockingService.releaseLock(1);
            });
        }

        for (Thread value : threads) {
            value.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        Assertions.assertTrue(Arrays.stream(jobs).allMatch(AtomicBoolean::get));
    }
}
