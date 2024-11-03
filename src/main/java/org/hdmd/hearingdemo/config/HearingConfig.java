package org.hdmd.hearingdemo.config;

import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class HearingConfig {

    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }
}
