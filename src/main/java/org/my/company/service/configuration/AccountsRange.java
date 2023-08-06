package org.my.company.service.configuration;

/**
 * Responsibility - contains the range of accounts which current node will own.
 */
public record AccountsRange(long start, long finish) {
}
