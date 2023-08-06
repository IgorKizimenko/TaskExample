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
import org.my.company.service.WithdrawalId;
import org.my.company.service.WithdrawalService;
import org.my.company.service.repository.OutsideTransactionRepository;
import org.my.company.service.repository.records.OutsideTransactionRecord;
import org.my.company.service.repository.records.OutsideTransactionState;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SendMoneyFromOutsideTransactionToDestinationFlowTest {
    @Mock
    private OutsideTransactionRepository outsideTransactionRepository;
    @Mock
    private WithdrawalService withdrawalService;
    @InjectMocks
    private SendMoneyFromOutsideTransactionToDestinationFlow flow;

    @DisplayName("happy case, sending should be fine, expect commit transaction as SENT")
    @Test
    void process() {
        var id = new WithdrawalId(UUID.randomUUID());
        var request = new OutsideTransactionRecord(id, 1L, new Address("sdf"), BigDecimal.TEN, OutsideTransactionState.NEW, "");

        var captor = ArgumentCaptor.forClass(OutsideTransactionRecord.class);
        doNothing().when(outsideTransactionRepository).update(captor.capture());

        flow.process(request);

        verify(withdrawalService, times(1)).requestWithdrawal(eq(id), any(), any());
        verify(outsideTransactionRepository, times(1)).update(any());
        var updRequest = captor.getValue();
        Assertions.assertEquals(OutsideTransactionState.SENT, updRequest.state());
    }
}