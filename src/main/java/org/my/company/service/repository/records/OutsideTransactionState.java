package org.my.company.service.repository.records;

public enum OutsideTransactionState {
    /**
     * for state then we extract qty from account and placed request to table.
     */
    NEW,
    /**
     * then we call the service and starting to wait response
     */
    SENT,
    /**
     * Happy case, finished.
     */
    COMPLETE,
    /**
     * Undefined state, need to look at.
     */
    ERROR,
    /**
     * Happy case, reject received.
     */
    REJECTED,
    /**
     * We receive reject, but failed on process it.
     * It's not undefined state, we know exactly what to do (return back money).
     */
    REJECT_FAILED
}
