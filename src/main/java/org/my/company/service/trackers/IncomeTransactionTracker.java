package org.my.company.service.trackers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.my.company.service.configuration.AccountsRange;
import org.my.company.service.flows.MoveMoneyFromInsideTransactionToDestinationAccountFlow;
import org.my.company.service.repository.InsideTransactionRepository;
import org.my.company.service.repository.records.InsideTransactionRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Responsibility - wake up once in timeframe, check the db queue and process it sequentially.
 * here you can see only one thread, because all in memory. In prod-code you most probably will see a thread pool + futures.
 */
@Slf4j
@Service
@AllArgsConstructor
public class IncomeTransactionTracker {
    private final InsideTransactionRepository insideTransactionRepository;
    private final AccountsRange accountRange;
    private final MoveMoneyFromInsideTransactionToDestinationAccountFlow flow;

    /**
     * 100ms delay check in db.
     */
    @Scheduled(fixedDelayString = "${income-transaction-check.fixed-delay-in-milliseconds:100}")
    public void checkIncomeTransaction() {
        List<InsideTransactionRecord> recordList = insideTransactionRepository.findInsideRequestsTo(accountRange);

        for (var request : recordList) {
            try {
                flow.process(request);
            } catch (RuntimeException e) {
                log.error("unexpected error, state is undefined. Please, take a look", e);
                insideTransactionRepository.update(request.buildAsError(e.getMessage()));
            }

        }
    }
}
