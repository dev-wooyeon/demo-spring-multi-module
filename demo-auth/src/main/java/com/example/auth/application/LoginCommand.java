package com.example.auth.application;

public record LoginCommand(String email, String rawPassword) {
}
