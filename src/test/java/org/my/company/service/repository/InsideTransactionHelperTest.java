package org.my.company.service.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.my.company.service.repository.records.AccountRecord;
import org.my.company.service.repository.records.InsideTransactionRecord;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsideTransactionHelperTest {
    @Mock
    private AccountsRepository accountsRepository;
    @Mock
    private InsideTransactionRepository insideTransactionRepository;
    @InjectMocks
    private InsideTransactionHelper helper;

    @DisplayName("Happy case, all good, expect 2 submissions")
    @Test
    void updateTogether() {
        var acc = mock(AccountRecord.class);
        when(acc.accountId()).thenReturn(1L);
        var transaction = mock(InsideTransactionRecord.class);
        when(accountsRepository.getAccountById(1L)).thenReturn(Optional.of(mock(AccountRecord.class)));
        //act
        helper.updateTogether(acc, transaction);

        verify(accountsRepository, times(1)).update(acc);
        verify(insideTransactionRepository, times(1)).update(transaction);
    }

    @DisplayName("Happy case, all good, expect 2 submissions, update + add")
    @Test
    void updateAndAddTogether() {
        var acc = mock(AccountRecord.class);
        when(acc.accountId()).thenReturn(1L);
        var transaction = mock(InsideTransactionRecord.class);
        when(accountsRepository.getAccountById(1L)).thenReturn(Optional.of(mock(AccountRecord.class)));
        //act
        helper.updateAndAddTogether(acc, transaction);

        verify(accountsRepository, times(1)).update(acc);
        verify(insideTransactionRepository, times(1)).add(transaction);
    }

    @DisplayName("Case with rollback. Exception on transaction. Expect repositories to be consistent.")
    @Test
    void updateTogether2() {
        var acc1 = mock(AccountRecord.class);
        var acc2 = mock(AccountRecord.class);
        when(acc2.accountId()).thenReturn(1L);
        var transaction = mock(InsideTransactionRecord.class);
        when(accountsRepository.getAccountById(1L)).thenReturn(Optional.of(acc1));
        doThrow(IllegalArgumentException.class).when(insideTransactionRepository).update(any());

        //act
        Assertions.assertThrows(IllegalArgumentException.class, () -> helper.updateTogether(acc2, transaction));

        verify(accountsRepository, times(1)).update(acc2);
        verify(accountsRepository, times(1)).update(acc1);
    }

    @DisplayName("Case with rollback. Exception on transaction. Expect repositories to be consistent.")
    @Test
    void updateAndAddTogether2() {
        var acc1 = mock(AccountRecord.class);
        var acc2 = mock(AccountRecord.class);
        when(acc2.accountId()).thenReturn(1L);
        var transaction = mock(InsideTransactionRecord.class);
        when(accountsRepository.getAccountById(1L)).thenReturn(Optional.of(acc1));
        doThrow(IllegalArgumentException.class).when(insideTransactionRepository).add(any());

        //act
        Assertions.assertThrows(IllegalArgumentException.class, () -> helper.updateAndAddTogether(acc2, transaction));

        verify(accountsRepository, times(1)).update(acc2);
        verify(accountsRepository, times(1)).update(acc1);
    }
}