package com.example.auth.domain;

public interface TokenProvider {
    String create(TokenPayload payload);

    TokenPayload verify(String token);
}
