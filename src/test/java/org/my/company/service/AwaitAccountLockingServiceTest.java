package org.my.company.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AwaitAccountLockingServiceTest {
    @Mock
    private AccountLockingService service;

    @InjectMocks
    private AwaitAccountLockingService lockingService;


    @DisplayName("happy case with reptry, on second try will obtain the lock")
    @Test
    void awaitLock() {
        when(service.obtainLock(1L)).thenReturn(true);
        boolean result = lockingService.awaitLock(1L, Duration.ofMillis(4));

        Assertions.assertTrue(result);
    }

    @DisplayName("happy case with retry, on second try will obtain the lock")
    @Test
    void awaitLock2() {
        when(service.obtainLock(1L)).thenReturn(false).thenReturn(true);
        boolean result = lockingService.awaitLock(1L, Duration.ofMillis(50));

        Assertions.assertTrue(result);
    }

    @DisplayName("not happy case with retry, after awaiting not obtain the lock")
    @Test
    void awaitLock3() {
        when(service.obtainLock(1L)).thenReturn(false);
        boolean result = lockingService.awaitLock(1L, Duration.ofMillis(50));

        Assertions.assertFalse(result);
    }
}