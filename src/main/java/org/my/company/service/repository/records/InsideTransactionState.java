package org.my.company.service.repository.records;

/**
 * States of internal transaction
 *  +PENDING_REJECT,REJECTED if we want to return money automatically. (not covered with current code).
 */
public enum InsideTransactionState {
    /**
     * State for new transaction, then money moved from account to transaction queue.
     */
    NEW,
    /**
     * Happy case, terminal state, all fine
     */
    COMPLETE,
    /**
     * Exception while processing, not existing account (even if we validate the input).
     * Pretty rare case, but we need it to avoid infinity reprocessing.
     */
    ERROR,
    /**
     * Basically we have account, we have transaction, but someone is working with our account too long,
     * and we're going to postpone this transaction and redo it manually, later.
     */
    FAILED_TO_COMPLETE
}
