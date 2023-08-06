package org.my.company.service.trackers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.my.company.service.WithdrawalId;
import org.my.company.service.configuration.AccountsRange;
import org.my.company.service.flows.MoveMoneyFromInsideTransactionToDestinationAccountFlow;
import org.my.company.service.repository.InsideTransactionRepository;
import org.my.company.service.repository.records.InsideTransactionRecord;
import org.my.company.service.repository.records.InsideTransactionState;

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
class IncomeTransactionTrackerTest {
    @Mock
    private InsideTransactionRepository insideTransactionRepository;
    @Mock
    private AccountsRange accountRange;
    @Mock
    private MoveMoneyFromInsideTransactionToDestinationAccountFlow flow;

    @InjectMocks
    private IncomeTransactionTracker tracker;

    @DisplayName("happy case, all fine, expect processing")
    @Test
    void checkIncomeTransaction() {
        var request = mock(InsideTransactionRecord.class);
        when(insideTransactionRepository.findInsideRequestsTo(accountRange)).thenReturn(List.of(request));

        tracker.checkIncomeTransaction();

        verify(flow, times(1)).process(request);
    }

    @DisplayName("not happy case, process throw exception, expect transaction goes to error state")
    @Test
    void checkIncomeTransaction2() {
        var request = new InsideTransactionRecord(new WithdrawalId(UUID.randomUUID()), 1L, 2L, BigDecimal.TEN, InsideTransactionState.NEW, "");
        when(insideTransactionRepository.findInsideRequestsTo(accountRange)).thenReturn(List.of(request));
        var argCaptor = ArgumentCaptor.forClass(InsideTransactionRecord.class);
        doNothing().when(insideTransactionRepository).update(argCaptor.capture());
        doThrow(RuntimeException.class).when(flow).process(any());

        tracker.checkIncomeTransaction();

        verify(flow, times(1)).process(request);
        var result = argCaptor.getValue();
        Assertions.assertEquals(InsideTransactionState.ERROR, result.state());
    }
}