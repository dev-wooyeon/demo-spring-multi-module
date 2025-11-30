package com.example;

import com.example.auth.application.AuthProperties;
import com.example.core.time.SystemTimeProvider;
import com.example.core.time.TimeProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example")
@EnableConfigurationProperties(AuthProperties.class)
@EnableJpaRepositories(basePackages = "com.example")
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

    @Bean
    TimeProvider timeProvider() {
        return new SystemTimeProvider();
    }
}
