package org.my.company.service.controllers;

import lombok.extern.slf4j.Slf4j;
import org.my.company.service.flows.exceptions.AccountNotFoundException;
import org.my.company.service.flows.exceptions.NotEnoughMoneyException;
import org.my.company.service.validation.DuplicateRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(value = {AccountNotFoundException.class})
    public ResponseEntity<String> accountNotFoundException(AccountNotFoundException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account " + ex.getAccountId() + " does not exist in the system");
    }

    @ExceptionHandler(value = {NotEnoughMoneyException.class})
    public ResponseEntity<String> notEnoughMoneyException(NotEnoughMoneyException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(value = {DuplicateRequestException.class})
    public ResponseEntity<String> duplicateRequestException(DuplicateRequestException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<String> illegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(value = {RuntimeException.class})
    public ResponseEntity<String> runtimeException(RuntimeException ex, WebRequest request) {
        log.error("unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
