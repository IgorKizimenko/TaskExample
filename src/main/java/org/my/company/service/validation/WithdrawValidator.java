package org.my.company.service.validation;

import lombok.RequiredArgsConstructor;
import org.my.company.service.Address;
import org.my.company.service.WithdrawalId;
import org.my.company.service.flows.income.InsideWithdraw;
import org.my.company.service.flows.income.OutsideWithdraw;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Responsibility - get raw data from rest endponts and withdrawalId that this data is matched with our contracts.
 * After validation return back the contracts.
 */
@Component
@RequiredArgsConstructor
public class WithdrawValidator {

    @Value("${withdraw.validation.max-address-length:100}")
    private final int maxAddressLength;
    //0.0000 at most
    @Value("${withdraw.validation.scale:4}")
    private final int scale;
    //3.....0. at most
    @Value("${withdraw.validation.precession:32}")
    private final int precession;

    public OutsideWithdraw validate(String uuid, long accountId, String address, BigDecimal amount) {
        if (address == null) {
            throw new IllegalArgumentException("Empty address filed is not allowed.");
        }

        if (address.length() > maxAddressLength) {
            throw new IllegalArgumentException("too big " + address.length() + " address in request. Only " + maxAddressLength + " allowed");
        }

        if (accountId <= 0) {
            throw new IllegalArgumentException("account id should be positive " + accountId);
        }

        return new OutsideWithdraw(validate(uuid), accountId, new Address(address), validateAndNormalizeScaleAdPrecession(amount));
    }

    public InsideWithdraw validate(String uuid, long accountFrom, long accountTo, BigDecimal amount) {
        if (uuid == null) {
            throw new IllegalArgumentException("Provided identifier is empty.");
        }
        if (accountFrom <= 0) {
            throw new IllegalArgumentException("'accountFrom' id should be positive " + accountFrom);
        }
        if (accountTo <= 0) {
            throw new IllegalArgumentException("account id should be positive " + accountTo);
        }

        if (accountTo == accountFrom) {
            throw new IllegalArgumentException("same account on 'from' and 'to' fields");
        }

        return new InsideWithdraw(validate(uuid), accountFrom, accountTo, validateAndNormalizeScaleAdPrecession(amount));
    }

    private BigDecimal validateAndNormalizeScaleAdPrecession(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount should not be empty");
        }

        if (BigDecimal.ZERO.compareTo(amount) > 0) {
            throw new IllegalArgumentException("Amount should be grater then 0");
        }

        //normalize data
        amount = amount.round(new MathContext(precession, RoundingMode.UP)).setScale(scale, RoundingMode.UP);

        if (BigDecimal.ZERO.compareTo(amount) > 0) {
            throw new IllegalArgumentException("Amount is too small.");
        }

        return amount;
    }

    public WithdrawalId validate(String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("Provided identifier is empty.");
        }

        return new WithdrawalId(UUID.fromString(uuid));
    }
}
