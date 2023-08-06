package org.my.company.service.flows;

import lombok.AllArgsConstructor;
import org.my.company.service.WithdrawalService;
import org.my.company.service.repository.OutsideTransactionRepository;
import org.my.company.service.repository.records.OutsideTransactionRecord;
import org.my.company.service.repository.records.OutsideTransactionState;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SendMoneyFromOutsideTransactionToDestinationFlow {
    private final OutsideTransactionRepository outsideTransactionRepository;
    private final WithdrawalService withdrawalService;

    public void process(OutsideTransactionRecord outsideTransaction) {
        if (outsideTransaction.state() != OutsideTransactionState.NEW) {
            throw new IllegalArgumentException("this brunch was made only for NEW state");
        }

        //gray area, not transactional, but idempotency will help, possible double sending only.
        withdrawalService.requestWithdrawal(outsideTransaction.uuid(), outsideTransaction.address(), outsideTransaction.amount());
        outsideTransactionRepository.update(outsideTransaction.buildSent());
        //end of gray area.
    }
}
