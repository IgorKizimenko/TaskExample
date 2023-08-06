package org.my.company.service.flows;

import lombok.AllArgsConstructor;
import org.my.company.service.UsersWithdrawalState;
import org.my.company.service.WithdrawalId;
import org.my.company.service.repository.InsideTransactionRepository;
import org.my.company.service.repository.OutsideTransactionRepository;
import org.springframework.stereotype.Service;

/**
 * Idea - we have different states for both types and users should see only some subsequence of types.
 * Some States, related to failed to obtain the lock (should be extremely rare) but we can't lie to user and say what it's complete,
 * that's why it's in progress.
 * Error - it's special state meaning what we have issue in code and receive some unexpected/undefined state.
 * User will see in the progress in this case and support should solve the problem.
 */
@Service
@AllArgsConstructor
public class TransactionView {
    private final InsideTransactionRepository repository;
    private final OutsideTransactionRepository outsideTransactionRepository;

    public UsersWithdrawalState findAndComputeUserState(WithdrawalId params) {

        var tr = repository.findStateById(params);

        return tr.map(insideTransactionState -> switch (insideTransactionState) {
            //even if it's error - it's not a terminal state.
            case NEW, ERROR, FAILED_TO_COMPLETE -> UsersWithdrawalState.PROCESSING;
            case COMPLETE -> UsersWithdrawalState.COMPLETED;
        }).orElseGet(() -> tryFindInOutcome(params));
    }

    private UsersWithdrawalState tryFindInOutcome(WithdrawalId params) {
        var result = outsideTransactionRepository.findStateById(params);
        return result.map(outcomeTransactionState -> switch (outcomeTransactionState) {
            case NEW, SENT, REJECT_FAILED, ERROR -> UsersWithdrawalState.PROCESSING;
            case COMPLETE -> UsersWithdrawalState.COMPLETED;
            case REJECTED -> UsersWithdrawalState.REJECTED;
        }).orElse(UsersWithdrawalState.NOT_FOUND);
    }
}
