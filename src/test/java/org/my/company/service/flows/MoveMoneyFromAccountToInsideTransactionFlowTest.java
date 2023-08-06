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
import org.my.company.service.flows.exceptions.AccountNotFoundException;
import org.my.company.service.flows.exceptions.NotEnoughMoneyException;
import org.my.company.service.flows.income.InsideWithdraw;
import org.my.company.service.repository.AccountsRepository;
import org.my.company.service.repository.InsideTransactionHelper;
import org.my.company.service.repository.records.AccountRecord;
import org.my.company.service.repository.records.InsideTransactionRecord;
import org.my.company.service.repository.records.InsideTransactionState;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoveMoneyFromAccountToInsideTransactionFlowTest {
    @Mock
    private AccountsRepository accountsRepository;
    @Mock
    private InsideTransactionHelper transactionHelper;
    @Mock
    private AwaitAccountLockingService accountLocker;

    @InjectMocks
    private MoveMoneyFromAccountToInsideTransactionFlow flow;

    @DisplayName("Happy case, all presented, expect movement")
    @Test
    void process() throws NotEnoughMoneyException, AccountNotFoundException {
        var request = new InsideWithdraw(new WithdrawalId(UUID.randomUUID()), 1L, 2L, BigDecimal.TEN);
        var acc1 = new AccountRecord(1L, BigDecimal.valueOf(100));
        var acc2 = new AccountRecord(2L, BigDecimal.valueOf(100));
        when(accountsRepository.getAccountById(1L)).thenReturn(Optional.of(acc1));
        when(accountsRepository.getAccountById(2L)).thenReturn(Optional.of(acc2));
        when(accountLocker.awaitLock(anyLong(), any())).thenReturn(true);
        var argumentCaptor = ArgumentCaptor.forClass(InsideTransactionRecord.class);
        doNothing().when(transactionHelper).updateAndAddTogether(any(), argumentCaptor.capture());
        //act
        flow.process(request);
        //assert
        var record = argumentCaptor.getValue();
        Assertions.assertEquals(InsideTransactionState.NEW, record.state());
        Assertions.assertEquals(0, BigDecimal.TEN.compareTo(record.amount()));
        Assertions.assertEquals(2L, record.accountIdTo());
        Assertions.assertEquals(1L, record.accountIdFrom());
    }

    @DisplayName("Not enough money. Expect exception")
    @Test
    void process2() {
        var request = new InsideWithdraw(new WithdrawalId(UUID.randomUUID()), 1L, 2L, BigDecimal.TEN);
        var acc1 = new AccountRecord(1L, BigDecimal.valueOf(9));
        var acc2 = new AccountRecord(1L, BigDecimal.valueOf(9));
        when(accountsRepository.getAccountById(1L)).thenReturn(Optional.of(acc1));
        when(accountsRepository.getAccountById(2L)).thenReturn(Optional.of(acc2));
        when(accountLocker.awaitLock(anyLong(), any())).thenReturn(true);
        //act
        Assertions.assertThrows(NotEnoughMoneyException.class, () -> flow.process(request));
    }

    @DisplayName("From account does not exist. Expect exception")
    @Test
    void process3() {
        var request = new InsideWithdraw(new WithdrawalId(UUID.randomUUID()), 1L, 2L, BigDecimal.TEN);
        var acc2 = new AccountRecord(1L, BigDecimal.valueOf(9));
        when(accountsRepository.getAccountById(2L)).thenReturn(Optional.of(acc2));
        when(accountsRepository.getAccountById(1L)).thenReturn(Optional.empty());
        when(accountLocker.awaitLock(anyLong(), any())).thenReturn(true);
        //act
        Assertions.assertThrows(AccountNotFoundException.class, () -> flow.process(request));
    }

    @DisplayName("To account does not exist. Expect exception")
    @Test
    void process4() {
        var request = new InsideWithdraw(new WithdrawalId(UUID.randomUUID()), 1L, 2L, BigDecimal.TEN);
        when(accountsRepository.getAccountById(2L)).thenReturn(Optional.empty());
        //act
        Assertions.assertThrows(AccountNotFoundException.class, () -> flow.process(request));
    }


    @DisplayName("Obtain the lock failed. Throwing exception expected")
    @Test
    void process5() {
        var request = new InsideWithdraw(new WithdrawalId(UUID.randomUUID()), 1L, 2L, BigDecimal.TEN);
        var acc2 = new AccountRecord(2L, BigDecimal.valueOf(100));
        when(accountsRepository.getAccountById(2L)).thenReturn(Optional.of(acc2));
        when(accountLocker.awaitLock(anyLong(), any())).thenReturn(false);
        //act
        Assertions.assertThrows(IllegalStateException.class, () -> flow.process(request));
    }

}