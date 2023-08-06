package org.my.company.service.controllers;

import lombok.AllArgsConstructor;
import org.my.company.service.repository.AccountsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * tech support api.
 */
@RequestMapping(path = "/emergency")
@RestController
@AllArgsConstructor
public class SupportController {

    private final AccountsRepository repository;
    @GetMapping(path = "/getBalance")
    public BigDecimal getBalance(long id) {

        var acc = repository.getAccountById(id);
        if (acc.isPresent()) {
            return acc.get().freeAmount();
        } else {
            throw new IllegalArgumentException("not presented account");
        }
    }
}
