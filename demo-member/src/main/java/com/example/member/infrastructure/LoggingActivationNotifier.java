package com.example.member.infrastructure;

import com.example.member.application.ActivationNotifier;
import com.example.member.domain.Email;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingActivationNotifier implements ActivationNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingActivationNotifier.class);

    @Override
    public void notify(Email email, String code, Instant expiresAt) {
        log.info("Activation requested for {} code={} expiresAt={}", email.getValue(), code, expiresAt);
    }
}
