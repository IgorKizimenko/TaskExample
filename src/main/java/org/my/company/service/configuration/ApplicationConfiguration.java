package org.my.company.service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    /**
     * by default - all range related to this node.
     */
    @Bean
    public AccountsRange accountsRange() {
        return new AccountsRange(0, Long.MAX_VALUE);
    }
}
