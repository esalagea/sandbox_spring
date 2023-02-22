package com.esalagea.jwtsample.spring;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("com.esalagea.persistence.repository")
@EntityScan("com.esalagea.persistence.entity")
@EnableAutoConfiguration
public class TestDbConfig {
}
