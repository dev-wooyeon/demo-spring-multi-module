package com.example.member.application;

import com.example.member.domain.Email;
import java.time.Instant;

public interface ActivationNotifier {
    void notify(Email email, String code, Instant expiresAt);
}
