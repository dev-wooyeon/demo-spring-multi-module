package com.example.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@Embeddable
public class Email {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Column(name = "email", nullable = false, unique = true, updatable = false)
    private String value;

    protected Email() {
        // for JPA
    }

    public Email(String value) {
        validate(value);
        this.value = value.toLowerCase();
    }

    private void validate(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (!EMAIL_PATTERN.matcher(candidate).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}
