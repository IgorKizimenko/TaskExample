package org.my.company.service.flows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.my.company.service.Address;
import org.my.company.service.AwaitAccountLockingService;
import org.my.company.service.WithdrawalId;
import org.my.company.service.WithdrawalService;
import org.my.company.service.WithdrawalState;
import org.my.company.service.repository.AccountsRepository;
import org.my.company.service.repository.OutsideTransactionHelper;
import org.my.company.service.repository.OutsideTransactionRepository;
import org.my.company.service.repository.records.AccountRecord;
import org.my.company.service.repository.records.OutsideTransactionRecord;
import org.my.company.service.repository.records.OutsideTransactionState;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckStateOfOutsideTransactionFlowTest {

    @Mock
    private AccountsRepository repository;
    @Mock
    private OutsideTransactionRepository outsideTransactionRepository;
    @Mock
    private OutsideTransactionHelper transactionHelper;
    @Mock
    private AwaitAccountLockingService accountLocker;
    @Mock
    private WithdrawalService withdrawalService;
    @InjectMocks
    private CheckStateOfOutsideTransactionFlow flow;
    @DisplayName("Happy case: flow get lock, all fine, expect submission to outcome repo with complete")
    @Test
    void process() {
        //assert
        var acc = new AccountRecord(1, BigDecimal.valueOf(100));
        var requestId = new WithdrawalId(UUID.randomUUID());
        when(withdrawalService.getRequestState(requestId)).thenReturn(WithdrawalState.COMPLETED);
        ArgumentCaptor<OutsideTransactionRecord> argumentCaptor = ArgumentCaptor.forClass(OutsideTransactionRecord.class);
        doNothing().when(outsideTransactionRepository).update(argumentCaptor.capture());
        //act
        flow.process(new OutsideTransactionRecord(requestId, acc.accountId(), new Address("sdf"), BigDecimal.TEN, OutsideTransactionState.SENT, ""));
        //check
        verify(outsideTransactionRepository, timeout(1)).update(any());
        verifyNoInteractions(repository, accountLocker);
        Assertions.assertEquals(OutsideTransactionState.COMPLETE, argumentCaptor.getValue().state());
    }

    @DisplayName("Happy case 2: flow get lock, all fine, we've got reject - expect submission to outcome repo with error and return api calls.")
    @Test
    void process2() {
        //assert
        var acc = new AccountRecord(1, BigDecimal.valueOf(100));
        var requestId = new WithdrawalId(UUID.randomUUID());
        when(repository.getAccountById(anyLong())).thenReturn(Optional.of(acc));
        when(accountLocker.awaitLock(anyLong(), any())).thenReturn(true);
        ArgumentCaptor<OutsideTransactionRecord> argumentCaptor = ArgumentCaptor.forClass(OutsideTransactionRecord.class);
        doNothing().when(transactionHelper).updateTogether(any(), argumentCaptor.capture());
        when(withdrawalService.getRequestState(requestId)).thenReturn(WithdrawalState.FAILED);
        //act
        flow.process(new OutsideTransactionRecord(requestId, acc.accountId(), new Address("sdf"), BigDecimal.TEN, OutsideTransactionState.SENT, ""));
        //check
        verify(transactionHelper, times(1)).updateTogether(any(), any());
        verify(accountLocker, times(1)).awaitLock(anyLong(), any());
        verify(accountLocker, times(1)).releaseLock(anyLong());
        Assertions.assertEquals(OutsideTransactionState.REJECTED, argumentCaptor.getValue().state());
    }

    @DisplayName("Not Happy case: flow failed to obtain the lock, expect to have failed to reject.")
    @Test
    void process3() {
        //assert
        var acc = new AccountRecord(1, BigDecimal.valueOf(100));
        var requestId = new WithdrawalId(UUID.randomUUID());
        when(accountLocker.awaitLock(anyLong(), any())).thenReturn(false);
        ArgumentCaptor<OutsideTransactionRecord> argumentCaptor = ArgumentCaptor.forClass(OutsideTransactionRecord.class);
        doNothing().when(outsideTransactionRepository).update(argumentCaptor.capture());
        when(withdrawalService.getRequestState(requestId)).thenReturn(WithdrawalState.FAILED);
        //act
        flow.process(new OutsideTransactionRecord(requestId, acc.accountId(), new Address("sdf"), BigDecimal.TEN, OutsideTransactionState.SENT, ""));
        //check
        verify(outsideTransactionRepository, timeout(1)).update(any());
        verifyNoInteractions(repository);
        Assertions.assertEquals(OutsideTransactionState.REJECT_FAILED, argumentCaptor.getValue().state());
    }
}