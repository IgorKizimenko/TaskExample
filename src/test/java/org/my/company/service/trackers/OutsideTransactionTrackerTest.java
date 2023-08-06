package org.my.company.service.trackers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.my.company.service.Address;
import org.my.company.service.WithdrawalId;
import org.my.company.service.configuration.AccountsRange;
import org.my.company.service.flows.CheckStateOfOutsideTransactionFlow;
import org.my.company.service.flows.SendMoneyFromOutsideTransactionToDestinationFlow;
import org.my.company.service.repository.OutsideTransactionRepository;
import org.my.company.service.repository.records.OutsideTransactionRecord;
import org.my.company.service.repository.records.OutsideTransactionState;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutsideTransactionTrackerTest {
    @Mock
    private OutsideTransactionRepository outsideTransactionRepository;
    @Mock
    private AccountsRange accountRange;
    @Mock
    private SendMoneyFromOutsideTransactionToDestinationFlow sender;
    @Mock
    private CheckStateOfOutsideTransactionFlow checker;
    @InjectMocks
    private OutsideTransactionTracker tracker;

    @DisplayName("Sending, happy case, all fine, expect processing")
    @Test
    void checkOutcomeTransaction() {
        var request = mock(OutsideTransactionRecord.class);
        when(outsideTransactionRepository.findRequestsForSending(accountRange)).thenReturn(List.of(request));

        tracker.checkOutcomeTransaction();

        verify(sender, times(1)).process(request);
    }

    @DisplayName("Sending, not happy case, process throw exception, expect transaction goes to error state")
    @Test
    void checkOutcomeTransaction2() {
        var request = new OutsideTransactionRecord(new WithdrawalId(UUID.randomUUID()), 1L, new Address("sdf"), BigDecimal.TEN, OutsideTransactionState.NEW, "");
        when(outsideTransactionRepository.findRequestsForSending(accountRange)).thenReturn(List.of(request));
        var argCaptor = ArgumentCaptor.forClass(OutsideTransactionRecord.class);
        doNothing().when(outsideTransactionRepository).update(argCaptor.capture());
        doThrow(RuntimeException.class).when(sender).process(any());

        tracker.checkOutcomeTransaction();

        verify(sender, times(1)).process(request);
        var result = argCaptor.getValue();
        Assertions.assertEquals(OutsideTransactionState.ERROR, result.state());
    }

    @DisplayName("Checking, happy case, all fine, expect processing")
    @Test
    void checkOutcomeStateTransaction() {
        var request = mock(OutsideTransactionRecord.class);
        when(outsideTransactionRepository.findRequestsForChecking(accountRange)).thenReturn(List.of(request));

        tracker.checkStateOfOutcomeTransaction();

        verify(checker, times(1)).process(request);
    }

    @DisplayName("Checking, not happy case, process throw exception, expect transaction goes to error state")
    @Test
    void checkOutcomeStateTransaction2() {
        var request = new OutsideTransactionRecord(new WithdrawalId(UUID.randomUUID()), 1L, new Address("sdf"), BigDecimal.TEN, OutsideTransactionState.SENT, "");
        when(outsideTransactionRepository.findRequestsForChecking(accountRange)).thenReturn(List.of(request));
        var argCaptor = ArgumentCaptor.forClass(OutsideTransactionRecord.class);
        doNothing().when(outsideTransactionRepository).update(argCaptor.capture());
        doThrow(RuntimeException.class).when(checker).process(any());

        tracker.checkStateOfOutcomeTransaction();

        verify(checker, times(1)).process(request);
        var result = argCaptor.getValue();
        Assertions.assertEquals(OutsideTransactionState.ERROR, result.state());
    }
}