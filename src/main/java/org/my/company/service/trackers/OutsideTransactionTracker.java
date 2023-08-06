package org.my.company.service.trackers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.my.company.service.configuration.AccountsRange;
import org.my.company.service.flows.CheckStateOfOutsideTransactionFlow;
import org.my.company.service.flows.SendMoneyFromOutsideTransactionToDestinationFlow;
import org.my.company.service.repository.OutsideTransactionRepository;
import org.my.company.service.repository.records.OutsideTransactionRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Component
public class OutsideTransactionTracker {
    private final OutsideTransactionRepository outsideTransactionRepository;
    private final AccountsRange accountRange;
    private final SendMoneyFromOutsideTransactionToDestinationFlow sender;
    private final CheckStateOfOutsideTransactionFlow checker;

    /**
     * 100ms delay check in db.
     */
    @Scheduled(fixedDelayString = "${outcome-transaction-check.fixed-delay-in-milliseconds:100}")
    public void checkOutcomeTransaction() {
        List<OutsideTransactionRecord> recordList = outsideTransactionRepository.findRequestsForSending(accountRange);

        for (var request : recordList) {
            try {
                sender.process(request);
            } catch (RuntimeException e) {
                log.error("unexpected error, state is undefined. Please, take a look", e);
                outsideTransactionRepository.update(request.buildError(e.getMessage()));
            }
        }
    }

    /**
     * 100ms delay check in db.
     */
    @Scheduled(fixedDelayString = "${outcome-transaction-check.fixed-delay-in-milliseconds:100}")
    public void checkStateOfOutcomeTransaction() {
        List<OutsideTransactionRecord> recordList = outsideTransactionRepository.findRequestsForChecking(accountRange);

        for (var request : recordList) {
            try {
                checker.process(request);
            } catch (RuntimeException e) {
                log.error("unexpected error, state is undefined. Please, take a look", e);
                outsideTransactionRepository.update(request.buildError(e.getMessage()));
            }
        }
    }
}
