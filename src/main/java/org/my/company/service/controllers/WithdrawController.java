package org.my.company.service.controllers;

import lombok.AllArgsConstructor;
import org.my.company.service.UsersWithdrawalState;
import org.my.company.service.WithdrawalId;
import org.my.company.service.flows.MoveMoneyFromAccountToInsideTransactionFlow;
import org.my.company.service.flows.MoveMoneyFromAccountToOutsideTransactionFlow;
import org.my.company.service.flows.TransactionView;
import org.my.company.service.flows.exceptions.AccountNotFoundException;
import org.my.company.service.flows.exceptions.NotEnoughMoneyException;
import org.my.company.service.flows.income.InsideWithdraw;
import org.my.company.service.flows.income.OutsideWithdraw;
import org.my.company.service.validation.DuplicateRequestException;
import org.my.company.service.validation.RequestIdDuplicateCheck;
import org.my.company.service.validation.WithdrawValidator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Main task controller.
 */
@RestController
@AllArgsConstructor
public class WithdrawController {

    private final WithdrawValidator incomeValidator;
    private final MoveMoneyFromAccountToInsideTransactionFlow insideFlow;
    private final MoveMoneyFromAccountToOutsideTransactionFlow outcomeFlow;
    private final TransactionView stateView;
    private final RequestIdDuplicateCheck requestIdDuplicateCheck;

    @PutMapping(path = "/requestWithdrawal")
    public void requestWithdrawal(String uuid, long accountId, String address, BigDecimal amount) throws DuplicateRequestException, NotEnoughMoneyException, AccountNotFoundException {
        OutsideWithdraw params = incomeValidator.validate(uuid, accountId, address, amount);
        requestIdDuplicateCheck.registerNewRequestOrThrow(uuid);

        outcomeFlow.process(params);
    }

    @PutMapping(path = "/requestWithdrawalToAnotherUser")
    public void requestWithdrawalToAnotherUser(String uuid, long accountIdFrom, long accountIdTo, BigDecimal amount) throws AccountNotFoundException, NotEnoughMoneyException, DuplicateRequestException {
        InsideWithdraw params = incomeValidator.validate(uuid, accountIdFrom, accountIdTo, amount);
        requestIdDuplicateCheck.registerNewRequestOrThrow(uuid);

        insideFlow.process(params);
    }

    @GetMapping(path = "/getWithdrawalState")
    public UsersWithdrawalState getWithdrawalState(String uuid) {
        WithdrawalId params = incomeValidator.validate(uuid);

        return stateView.findAndComputeUserState(params);
    }
}
