package org.my.company.service.flows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.my.company.service.AwaitAccountLockingService;
import org.my.company.service.WithdrawalId;
import org.my.company.service.repository.AccountsRepository;
import org.my.company.service.repository.InsideTransactionHelper;
import org.my.company.service.repository.InsideTransactionRepository;
import org.my.company.service.repository.records.AccountRecord;
import org.my.company.service.repository.records.InsideTransactionRecord;
import org.my.company.service.repository.records.InsideTransactionState;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoveMoneyFromInsideTransactionToDestinationAccountFlowTest {
    @Mock
    private AccountsRepository accountsRepository;
    @Mock
    private InsideTransactionRepository transactionRepository;
    @Mock
    private AwaitAccountLockingService accountLocker;
    @Mock
    private InsideTransactionHelper transactionHelper;
    @InjectMocks
    private MoveMoneyFromInsideTransactionToDestinationAccountFlow flow;

    @DisplayName("Happy case, transfer money from queue to account.")
    @Test
    void process() {
        var id = new WithdrawalId(UUID.randomUUID());
        InsideTransactionRecord record = new InsideTransactionRecord(id, 1L, 2L, BigDecimal.TEN, InsideTransactionState.NEW, "");
        when(accountLocker.awaitLock(eq(2L),any())).thenReturn(true);
        when(accountsRepository.getAccountById(2L)).thenReturn(Optional.of(new AccountRecord(2L, BigDecimal.ZERO)));
        var argumentCaptor = ArgumentCaptor.forClass(InsideTransactionRecord.class);
        var argumentCaptor2 = ArgumentCaptor.forClass(AccountRecord.class);
        doNothing().when(transactionHelper).updateTogether(argumentCaptor2.capture(), argumentCaptor.capture());

        //act
        flow.process(record);
        //assert
        var tr = argumentCaptor.getValue();
        Assertions.assertEquals(InsideTransactionState.COMPLETE, tr.state());
        var acc = argumentCaptor2.getValue();
        Assertions.assertEquals(0, BigDecimal.TEN.compareTo(acc.freeAmount()));
    }

    @DisplayName("transfer money from queue to non existing account. Expect transaction marked as error.")
    @Test
    void process2() {
        var id = new WithdrawalId(UUID.randomUUID());
        InsideTransactionRecord record = new InsideTransactionRecord(id, 1L, 2L, BigDecimal.TEN, InsideTransactionState.NEW, "");
        when(accountLocker.awaitLock(eq(2L),any())).thenReturn(true);
        when(accountsRepository.getAccountById(2L)).thenReturn(Optional.empty());
        var argumentCaptor = ArgumentCaptor.forClass(InsideTransactionRecord.class);
        doNothing().when(transactionRepository).update(argumentCaptor.capture());

        //act
        flow.process(record);
        //assert
        var tr = argumentCaptor.getValue();
        Assertions.assertEquals(InsideTransactionState.ERROR, tr.state());
    }

    @DisplayName("Obtain lock failed. Expect transaction to be FAILED_TO_COMPLETE")
    @Test
    void process3() {
        var id = new WithdrawalId(UUID.randomUUID());
        InsideTransactionRecord record = new InsideTransactionRecord(id, 1L, 2L, BigDecimal.TEN, InsideTransactionState.NEW, "");
        when(accountLocker.awaitLock(eq(2L),any())).thenReturn(false);
        var argumentCaptor = ArgumentCaptor.forClass(InsideTransactionRecord.class);
        doNothing().when(transactionRepository).update(argumentCaptor.capture());

        //act
        flow.process(record);
        //assert
        var tr = argumentCaptor.getValue();
        Assertions.assertEquals(InsideTransactionState.FAILED_TO_COMPLETE, tr.state());
    }
}